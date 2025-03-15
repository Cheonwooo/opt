package com.opt.ssafy.optback.domain.auth.application;

import com.opt.ssafy.optback.domain.auth.dto.KakaoMemberInfo;
import com.opt.ssafy.optback.domain.auth.dto.KakaoTokenResponse;
import com.opt.ssafy.optback.domain.auth.dto.SignInRequest;
import com.opt.ssafy.optback.domain.auth.dto.SignInResponse;
import com.opt.ssafy.optback.domain.auth.dto.SignUpRequest;
import com.opt.ssafy.optback.domain.auth.exception.DuplicatedSignUpException;
import com.opt.ssafy.optback.domain.badge.service.BadgeService;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.member.entity.Role;
import com.opt.ssafy.optback.domain.member.exception.MemberNotFoundException;
import com.opt.ssafy.optback.domain.member.repository.MemberRepository;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthService {

    public final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final TokenBlacklistService tokenBlacklistService;
    private final RedisTemplate<String, String> redisTemplate;
    private final BadgeService badgeService;

    public void signUp(SignUpRequest signUpRequest) {
        try {
            Member member = Member.builder()
                    .email(signUpRequest.getEmail())
                    .build();
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedSignUpException();
        }
    }

    public SignInResponse signIn(SignInRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(MemberNotFoundException::new);
        Authentication authentication = authenticate(member);
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);
        return SignInResponse.from(member, accessToken, refreshToken);
    }

    private Authentication authenticate(Member member) {
        User user = new User(String.valueOf(member.getId()), "",
                Collections.singletonList(new SimpleGrantedAuthority(member.getRole().name())));
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Value("${kakao.auth.client}")
    private String clientId;

    @Value("${kakao.auth.redirect}")
    private String redirectUri;

    @Value("${kakao.auth.token-uri}")
    private String tokenUri;

    @Value("${kakao.auth.member-info-uri}")
    private String memberInfoUri;

    @Value("${kakao.auth.logout}")
    private String logoutUri;

    @Value("${kakao.auth.unlink}")
    private String unlinkUri;

    // 로그인 & 회원가입
    public SignInResponse kakaoLogin(String accessCode, HttpServletResponse httpServletResponse) {
        String accessToken = requestKakaoAccessToken(accessCode);
        KakaoMemberInfo kakaoMember = requestKakaoMemberInfo(accessToken);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("카카오 엑세스 토큰을 확인할 수 없습니다");
        }
        Member member = memberRepository.findByEmail(kakaoMember.getEmail()).
                orElseGet(() -> registerNewMember(kakaoMember));

        Authentication authentication = authenticate(member);
        String jwt = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        // Redis에 토큰 저장
        redisTemplate.opsForValue().set("kakao_access_token:" + kakaoMember.getEmail(), accessToken, 6, TimeUnit.HOURS);

        httpServletResponse.addHeader("Authorization", "Bearer " + jwt);

        return SignInResponse.from(member, jwt, refreshToken);

    }

    private Member registerNewMember(KakaoMemberInfo kakaoMember) {
        Member newMember = Member.builder()
                .email(kakaoMember.getEmail())
                .imagePath(kakaoMember.getProfileImageUrl())
                .role(Role.ROLE_USER)
                .mainBadgeId(1)
                .build();
        badgeService.addBadge(newMember, 1);
        return memberRepository.save(newMember);
    }

    private String requestKakaoAccessToken(String accessCode) {
        System.out.println("🐿️ 전달된 카카오 엑세스 토큰: " + accessCode);

        String requestUri = String.format("%s?grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s",
                tokenUri, clientId, redirectUri, accessCode);

        System.out.println("🐿️ 카카오 토큰 요청 URL: " + requestUri);

        KakaoTokenResponse response = restTemplate.postForObject(requestUri, null, KakaoTokenResponse.class);

        System.out.println("🐿️ 카카오 응답: " + response);

        if (response == null || response.getAccessToken() == null) {
            throw new MalformedJwtException("카카오 엑세스 토큰을 가져올 수 없습니다");
        }

        System.out.println("🐿️ 발급된 액세스 토큰: " + response.getAccessToken());

        return response.getAccessToken();
    }

    private KakaoMemberInfo requestKakaoMemberInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<KakaoMemberInfo> response = restTemplate.exchange(memberInfoUri, HttpMethod.GET, requestEntity,
                KakaoMemberInfo.class);
        System.out.println(response.getBody().toString());
        if (response.getBody() == null) {
            throw new RuntimeException("카카오 사용자 정보를 가져올 수 없습니다");
        }
        return response.getBody();
    }

    // 로그아웃
    public void logout(String email, String accessToken) {
        System.out.println("🐿️ 로그아웃 실행: " + accessToken);

        String kakaoAccessToken = (String) redisTemplate.opsForValue().get("kakao_access_token:" + email);
        if (accessToken == null || accessToken.split("\\.").length != 3) {
            throw new MalformedJwtException("유효하지 않은 JWT 형식입니다.");
        }

        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        long expirationTime = jwtProvider.getExpirationTime(accessToken);

        // 블랙리스트에 추가하여 토큰 차단
        if (expirationTime > 0) {
            tokenBlacklistService.blackList(accessToken, expirationTime);
        }

        // 카카오 로그아웃
        boolean kakaoLogoutSuccess = requestKakaoLogout(kakaoAccessToken);
        if (!kakaoLogoutSuccess) {
            throw new MalformedJwtException("카카오 로그아웃 요청이 실패했습니다");
        }
        // SpringSecurity 인증 정보 삭제
        SecurityContextHolder.clearContext();
    }

    private boolean requestKakaoLogout(String accessCode) {
        System.out.println("🐿️ 카카오 로그아웃 요청: " + accessCode);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessCode);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(logoutUri, HttpMethod.POST, requestEntity,
                    String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 회원탈퇴
    public void deleteMember(String email, String accessToken) {
        System.out.println("🐿️ 회원탈퇴 실행: " + accessToken);
        String kakaoAccessToken = (String) redisTemplate.opsForValue().get("kakao_access_token:" + email);
        boolean kakaoUnlinkSuccess = requestKakaoUnlink(kakaoAccessToken);
        if (!kakaoUnlinkSuccess) {
            throw new RuntimeException("카카오 연결 끊기 요청 실패");
        }

        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        memberRepository.delete(member);

        redisTemplate.delete("kakao_access_token:" + email);

        SecurityContextHolder.clearContext();

    }

    private boolean requestKakaoUnlink(String accessToken) {
        System.out.println("🐿️ 카카오 연결끊기 요청: " + accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(unlinkUri, HttpMethod.POST, requestEntity,
                    String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            throw new RuntimeException("카카오 연결 끊기 요청 실패");
        }

    }

}

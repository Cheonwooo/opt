package com.opt.ssafy.optback.config;

import com.opt.ssafy.optback.domain.auth.application.JwtProvider;
import com.opt.ssafy.optback.domain.auth.application.TokenBlacklistService;
import com.opt.ssafy.optback.domain.auth.filter.AuthExceptionHandlerFilter;
import com.opt.ssafy.optback.domain.auth.filter.CustomAuthenticationEntryPoint;
import com.opt.ssafy.optback.domain.auth.filter.JwtAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ CORS 설정 추가
                .authorizeHttpRequests((auth) -> {
                            auth.requestMatchers("/auth/sign-in").permitAll();
                            auth.requestMatchers("/challenges/**").permitAll();
                            auth.requestMatchers("/auth/sign-up").permitAll();
                            auth.requestMatchers("/exercises").permitAll();
                            auth.requestMatchers("/exercises/{id}").permitAll();
                            auth.requestMatchers("/error").permitAll();
                            auth.requestMatchers("/auth/kakao").permitAll();
                            auth.requestMatchers("/auth/kakao-front").permitAll();
                            auth.requestMatchers("/challenges").permitAll();
                            auth.requestMatchers("/auth/sign-out").permitAll();
                            auth.requestMatchers("/auth/withdraw").permitAll();
                            auth.requestMatchers("/onboarding").permitAll();
                            auth.requestMatchers("/ws-chat/**").permitAll();
                            auth.requestMatchers("/app/**").permitAll();
                            auth.requestMatchers("/profile/**").permitAll();
                            auth.requestMatchers("/exercises").permitAll();
                            auth.requestMatchers("/health").permitAll();
                            auth.requestMatchers("/trainer-reviews/{id}").permitAll();
                            auth.requestMatchers("/challenges/{id}/contributions").permitAll();
                            auth.requestMatchers("/gyms/{id}").permitAll();
                            auth.requestMatchers(HttpMethod.GET, "/certificate").permitAll();
                            auth.requestMatchers(HttpMethod.GET, "/trainer-reviews/summary").permitAll();
                            auth.anyRequest().authenticated();
                        }
                ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new AuthExceptionHandlerFilter(), JwtAuthenticationFilter.class);
        http.exceptionHandling(manager -> manager.authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler(new AccessDeniedHandlerImpl()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider,
                                                           TokenBlacklistService tokenBlacklistService) {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    // ✅ CORS 설정을 위한 메서드 추가 (Live Server 허용)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*")); // ✅ 모든 오리진 허용 (테스트 환경)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
//

package com.opt.ssafy.optback.domain.push.application;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.push.dto.AndroidNotificationDTO;
import com.opt.ssafy.optback.domain.push.dto.FcmMessage;
import com.opt.ssafy.optback.domain.push.dto.FcmMessage.Message;
import com.opt.ssafy.optback.domain.push.dto.FcmMessage.Notification;
import com.opt.ssafy.optback.domain.push.dto.NotificationDto;
import com.opt.ssafy.optback.domain.push.entity.FcmToken;
import com.opt.ssafy.optback.domain.push.repository.FcmTokenRepository;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushService {
    private final UserDetailsServiceImpl userDetailsService;
    private final FcmTokenRepository fcmTokenRepository;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client = new OkHttpClient();

    @Value("${fcm.project.id}")
    private String PROJECT_ID;
    @Value("${fcm.secret.file}")
    private String secretFile;
    private static final String FCM_SEND_URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    public void sendPushMessage(String title, String body, Map<String, String> data, String token)
            throws IOException {
        String message = makeMessage(title, body, data, token);
        RequestBody requestBody = RequestBody.create(message, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                .post(requestBody)
                .url(String.format(FCM_SEND_URL, PROJECT_ID))
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new RuntimeException("FCM push message failed: " + response.body().string());
        }
        response.close();

    }

    private String makeMessage(String title, String body, Map<String, String> data, String token)
            throws JsonProcessingException {
        AndroidNotificationDTO android = new AndroidNotificationDTO();
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle(title);
        notificationDto.setBody(body);
        android.setNotification(notificationDto);

        FcmMessage fcmMessage = FcmMessage.builder()
                .message(Message.builder()
                        .token(token)
                        .data(data)
                        .android(android)
                        .notification(Notification.builder()
                                .title(title)
                                .body(body)
                                .build())
                        .build())
                .build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

    private String getAccessToken() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("firebase/" + secretFile);

        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(classPathResource.getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    public void save(String token) {
        Member member = userDetailsService.getMemberByContextHolder();

        // 기존 토큰 존재 여부 확인
        if (!fcmTokenRepository.existsByMemberIdAndToken(member.getId(), token)) {
            FcmToken fcmToken = FcmToken.builder()
                    .memberId(member.getId())
                    .token(token)
                    .build();
            fcmTokenRepository.save(fcmToken);
        }
    }

}

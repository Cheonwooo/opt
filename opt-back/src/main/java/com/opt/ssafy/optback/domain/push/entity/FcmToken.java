package com.opt.ssafy.optback.domain.push.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Builder
@Document(collection = "fcm_tokens")
public class FcmToken {

    @Id
    private String id;
    @Field(name = "member_id")
    private Integer memberId;
    @Field(name = "token")
    private String token;
}

package com.opt.ssafy.optback.domain.gym.dto;

import com.opt.ssafy.optback.domain.gym.entity.Gym;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GymDetailResponse {
    private Integer id;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static GymDetailResponse from(Gym gym) {
        return GymDetailResponse.builder()
                .id(gym.getId())
                .name(gym.getGymName())
                .latitude(gym.getLatitude())
                .longitude(gym.getLongitude())
                .build();
    }

}

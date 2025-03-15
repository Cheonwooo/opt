package com.opt.ssafy.optback.domain.gym.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "gym")
public class Gym {

    @Id
    private int id;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "full_address", nullable = false)
    private String fullAddress;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "gym_name", nullable = false)
    private String gymName;

    @Column(name = "latitude", nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false)
    private BigDecimal longitude;

}

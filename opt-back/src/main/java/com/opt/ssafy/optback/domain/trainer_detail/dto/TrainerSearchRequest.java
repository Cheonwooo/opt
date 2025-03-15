package com.opt.ssafy.optback.domain.trainer_detail.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerSearchRequest {

    private BigDecimal myLatitude;
    private BigDecimal myLongitude;
    Boolean isOneDayAvailable;
    private String name;
    private String address;
    private List<String> interests;
    private String sortBy;

}

package com.opt.ssafy.optback.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GptRequest {

    private String model;
    private String prompt;
    private float temperature;
    
}

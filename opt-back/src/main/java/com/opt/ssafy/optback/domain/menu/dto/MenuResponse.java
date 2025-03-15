package com.opt.ssafy.optback.domain.menu.dto;

import com.opt.ssafy.optback.domain.menu.entity.Menu;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuResponse {

    private int id;
    private String name;
    private int trainerId;
    private int price;
    private int totalSessions;

    public MenuResponse(Menu menu) {
        this.id = menu.getId();
        this.name = menu.getName();
        this.trainerId = menu.getTrainerId();
        this.price = menu.getPrice();
        this.totalSessions = menu.getTotalSessions();
    }
}

package com.opt.ssafy.optback.domain.gym.api;

import com.opt.ssafy.optback.domain.gym.application.GymService;
import com.opt.ssafy.optback.domain.gym.dto.GymDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gyms")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    @GetMapping("/{id}")
    public ResponseEntity<GymDetailResponse> getGymDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(gymService.getGymDetail(id));
    }

}

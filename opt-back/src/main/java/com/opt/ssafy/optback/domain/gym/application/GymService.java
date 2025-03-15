package com.opt.ssafy.optback.domain.gym.application;

import com.opt.ssafy.optback.domain.gym.dto.GymDetailResponse;
import com.opt.ssafy.optback.domain.gym.entity.Gym;
import com.opt.ssafy.optback.domain.gym.exception.GymNotFoundException;
import com.opt.ssafy.optback.domain.gym.repository.GymRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;

    public GymDetailResponse getGymDetail(Integer id) {
        Gym gym = gymRepository.findById(id).orElseThrow(() -> new GymNotFoundException("헬스장 정보를 찾을 수 없습니다"));
        return GymDetailResponse.from(gym);
    }

}

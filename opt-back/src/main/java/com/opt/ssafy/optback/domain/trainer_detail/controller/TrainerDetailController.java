package com.opt.ssafy.optback.domain.trainer_detail.controller;

import com.opt.ssafy.optback.domain.member.repository.TrainerSpecialtyRepository;
import com.opt.ssafy.optback.domain.trainer_detail.Service.TrainerDetailService;
import com.opt.ssafy.optback.domain.trainer_detail.dto.TrainerDetailResponse;
import com.opt.ssafy.optback.domain.trainer_detail.dto.TrainerSearchRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trainers")
public class TrainerDetailController {

    private final TrainerDetailService trainerService;
    private final TrainerSpecialtyRepository trainerSpecialtyRepository;

    // 트레이너 상세 정보 조회
    @GetMapping("/details/{trainer_id}")
    public ResponseEntity<TrainerDetailResponse> getTrainerByTrainerId(@PathVariable("trainer_id") int trainerId) {
        TrainerDetailResponse response = trainerService.getTrainerDetail(trainerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/specialties")
    public ResponseEntity<List<String>> getTrainerSpecialties(@PathVariable int id) {
        List<String> specialties = trainerService.getTrainerSpecialties(id);
        return ResponseEntity.ok(specialties);
    }

    // 트레이너 검색
    @PostMapping("/search")
    public ResponseEntity<Page<TrainerDetailResponse>> getSearchTrainers(
            @RequestBody TrainerSearchRequest request, Pageable pageable) {
        List<TrainerDetailResponse> responses = trainerService.searchAndSortTrainers(request);

        return ResponseEntity.ok(convertListToPage(responses, pageable));
    }

    @PostMapping("/recommends")
    public ResponseEntity<Page<TrainerDetailResponse>> getRecommendTrainers(
            @RequestBody TrainerSearchRequest request, Pageable pageable) {
        List<TrainerDetailResponse> responses = trainerService.getRecommendedTrainers(request);

        return ResponseEntity.ok(convertListToPage(responses, pageable));
    }

    // Pageable 변환용 메서드
    private <T> Page<T> convertListToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<T> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }

}

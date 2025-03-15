package com.opt.ssafy.optback.domain.exercise.api;

import com.opt.ssafy.optback.domain.exercise.application.ExerciseService;
import com.opt.ssafy.optback.domain.exercise.dto.ExerciseDetailResponse;
import com.opt.ssafy.optback.domain.exercise.dto.ExerciseFavoriteRequest;
import com.opt.ssafy.optback.domain.exercise.dto.ExerciseInfoResponse;
import com.opt.ssafy.optback.global.dto.SuccessResponse;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public ResponseEntity<Slice<ExerciseInfoResponse>> getFilteredExercises(
            @Nullable @RequestParam(required = false) String name,
            @Nullable @RequestParam(required = false) String bodyPart,
            Pageable pageable) {

        Slice<ExerciseInfoResponse> exerciseInfos = exerciseService.getFilteredExerciseInfos(name, bodyPart, pageable);
        return ResponseEntity.ok(exerciseInfos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseDetailResponse> getExerciseDetailById(@PathVariable Integer id) {
        ExerciseDetailResponse exerciseDetail = exerciseService.getExerciseDetailById(id);
        return ResponseEntity.ok(exerciseDetail);
    }


    @GetMapping("/favorites")
    public ResponseEntity<List<ExerciseInfoResponse>> getFavoriteExercises() {
        List<ExerciseInfoResponse> exerciseInfos = exerciseService.getMyFavoriteExerciseInfos();
        return ResponseEntity.ok(exerciseInfos);
    }

    @PostMapping("/favorites")
    public ResponseEntity<SuccessResponse> addFavoriteExercise(
            @RequestBody ExerciseFavoriteRequest exerciseFavoriteRequest) {
        exerciseService.addFavoriteExercise(exerciseFavoriteRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorites")
    public ResponseEntity<SuccessResponse> deleteFavoriteExercise(
            @RequestBody ExerciseFavoriteRequest exerciseFavoriteRequest) {
        exerciseService.deleteFavoriteExercise(exerciseFavoriteRequest);
        return ResponseEntity.ok().build();
    }

}

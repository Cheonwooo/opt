package com.opt.ssafy.optback.domain.exercise.api;

import com.opt.ssafy.optback.domain.exercise.application.ExerciseRecordService;
import com.opt.ssafy.optback.domain.exercise.dto.CreateExerciseRecordRequest;
import com.opt.ssafy.optback.domain.exercise.dto.ExerciseRecordResponse;
import com.opt.ssafy.optback.domain.exercise.dto.UpdateExerciseRecordRequest;
import com.opt.ssafy.optback.domain.meal_record.dto.MonthlyRecordResponse;
import com.opt.ssafy.optback.domain.meal_record.service.MealRecordService;
import com.opt.ssafy.optback.global.dto.SuccessResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exercise-records")
public class ExerciseRecordController {

    private final ExerciseRecordService exerciseRecordService;
    private final MealRecordService mealRecordService;

    @GetMapping
    public ResponseEntity<List<ExerciseRecordResponse>> findExerciseRecordsByDate(
            @RequestParam(value = "date") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date) {
        return ResponseEntity.ok(exerciseRecordService.findExerciseRecordsByDate(date));
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> findExerciseRecordsByYearAndMonth(@RequestParam Integer year,
                                                               @RequestParam Integer month) {
        List<LocalDate> exerciseDates = exerciseRecordService.findExerciseRecordsByYearAndMonth(year, month);
        List<LocalDate> mealDates = mealRecordService.findMealRecordsByYearAndMonth(year, month);
        return ResponseEntity.ok(MonthlyRecordResponse.builder()
                .exerciseDates(exerciseDates)
                .mealDates(mealDates)
                .build());
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> createExerciseRecord(CreateExerciseRecordRequest request)
            throws IOException {
        exerciseRecordService.createExerciseRecord(request, request.getMedias());
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("등록되었습니다").build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deleteExerciseRecord(@PathVariable Integer id) {
        exerciseRecordService.deleteExerciseRecord(id);
        return ResponseEntity.ok(SuccessResponse.builder().message("삭제되었습니다").build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SuccessResponse> updateExerciseRecord(@PathVariable Integer id,
                                                                UpdateExerciseRecordRequest request)
            throws IOException {
        exerciseRecordService.updateExerciseRecord(id, request, request.getMedias());
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("수정되었습니다").build());
    }

}

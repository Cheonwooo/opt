package com.opt.ssafy.optback.domain.meal_record.service;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.meal_record.dto.CreateMealRecord;
import com.opt.ssafy.optback.domain.meal_record.dto.MealNutritionDto;
import com.opt.ssafy.optback.domain.meal_record.dto.MealRecordRequest;
import com.opt.ssafy.optback.domain.meal_record.entity.MealRecord;
import com.opt.ssafy.optback.domain.meal_record.exception.MealRecordNotFoundException;
import com.opt.ssafy.optback.domain.meal_record.exception.MealRecordNotSaveException;
import com.opt.ssafy.optback.domain.meal_record.repository.MealRecordRepository;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.global.application.S3Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealRecordService {

    private final MealRecordRepository mealRecordRepository;
    private final MealNutritionAiService mealNutritionAiService;
    private final S3Service s3Service;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${meal.image.bucket.name}")
    private String bucket;

    // 식단 조회
    public MealRecord findMealRecordByMemberTypeAndDate(MealRecordRequest mealRecordRequest) {
        Member member = userDetailsService.getMemberByContextHolder();
        int memberId = member.getId();
        String type = mealRecordRequest.getType();
        LocalDate date = mealRecordRequest.getCreatedDate();

        Optional<MealRecord> record = mealRecordRepository.findByMemberIdAndTypeAndCreatedDate(memberId, type, date);

        if (record.isEmpty()) {
            throw new MealRecordNotFoundException("식단 기록을 조회할 수 없습니다");
        }

        return record.get();

    }

    // 식단 저장
    @Transactional
    public MealRecord saveMealRecord(CreateMealRecord mealRecordRequest, MultipartFile images) {
        if (images == null || images.isEmpty()) {
            throw new MealRecordNotSaveException("식단 이미지가 없습니다");
        }

        // 오늘 날짜 확인
        LocalDate date = mealRecordRequest.getCreatedDate();
        checkToday(date);

        // 이미지 업로드
        String imagePath = uploadImageToS3(images);

        Member member = userDetailsService.getMemberByContextHolder();
        int memberId = member.getId();
        String type = mealRecordRequest.getType();
        MealRecord mealRecord = MealRecord.builder()
                .memberId(memberId)
                .createdDate(date)
                .imagePath(imagePath)
                .type(type)
                .build();

        MealRecord savedRecord = mealRecordRepository.save(mealRecord);

        return savedRecord;
    }

    private void checkToday(LocalDate selectedDate) {
        LocalDate today = LocalDate.now();
        if (!selectedDate.isEqual(today)) {
            throw new MealRecordNotSaveException("오늘 날짜가 아닙니다");
        }
    }

    private String uploadImageToS3(MultipartFile image) {
        try {
            return s3Service.uploadImageFile(image, bucket);
        } catch (Exception e) {
            throw new MealRecordNotSaveException("식단 이미지 업로드 실패");
        }
    }

    // 식단 분석
    @Transactional
    public MealRecord analyzeMealImage(MealRecord record) {
        String imagePath = record.getImagePath();
        MealNutritionDto nutrition = mealNutritionAiService.analyzeMealImage(imagePath);

        record.setNutrition(nutrition);

        return record;
    }

    // 식단 수정
    @Transactional
    public MealRecord updateMealRecord(MealRecordRequest savedRequest,
                                       MultipartFile updateImage) {
        // 기존 데이터 및 변경할 데이터 오늘 날짜 확인
        checkToday(savedRequest.getCreatedDate());

        MealRecord findRecord = findMealRecordByMemberTypeAndDate(savedRequest);
        if (findRecord == null) {
            throw new MealRecordNotSaveException("기존 데이터를 찾을 수 없습니다.");
        }

        // 이미지가 변경된 경우
        if (updateImage != null) {
            String newImagePath = uploadImageToS3(updateImage);
            deleteImageFromS3(findRecord.getImagePath());
            findRecord.setNewImage(newImagePath);
        }

        return findRecord;
    }

    private void deleteImageFromS3(String imagePath) {
        try {
            s3Service.deleteMedia(imagePath, bucket);
        } catch (Exception e) {
            throw new MealRecordNotSaveException("식단 이미지 삭제 실패");
        }
    }

    // 식단 삭제
    @Transactional
    public void deleteMealRecord(MealRecordRequest mealRecordRequest) {
        // 오늘 날짜 확인
        LocalDate date = mealRecordRequest.getCreatedDate();
        checkToday(date);

        MealRecord findRecord = findMealRecordByMemberTypeAndDate(mealRecordRequest);

        try {
            String imagePath = findRecord.getImagePath();
            mealRecordRepository.delete(findRecord);
            deleteImageFromS3(imagePath);
        } catch (Exception e) {
            throw new MealRecordNotSaveException("식단 삭제 실패");
        }

    }

    @Transactional
    public MealRecord updateNutrition(MealRecordRequest mealRecordRequest) {
        MealRecord findRecord = findMealRecordByMemberTypeAndDate(mealRecordRequest);
        MealNutritionDto nutrition = mealNutritionAiService.analyzeMealImage(findRecord.getImagePath());

        if (nutrition == null) {
            throw new MealRecordNotSaveException("AI 분석 실패");
        }

        findRecord.setNutrition(nutrition);

        return findRecord;
    }

    public List<LocalDate> findMealRecordsByYearAndMonth(Integer year, Integer month) {
        Member member = userDetailsService.getMemberByContextHolder();
        List<LocalDate> dates = mealRecordRepository.findDistinctDatesByYearAndMonth(year, month, member.getId());
        return dates.stream().sorted().collect(Collectors.toList()); // 날짜 정렬 후 반환
    }

}

package com.opt.ssafy.optback.domain.exercise.application;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.exercise.dto.CreateExerciseRecordRequest;
import com.opt.ssafy.optback.domain.exercise.dto.ExerciseRecordResponse;
import com.opt.ssafy.optback.domain.exercise.dto.UpdateExerciseRecordRequest;
import com.opt.ssafy.optback.domain.exercise.entity.Exercise;
import com.opt.ssafy.optback.domain.exercise.entity.ExerciseRecord;
import com.opt.ssafy.optback.domain.exercise.entity.ExerciseRecordMedia;
import com.opt.ssafy.optback.domain.exercise.exception.ExerciseNotFoundException;
import com.opt.ssafy.optback.domain.exercise.repository.ExerciseRecordMediaRepository;
import com.opt.ssafy.optback.domain.exercise.repository.ExerciseRecordRepository;
import com.opt.ssafy.optback.domain.exercise.repository.ExerciseRepository;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.global.application.S3Service;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseRecordService {

    private final S3Service s3Service;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final ExerciseRecordMediaRepository exerciseRecordMediaRepository;
    @Value("${exercise.media.bucket.name}")
    private String bucketName;

    public List<ExerciseRecordResponse> findExerciseRecordsByDate(LocalDate date) {
        Member member = userDetailsService.getMemberByContextHolder();

        return member.getExerciseRecords().stream()
                .filter(exerciseRecord -> exerciseRecord.getCreatedAt().isEqual(date))
                .map(ExerciseRecordResponse::from)
                .toList();
    }

    public void createExerciseRecord(CreateExerciseRecordRequest request, List<MultipartFile> medias)
            throws IOException {
        Member member = userDetailsService.getMemberByContextHolder();
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ExerciseNotFoundException("운동을 찾지 못함"));
        ExerciseRecord exerciseRecord = exerciseRecordRepository.save(ExerciseRecord.builder()
                .member(member)
                .exercise(exercise)
                .rep(request.getRep())
                .sets(request.getSet())
                .weight(request.getWeight())
                .build());
        if (medias != null && !medias.isEmpty()) {
            saveExerciseMedias(exerciseRecord.getId(), medias);
        }
    }

    private void saveExerciseMedias(Integer exerciseRecordId, List<MultipartFile> medias) throws IOException {
        ExerciseRecord exerciseRecord = exerciseRecordRepository.findById(exerciseRecordId).orElseThrow();
        for (MultipartFile media : medias) {
            String fileName = media.getOriginalFilename();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            List<ExerciseRecordMedia> recordMedias = new ArrayList<>();
            if (List.of("jpg", "jpeg", "png", "gif").contains(extension)) {
                String path = s3Service.uploadImageFile(media, bucketName);
                ExerciseRecordMedia recordMedia = ExerciseRecordMedia.builder()
                        .exerciseRecord(exerciseRecord)
                        .mediaType("IMAGE")
                        .mediaPath(path)
                        .build();
                recordMedias.add(recordMedia);
            }
            if (List.of("mp4", "avi", "mov", "mkv").contains(extension)) {
                String path = s3Service.uploadVideoFile(media, bucketName);
                ExerciseRecordMedia recordMedia = ExerciseRecordMedia.builder()
                        .exerciseRecord(exerciseRecord)
                        .mediaType("VIDEO")
                        .mediaPath(path)
                        .build();
                recordMedias.add(recordMedia);
            }
            exerciseRecordMediaRepository.saveAll(recordMedias);
        }
    }

    public void deleteExerciseRecord(Integer id) {
        ExerciseRecord exerciseRecord = exerciseRecordRepository.findById(id).orElseThrow();
        List<ExerciseRecordMedia> medias = exerciseRecord.getMedias();
        medias.forEach(exerciseRecordMedia -> s3Service.deleteMedia(exerciseRecordMedia.getMediaPath(), bucketName));
        exerciseRecordRepository.delete(exerciseRecord);
    }

    @Transactional
    public void updateExerciseRecord(Integer exerciseRecordId,
                                     UpdateExerciseRecordRequest request,
                                     List<MultipartFile> newMedias) throws IOException {
        ExerciseRecord exerciseRecord = exerciseRecordRepository.findById(exerciseRecordId).orElseThrow();
        exerciseRecord.update(request.getSet(), request.getRep(), request.getWeight());

        List<ExerciseRecordMedia> medias = exerciseRecord.getMedias();
        for (ExerciseRecordMedia media : medias) {
            if (request.getMediaIdsToDelete() != null && request.getMediaIdsToDelete().contains(media.getId())) {
                s3Service.deleteMedia(media.getMediaPath(), bucketName);
            }
        }
        if (request.getMediaIdsToDelete() != null && !request.getMediaIdsToDelete().isEmpty()) {
            medias.removeIf(media -> request.getMediaIdsToDelete().contains(media.getId()));
        }
        if (newMedias != null && !newMedias.isEmpty()) {
            saveExerciseMedias(exerciseRecord.getId(), newMedias);
        }
    }

    public List<LocalDate> findExerciseRecordsByYearAndMonth(Integer year, Integer month) {
        Member member = userDetailsService.getMemberByContextHolder();
        List<LocalDate> dates = exerciseRecordRepository.findDistinctDatesByYearAndMonth(year, month, member);
        return dates.stream().sorted().collect(Collectors.toList()); // 날짜 정렬 후 반환
    }

}

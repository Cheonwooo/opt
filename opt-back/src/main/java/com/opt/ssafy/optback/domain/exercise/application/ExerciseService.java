package com.opt.ssafy.optback.domain.exercise.application;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.exercise.dto.ExerciseDetailResponse;
import com.opt.ssafy.optback.domain.exercise.dto.ExerciseFavoriteRequest;
import com.opt.ssafy.optback.domain.exercise.dto.ExerciseInfoResponse;
import com.opt.ssafy.optback.domain.exercise.entity.Exercise;
import com.opt.ssafy.optback.domain.exercise.entity.FavoriteExercise;
import com.opt.ssafy.optback.domain.exercise.entity.QExercise;
import com.opt.ssafy.optback.domain.exercise.exception.ExerciseNotFoundException;
import com.opt.ssafy.optback.domain.exercise.repository.ExerciseFavoriteRepository;
import com.opt.ssafy.optback.domain.exercise.repository.ExerciseRepository;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExerciseService {

    private final UserDetailsServiceImpl userDetailsService;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseFavoriteRepository exerciseFavoriteRepository;
    private final JPAQueryFactory queryFactory;

    public Page<ExerciseInfoResponse> getFilteredExerciseInfos(String name, String bodyPart, Pageable pageable) {
        QExercise qExercise = QExercise.exercise;
        BooleanBuilder filterBuilder = new BooleanBuilder();

        if (name != null && !name.isEmpty()) {
            filterBuilder.and(qExercise.name.containsIgnoreCase(name));
        }

        if (bodyPart != null && !bodyPart.isEmpty()) {
            ExerciseQueryHelper.addBodyPartFilter(bodyPart, qExercise, filterBuilder);
        }

        long totalCount = queryFactory.selectFrom(qExercise)
                .where(filterBuilder)
                .fetchCount();

        // 로그인 여부 확인 후 즐겨찾기 운동 목록 조회
        List<Integer> favoriteIds = new ArrayList<>();
        if (!userDetailsService.isAnonymous()) {
            Member member = userDetailsService.getMemberByContextHolder();
            favoriteIds = member.getFavoriteExercises().stream()
                    .map(favoriteExercise -> favoriteExercise.getExercise().getId()).toList();
        }

        // 최종 운동 데이터 조회
        List<Integer> finalFavoriteIds = favoriteIds;
        List<ExerciseInfoResponse> exerciseInfos = queryFactory.selectFrom(qExercise)
                .where(filterBuilder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(exercise -> ExerciseInfoResponse.from(exercise, finalFavoriteIds))
                .toList();

        return new PageImpl<>(exerciseInfos, pageable, totalCount);
    }

    public ExerciseDetailResponse getExerciseDetailById(Integer id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException("찾을 수 없는 운동입니다"));
        return ExerciseDetailResponse.from(exercise);
    }

    public List<ExerciseInfoResponse> getMyFavoriteExerciseInfos() {
        Member member = userDetailsService.getMemberByContextHolder();
        List<FavoriteExercise> exercises = member.getFavoriteExercises();
        return exercises.stream()
                .map(favoriteExercise -> favoriteExercise.getExercise())
                .map(ExerciseInfoResponse::from).toList();
    }

    public void addFavoriteExercise(ExerciseFavoriteRequest exerciseFavoriteRequest) {
        Member member = userDetailsService.getMemberByContextHolder();
        Exercise exercise = exerciseRepository.findById(exerciseFavoriteRequest.getExerciseId())
                .orElseThrow(() -> new ExerciseNotFoundException("찾을 수 없는 운동입니다"));
        FavoriteExercise favoriteExercise = FavoriteExercise.builder()
                .member(member)
                .exercise(exercise)
                .build();
        exerciseFavoriteRepository.save(favoriteExercise);
    }

    public void deleteFavoriteExercise(ExerciseFavoriteRequest exerciseFavoriteRequest) {
        Member member = userDetailsService.getMemberByContextHolder();
        Exercise exercise = exerciseRepository.findById(exerciseFavoriteRequest.getExerciseId())
                .orElseThrow(() -> new ExerciseNotFoundException("찾을 수 없는 운동입니다"));
        exerciseFavoriteRepository.deleteByExerciseAndMember(exercise, member);
    }

}

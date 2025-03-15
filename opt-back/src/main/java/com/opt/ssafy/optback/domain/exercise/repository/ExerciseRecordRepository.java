package com.opt.ssafy.optback.domain.exercise.repository;

import com.opt.ssafy.optback.domain.exercise.entity.ExerciseRecord;
import com.opt.ssafy.optback.domain.member.entity.Member;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Integer> {

    List<ExerciseRecord> findByMemberIdAndCreatedAtBetween(int memberId, LocalDate createdAtAfter,
                                                           LocalDate createdAtBefore);

    // 특정 운동 ID에 대한 사용자의 총 거리 합산
    @Query("SELECT COALESCE(SUM(e.distance), 0) FROM ExerciseRecord e WHERE e.member.id = :memberId AND e.exercise.id = :exerciseId")
    int getTotalDistanceByMemberAndExercise(@Param("memberId") int memberId, @Param("exerciseId") int exerciseId);

    // 특정 운동 ID에 대한 사용자의 총 무게 합산
    @Query("SELECT COALESCE(SUM(e.weight), 0) FROM ExerciseRecord e WHERE e.member.id = :memberId AND e.exercise.id = :exerciseId")
    int getTotalWeightByMemberAndExercise(@Param("memberId") int memberId, @Param("exerciseId") int exerciseId);

    // 특정 운동 ID에 대한 횟수 조회
    @Query("SELECT COUNT(e) FROM ExerciseRecord e WHERE e.member.id = :memberId AND e.exercise.id = :exerciseId")
    int getExerciseCompletionCount(@Param("memberId") int memberId, @Param("exerciseId") int exerciseId);

    @Query("SELECT DISTINCT e.createdAt FROM ExerciseRecord e " +
            "WHERE YEAR(e.createdAt) = :year AND MONTH(e.createdAt) = :month "
            + "AND e.member = :member")
    List<LocalDate> findDistinctDatesByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month,
                                                    @Param("member") Member member);
}

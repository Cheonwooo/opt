package com.opt.ssafy.optback.domain.exercise.repository;

import com.opt.ssafy.optback.domain.exercise.entity.Exercise;
import com.opt.ssafy.optback.domain.exercise.entity.FavoriteExercise;
import com.opt.ssafy.optback.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseFavoriteRepository extends JpaRepository<FavoriteExercise, Integer> {
    void deleteByExerciseAndMember(Exercise exercise, Member member);
}

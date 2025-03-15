package com.opt.ssafy.optback.domain.exercise.repository;

import com.opt.ssafy.optback.domain.exercise.entity.ExerciseRecordMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRecordMediaRepository extends JpaRepository<ExerciseRecordMedia, Integer> {
}

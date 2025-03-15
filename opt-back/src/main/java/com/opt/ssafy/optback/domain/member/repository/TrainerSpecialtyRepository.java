package com.opt.ssafy.optback.domain.member.repository;


import com.opt.ssafy.optback.domain.member.entity.TrainerSpecialty;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerSpecialtyRepository extends JpaRepository<TrainerSpecialty, Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM TrainerSpecialty ts WHERE ts.trainerId = :trainerId")
    void deleteByTrainerId(@Param("trainerId") Integer trainerId);

    @Query("SELECT ts.keyword FROM TrainerSpecialty ts WHERE ts.trainerId = :trainerId")
    List<String> findKeywordsByTrainerId(@Param("trainerId") Integer trainerId);

    List<TrainerSpecialty> findByTrainerId(int trainerId);
}

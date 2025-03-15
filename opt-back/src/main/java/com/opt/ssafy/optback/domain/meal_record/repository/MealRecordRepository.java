package com.opt.ssafy.optback.domain.meal_record.repository;

import com.opt.ssafy.optback.domain.meal_record.entity.MealRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealRecordRepository extends JpaRepository<MealRecord, Integer> {

    // 중복 확인
    Optional<MealRecord> findByMemberIdAndTypeAndCreatedDate(int memberId, String type, LocalDate createdDate);

    List<MealRecord> findByMemberIdAndCreatedDateBetween(int memberId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT m.createdDate FROM MealRecord m " +
            "WHERE YEAR(m.createdDate) = :year AND MONTH(m.createdDate) = :month "
            + "AND m.memberId = :memberId")
    List<LocalDate> findDistinctDatesByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month,
                                                    @Param("memberId") Integer memberId);
}

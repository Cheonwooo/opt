package com.opt.ssafy.optback.domain.challenge.repository;

import com.opt.ssafy.optback.domain.challenge.entity.Challenge;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {
    List<Challenge> findByHostId(int hostId);

    List<Challenge> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(Date startDateIsLessThan,
                                                                           Date endDateIsGreaterThan);

    List<Challenge> findByEndDateLessThan(Date endDateIsLessThan);

    List<Challenge> findByStartDateGreaterThan(Date startDateIsGreaterThan);

    List<Challenge> findByIdIn(List<Integer> challengeIds);

    @Query("SELECT c FROM Challenge c WHERE c.endDate = :targetDate")
    List<Challenge> findByEndDate(@Param("targetDate") Date targetDate);

    List<Challenge> findByStartDateAndStatus(Date startTargetDate, String open);

    List<Challenge> findByEndDateAndStatus(Date endTargetDate, String progress);

    Page<Challenge> findAllByStatusOrderByIdDesc(String status, Pageable pageable);

}

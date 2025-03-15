package com.opt.ssafy.optback.domain.counsel.repository;

import com.opt.ssafy.optback.domain.counsel.entity.Counsel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounselRepository extends JpaRepository<Counsel, Integer> {
    List<Counsel> findByTrainerId(int id);

    List<Counsel> findByMemberId(int id);

}

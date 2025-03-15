package com.opt.ssafy.optback.domain.trainer_detail.Repository;

import com.opt.ssafy.optback.domain.trainer_detail.entity.TrainerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerDetailRepository extends
        JpaRepository<TrainerDetail, Integer>,
        JpaSpecificationExecutor {

}

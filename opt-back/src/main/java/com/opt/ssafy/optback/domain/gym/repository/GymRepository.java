package com.opt.ssafy.optback.domain.gym.repository;

import com.opt.ssafy.optback.domain.gym.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymRepository extends JpaRepository<Gym, Integer> {
}

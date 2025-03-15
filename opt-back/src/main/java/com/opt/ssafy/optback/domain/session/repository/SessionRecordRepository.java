package com.opt.ssafy.optback.domain.session.repository;

import com.opt.ssafy.optback.domain.session.entity.SessionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRecordRepository extends JpaRepository<SessionRecord, Integer> {
    List<SessionRecord> findBySessionId(Integer sessionId);
}

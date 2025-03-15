package com.opt.ssafy.optback.domain.challenge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "challenge_record")
public class ChallengeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // ChallengeMember를 직접 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_member_id", nullable = false)
    private ChallengeMember challengeMember;

    // 챌린지와 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    // 단순 Member 식별자 (추후 Member 엔티티와 연관관계를 맺을 수 있음)
    @Column(name = "member_id")
    private int memberId;

    @Temporal(TemporalType.DATE)
    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "count")
    private Integer count;

    @Column
    private Integer duration;

    @Column
    private Integer distance;

    @Column(name = "is_passed")
    private boolean isPassed;

    public void setCount(Integer counts) {
        count = counts;
    }

    public void setIsPassed() {
        isPassed = true;
    }

    public void setDuration(Integer newDuration) {
        duration = newDuration;
    }

    public void setDistance(Integer newDistance) {
        distance = newDistance;
    }
}

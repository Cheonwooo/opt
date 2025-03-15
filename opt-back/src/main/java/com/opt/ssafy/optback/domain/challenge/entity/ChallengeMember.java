package com.opt.ssafy.optback.domain.challenge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
@Table(name = "challenge_member")
public class ChallengeMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "challenge_id")
    private int challengeId;

    @Column(name = "member_id")
    private int memberId;

    @Column(name = "status", columnDefinition = "char(10)")
    private String status;  // 예: "JOINED", "ENDED", 등

    // DB에서는 join_at이 date 타입이며 default (curdate())로 설정됨
    @Temporal(TemporalType.DATE)
    @Column(name = "join_at", nullable = false)
    private Date joinAt;

    public void setStatus(String ended) {
        this.status = ended;
    }
}

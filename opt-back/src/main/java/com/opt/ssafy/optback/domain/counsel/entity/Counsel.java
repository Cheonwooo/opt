package com.opt.ssafy.optback.domain.counsel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "counsel")
public class Counsel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 상담 시작 시각
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    // 상담 종료 시각
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    // 상담을 신청한 회원의 ID (토큰에서 가져옴)
    @Column(name = "member_id", nullable = false)
    private int memberId;

    // 상담을 진행할 트레이너의 ID
    @Column(name = "trainer_id", nullable = false)
    private int trainerId;

    // 상담 상태 (예: "REQUESTED", "APPROVED", "CANCELLED" 등)
    @Column(name = "status", length = 10)
    private String status;

    // 상담 일정을 수정할 때 사용되는 도메인 메서드
    public void updateCounsel(LocalDateTime newStartAt, LocalDateTime newEndAt, String newStatus) {
        if (newStartAt != null) {
            this.startAt = newStartAt;
        }
        if (newEndAt != null) {
            this.endAt = newEndAt;
        }
        if (newStatus != null) {
            this.status = newStatus;
        }
    }

}

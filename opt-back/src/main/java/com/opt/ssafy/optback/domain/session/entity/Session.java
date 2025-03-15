package com.opt.ssafy.optback.domain.session.entity;

import com.opt.ssafy.optback.domain.ticket.entity.Ticket;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // PT 이용권 ID
    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    // 회차 번호
    @Column(name = "number")
    private int number;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_member_signed")
    private boolean isMemberSigned;

    @Column(name = "is_trainer_signed")
    private boolean isTrainerSigned;

    /**
     * 수업 일정 업데이트 도메인 메서드. 전달된 값이 null이면 해당 필드는 변경하지 않습니다.
     */
    public void updateSession(LocalDateTime newStartAt, LocalDateTime newEndAt) {
        if (newStartAt != null) {
            this.startAt = newStartAt;
        }
        if (newEndAt != null) {
            this.endAt = newEndAt;
        }
    }

    public void markTrainerSigned() {
        this.isTrainerSigned = true;
    }

    public void markMemberSigned() {
        this.isMemberSigned = true;
    }

}

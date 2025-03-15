package com.opt.ssafy.optback.domain.session.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "session_record")
public class SessionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 해당 수업(Session)과 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "exercise_id", nullable = false)
    private int exerciseId;

    @Column(name = "set_count")
    private Integer setCount;

    @Column(name = "rep_count")
    private Integer repCount;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "duration")
    private Integer duration; // 예: 운동 시간(초 또는 분)

    @Column(name = "distance")
    private Integer distance; // 예: 미터 단위

    /**
     * 전달된 값이 null이 아니면 해당 필드를 업데이트하는 도메인 메서드.
     */
    public void updateFields(Integer newSetCount, Integer newRepCount, Integer newWeight,
                             Integer newDuration, Integer newDistance) {
        if (newSetCount != null) {
            this.setCount = newSetCount;
        }
        if (newRepCount != null) {
            this.repCount = newRepCount;
        }
        if (newWeight != null) {
            this.weight = newWeight;
        }
        if (newDuration != null) {
            this.duration = newDuration;
        }
        if (newDistance != null) {
            this.distance = newDistance;
        }
    }
}

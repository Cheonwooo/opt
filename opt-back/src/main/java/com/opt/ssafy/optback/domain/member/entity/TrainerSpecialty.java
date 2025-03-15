package com.opt.ssafy.optback.domain.member.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "trainer_specialty")
public class TrainerSpecialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // member 테이블의 id를 참조하는 외래 키
    @Column(name = "trainer_id", nullable = false)
    private Integer trainerId;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    protected TrainerSpecialty() {
    }

    public TrainerSpecialty(Integer trainerId, String keyword) {
        this.trainerId = trainerId;
        this.keyword = keyword;
    }


    public void setTrainerId(Integer trainerId) {
        this.trainerId = trainerId;
    }

}

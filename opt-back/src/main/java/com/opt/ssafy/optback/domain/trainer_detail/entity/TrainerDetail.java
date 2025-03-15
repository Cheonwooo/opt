package com.opt.ssafy.optback.domain.trainer_detail.entity;

import com.opt.ssafy.optback.domain.certificate.entity.Certificate;
import com.opt.ssafy.optback.domain.gym.entity.Gym;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.trainer_review.entity.TrainerReview;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Builder
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trainer_detail")
public class TrainerDetail {

    @Id
    @Column(name = "trainer_id", nullable = false)
    private Integer trainerId;

    @Builder.Default
    @Column(name = "is_one_day_available", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isOneDayAvailable = false;

    @ManyToOne
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @Column(name = "intro", columnDefinition = "TEXT")
    private String intro;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "available_hours", length = 10)
    private String availableHours;

    @OneToOne
    @JoinColumn(name = "trainer_id", referencedColumnName = "id")
    private Member member;

    @OneToMany(mappedBy = "trainerDetail", fetch = FetchType.LAZY)
    private List<TrainerReview> reviews;

    @OneToMany(mappedBy = "trainerDetail", fetch = FetchType.LAZY)
    private List<Certificate> certificates;

    public void updateIntro(String intro) {
        this.intro = intro;
    }

}

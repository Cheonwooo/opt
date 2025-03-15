package com.opt.ssafy.optback.domain.exercise.entity;

import com.opt.ssafy.optback.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Builder
@Getter
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "exercise_record")
public class ExerciseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @Column(name = "created_at", columnDefinition = "DATE DEFAULT CURRENT_DATE")
    private LocalDate createdAt;

    @Column(name = "`set`")
    private Integer sets;

    @Column(name = "rep")
    private Integer rep;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "distance")
    private Integer distance;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "exerciseRecord", orphanRemoval = true)
    private List<ExerciseRecordMedia> medias;

    @PrePersist // ✅ 최초 저장 시 자동으로 실행됨
    public void prePersist() {
        this.createdAt = (this.createdAt == null) ? LocalDate.now() : this.createdAt;
    }

    public void update(Integer sets, Integer rep, Integer weight) {
        this.sets = sets;
        this.rep = rep;
        this.weight = weight;
    }

}

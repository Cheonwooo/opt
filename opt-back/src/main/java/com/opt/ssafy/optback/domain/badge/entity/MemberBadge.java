package com.opt.ssafy.optback.domain.badge.entity;

import com.opt.ssafy.optback.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member_badge")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "created_date", nullable = false)
    private LocalDate createDate;

    public static MemberBadge create(Member member, Badge badge) {
        return MemberBadge.builder()
                .member(member)
                .badge(badge)
                .createDate(LocalDate.now())
                .build();
    }
}

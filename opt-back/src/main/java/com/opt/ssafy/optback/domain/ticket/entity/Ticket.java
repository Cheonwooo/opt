package com.opt.ssafy.optback.domain.ticket.entity;

import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.session.entity.Session;
import com.opt.ssafy.optback.domain.ticket.dto.UpdateTicketRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Member trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Member student;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "total_sessions", nullable = false)
    private int totalSessions;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "last_used_date")
    private LocalDate lastUsedDate;

    @Column(name = "used_sessions")
    private int usedSessions;

    @Column(name = "status", nullable = false)
    private String status;

    @OneToMany(mappedBy = "ticket")
    private List<Session> sessions = new ArrayList<>();

    public void setNewRecord(UpdateTicketRequest request) {
        this.price = request.getPrice();
        this.totalSessions = request.getTotalSessions();
        this.startDate = request.getStartDate();
        this.status = request.getStatus();
    }

    public void setLastUsedDate(LocalDate lastUsedDate) {
        this.lastUsedDate = LocalDate.now();
    }

    public void setUsedSessions(int usedSessions) {
        this.usedSessions = usedSessions;
    }
}

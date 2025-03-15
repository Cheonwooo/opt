package com.opt.ssafy.optback.domain.ticket.repository;

import com.opt.ssafy.optback.domain.ticket.entity.Ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByStudentIdAndLastUsedDateIsNull(int id);

    List<Ticket> findByStudentIdAndLastUsedDateIsNotNull(int id);

    List<Ticket> findByTrainerIdAndLastUsedDateIsNull(int id);

    List<Ticket> findByTrainerIdAndLastUsedDateIsNotNull(int id);

}

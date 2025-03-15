package com.opt.ssafy.optback.domain.ticket.aspect;

import com.opt.ssafy.optback.domain.session.dto.SessionResponse;
import com.opt.ssafy.optback.domain.ticket.exception.TicketNotSaveException;
import com.opt.ssafy.optback.domain.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TicketUseAspect {

    private final TicketService ticketService;

    @AfterReturning(
            value = "execution(* com.opt.ssafy.optback.domain.session.application.SessionService.memberCheckSession(..)) || "
                    +
                    "execution(* com.opt.ssafy.optback.domain.session.application.SessionService.trainerCheckSession(..))",
            returning = "response")
    public void afterCheckSession(final JoinPoint joinPoint, Object response) {
        if (response instanceof SessionResponse) {
            SessionResponse sessionResponse = (SessionResponse) response;
            ticketService.updateUsed(sessionResponse);
        } else {
            throw new TicketNotSaveException("반환 세션 타입이 잘못되었습니다");
        }
    }
}

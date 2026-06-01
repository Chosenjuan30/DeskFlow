package com.deskflow.module.ticket.service;

import com.deskflow.module.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReferenceNumberGenerator {

    private final TicketRepository ticketRepository;

    // REQUIRES_NEW so the sequence call commits independently of the outer transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generate() {
        int year = LocalDate.now().getYear();
        long seq  = ticketRepository.nextRefSeq();
        return String.format("TKT-%d%05d", year, seq);
    }
}

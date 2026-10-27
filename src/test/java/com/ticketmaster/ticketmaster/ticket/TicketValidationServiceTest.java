
package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.event.Event;
import com.ticketmaster.ticketmaster.ticket.dto.TicketValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketValidationServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    private TicketValidationService service;

    @BeforeEach
    void setUp() {
        service = new TicketValidationService(ticketRepository);
    }

    private Ticket createTicket(TicketStatus status) {
    Ticket ticket = new Ticket();
    ticket.setOwnerId(7L);
    ticket.setTicketCode("TICKET-ABC");
    ticket.setStatus(status);
    ticket.setEvent(mock(Event.class));

    return ticket;
}

    @Test
    void validatesValidTicketAndMarksItUsed() {
        Ticket ticket = createTicket(TicketStatus.VALID);
        when(ticket.getEvent().getTitle()).thenReturn("Test Event");

        when(ticketRepository.findByTicketCodeForUpdate("TICKET-ABC"))
                .thenReturn(Optional.of(ticket));

        TicketValidationResponse response =
                service.validateTicket("TICKET-ABC");

        assertEquals(TicketStatus.USED, ticket.getStatus());
        assertNotNull(ticket.getUsedAt());
        assertEquals("TICKET-ABC", response.ticketCode());
        assertEquals("Test Event", response.eventTitle());
        assertEquals(TicketStatus.USED, response.status());
        assertEquals("Ticket validated successfully.", response.message());
    }

    @Test
    void rejectsUnknownTicket() {
        when(ticketRepository.findByTicketCodeForUpdate("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.validateTicket("UNKNOWN"));
    }

    @Test
    void rejectsAlreadyUsedTicket() {
        Ticket ticket = createTicket(TicketStatus.USED);

        when(ticketRepository.findByTicketCodeForUpdate("TICKET-ABC"))
                .thenReturn(Optional.of(ticket));

        assertThrows(IllegalStateException.class,
                () -> service.validateTicket("TICKET-ABC"));
    }

    @Test
    void rejectsCancelledTicket() {
        Ticket ticket = createTicket(TicketStatus.CANCELLED);

        when(ticketRepository.findByTicketCodeForUpdate("TICKET-ABC"))
                .thenReturn(Optional.of(ticket));

        assertThrows(IllegalStateException.class,
                () -> service.validateTicket("TICKET-ABC"));
    }

    @Test
    void rejectsBlankTicketCode() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validateTicket("   "));

        verifyNoInteractions(ticketRepository);
    }
}

package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.event.Event;
import com.ticketmaster.ticketmaster.event.TicketType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketIssuingServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketIssuingService ticketIssuingService;

    @Test
    void issuesOneTicketForEachPurchasedUnit() {
        Event event = mock(Event.class);
        TicketType ticketType = mock(TicketType.class);

        when(ticketRepository.existsByOrderId(10L)).thenReturn(false);
        when(ticketRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Ticket> result = ticketIssuingService.issueTickets(
                10L, 20L, "PAID",
                List.of(new TicketIssuingService.TicketRequest(
                        event, ticketType, 3))
        );

        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(
                t -> t.getStatus() == TicketStatus.VALID));
        assertTrue(result.stream().allMatch(
                t -> t.getOrderId().equals(10L)));
        assertTrue(result.stream().allMatch(
                t -> t.getOwnerId().equals(20L)));
        assertTrue(result.stream().allMatch(
                t -> t.getEvent() == event));
        assertTrue(result.stream().allMatch(
                t -> t.getTicketType() == ticketType));
        assertTrue(result.stream().allMatch(
                t -> t.getIssuedAt() != null));
        assertTrue(result.stream().allMatch(t -> {
            try {
                UUID.fromString(t.getTicketCode());
                return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }));
        assertEquals(3, result.stream()
                .map(Ticket::getTicketCode)
                .distinct()
                .count());

        verify(ticketRepository).saveAll(anyList());
    }

    @Test
    void doesNotIssueDuplicateTicketsForAnAlreadyProcessedOrder() {
        Ticket existingTicket = mock(Ticket.class);

        when(ticketRepository.existsByOrderId(10L)).thenReturn(true);
        when(ticketRepository.findAllByOrderId(10L))
                .thenReturn(List.of(existingTicket));

        List<Ticket> result = ticketIssuingService.issueTickets(
                10L, 20L, "PAID",
                List.of(new TicketIssuingService.TicketRequest(
                        mock(Event.class), mock(TicketType.class), 3))
        );

        assertEquals(1, result.size());
        assertSame(existingTicket, result.get(0));
        verify(ticketRepository, never()).saveAll(anyList());
    }

    @Test
    void rejectsAnUnpaidOrder() {
        assertThrows(IllegalStateException.class,
                () -> ticketIssuingService.issueTickets(
                        10L, 20L, "PENDING",
                        List.of(new TicketIssuingService.TicketRequest(
                                mock(Event.class), mock(TicketType.class), 1))
                ));

        verify(ticketRepository, never()).saveAll(anyList());
    }

    @Test
    void rejectsZeroOrNegativeQuantities() {
        when(ticketRepository.existsByOrderId(10L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> ticketIssuingService.issueTickets(
                        10L, 20L, "PAID",
                        List.of(new TicketIssuingService.TicketRequest(
                                mock(Event.class), mock(TicketType.class), 0))
                ));

        verify(ticketRepository, never()).saveAll(anyList());
    }
}

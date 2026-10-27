package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.ticket.dto.TicketResponse;
import com.ticketmaster.ticketmaster.user.entity.User;
import com.ticketmaster.ticketmaster.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketQueryService ticketQueryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TicketController ticketController;

    @Test
    void getMyTicketsUsesAuthenticatedUsersDatabaseId() {
        User user = User.builder()
                .id(7L)
                .email("customer@example.com")
                .password("encoded-password")
                .role(User.Role.CUSTOMER)
                .build();

        when(authentication.getName()).thenReturn("customer@example.com");
        when(userRepository.findByEmail("customer@example.com"))
                .thenReturn(Optional.of(user));
        when(ticketQueryService.getMyTickets(7L)).thenReturn(List.of());

        List<TicketResponse> result = ticketController.getMyTickets(authentication);

        assertTrue(result.isEmpty());
        verify(ticketQueryService).getMyTickets(7L);
    }

    @Test
    void getTicketByIdUsesAuthenticatedUsersDatabaseId() {
        User user = User.builder()
                .id(7L)
                .email("customer@example.com")
                .password("encoded-password")
                .role(User.Role.CUSTOMER)
                .build();

        TicketResponse response = new TicketResponse(
                10L, "TICKET-ABC", "Music Festival", "VIP",
                TicketStatus.VALID, null, null
        );

        when(authentication.getName()).thenReturn("customer@example.com");
        when(userRepository.findByEmail("customer@example.com"))
                .thenReturn(Optional.of(user));
        when(ticketQueryService.getMyTicketById(10L, 7L))
                .thenReturn(response);

        TicketResponse result =
                ticketController.getTicketById(10L, authentication);

        assertEquals("TICKET-ABC", result.ticketCode());
        assertEquals("Music Festival", result.eventTitle());
        verify(ticketQueryService).getMyTicketById(10L, 7L);
    }

    @Test
    void getMyTicketsFailsWhenAuthenticatedUserCannotBeFound() {
        when(authentication.getName()).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> ticketController.getMyTickets(authentication)
        );

        verifyNoInteractions(ticketQueryService);
    }

    @Test
    void getTicketByIdPropagatesForbiddenForAnotherCustomersTicket() {
        User user = User.builder()
                .id(7L)
                .email("customer@example.com")
                .password("encoded-password")
                .role(User.Role.CUSTOMER)
                .build();

        when(authentication.getName()).thenReturn("customer@example.com");
        when(userRepository.findByEmail("customer@example.com"))
                .thenReturn(Optional.of(user));
        when(ticketQueryService.getMyTicketById(10L, 7L))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN
                ));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ticketController.getTicketById(10L, authentication)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    
@Test
void getTicketQrCodeReturnsPngUsingTicketCode() {
    User user = User.builder()
            .id(7L)
            .email("customer@example.com")
            .password("encoded-password")
            .role(User.Role.CUSTOMER)
            .build();

    Ticket ticket = new Ticket();
    ticket.setOwnerId(7L);
    ticket.setTicketCode("TICKET-ABC");

    byte[] expectedPng = new byte[]{(byte) 137, 80, 78, 71};

    when(ticketRepository.findById(10L))
            .thenReturn(Optional.of(ticket));
    when(authentication.getName())
            .thenReturn("customer@example.com");
    when(userRepository.findByEmail("customer@example.com"))
            .thenReturn(Optional.of(user));
    when(qrCodeService.generateQrCode("TICKET-ABC"))
            .thenReturn(expectedPng);

    var response = ticketController.getTicketQrCode(10L, authentication);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(
            org.springframework.http.MediaType.IMAGE_PNG,
            response.getHeaders().getContentType());
    assertArrayEquals(expectedPng, response.getBody());

    verify(qrCodeService).generateQrCode("TICKET-ABC");
}

@Test
void getTicketQrCodeReturnsNotFoundForMissingTicket() {
    when(ticketRepository.findById(999L))
            .thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> ticketController.getTicketQrCode(999L, authentication));

    assertEquals(404, exception.getStatusCode().value());
    verifyNoInteractions(qrCodeService);
}

@Test
void getTicketQrCodeRejectsAnotherCustomersTicket() {
    Ticket ticket = new Ticket();
    ticket.setOwnerId(7L);
    ticket.setTicketCode("TICKET-ABC");

    User otherCustomer = User.builder()
            .id(8L)
            .email("other@example.com")
            .password("encoded-password")
            .role(User.Role.CUSTOMER)
            .build();

    when(ticketRepository.findById(10L))
            .thenReturn(Optional.of(ticket));
    when(authentication.getName())
            .thenReturn("other@example.com");
    when(userRepository.findByEmail("other@example.com"))
            .thenReturn(Optional.of(otherCustomer));

    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> ticketController.getTicketQrCode(10L, authentication));

    assertEquals(403, exception.getStatusCode().value());
    verifyNoInteractions(qrCodeService);
}

    

}

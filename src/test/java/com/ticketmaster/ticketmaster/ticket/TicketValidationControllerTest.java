package com.ticketmaster.ticketmaster.ticket;

import com.ticketmaster.ticketmaster.ticket.dto.TicketValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TicketValidationControllerTest {

    @Mock
    private TicketValidationService ticketValidationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new TicketValidationController(ticketValidationService))
                .setValidator(validator)
                .build();
    }

    @Test
    void validatesTicketAndReturnsSuccessResponse() throws Exception {
        when(ticketValidationService.validateTicket("TICKET-ABC"))
                .thenReturn(new TicketValidationResponse(
                        1L,
                        "TICKET-ABC",
                        "Test Event",
                        TicketStatus.USED,
                        LocalDateTime.of(2026, 10, 26, 12, 0),
                        "Ticket validated successfully."
                ));

        mockMvc.perform(post("/api/tickets/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketCode\":\"TICKET-ABC\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(1))
                .andExpect(jsonPath("$.ticketCode").value("TICKET-ABC"))
                .andExpect(jsonPath("$.eventTitle").value("Test Event"))
                .andExpect(jsonPath("$.status").value("USED"))
                .andExpect(jsonPath("$.message")
                        .value("Ticket validated successfully."));

        verify(ticketValidationService)
                .validateTicket("TICKET-ABC");
    }

    @Test
    void rejectsBlankTicketCodeBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/tickets/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketCode\":\" \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ticketValidationService);
    }
}

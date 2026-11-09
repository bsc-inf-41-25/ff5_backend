package com.ticketmaster.ticketmaster.payment;

import com.ticketmaster.ticketmaster.order.entity.Order;
import com.ticketmaster.ticketmaster.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "PayChangu checkout and webhook endpoints")
public class PaymentController {

    private final OrderService orderService;
    private final PayChanguService payChanguService;

    @Value("${paychangu.demo-mode:true}")
    private boolean demoMode;

    public PaymentController(OrderService orderService, PayChanguService payChanguService) {
        this.orderService = orderService;
        this.payChanguService = payChanguService;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Initiate a PayChangu checkout")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> request) {
        Double amount = Double.valueOf(request.get("amount").toString());
        Long orderId = Long.valueOf(request.get("orderId").toString());
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        String checkoutUrl = payChanguService.initiateCheckout(amount, email, orderId);
        return ResponseEntity.ok(Map.of("checkoutUrl", checkoutUrl));
    }

    @GetMapping("/callback")
    public void callback(@RequestParam("tx_ref") String txRef, HttpServletResponse response) throws IOException {
        response.sendRedirect("myapp://payment-result?status=success&tx_ref=" + txRef);
    }

    @GetMapping("/return")
    public void returnUrl(@RequestParam(value = "tx_ref", required = false) String txRef,
                          @RequestParam(value = "status", defaultValue = "failed") String status,
                          HttpServletResponse response) throws IOException {
        response.sendRedirect("myapp://payment-result?status=" + status + "&tx_ref=" + txRef);
    }

    @PostMapping("/demo-confirm/{orderId}")
    @Operation(summary = "Demo: Manually confirm payment and issue tickets (requires payment.demo-mode=true)")
    public ResponseEntity<?> demoConfirm(@PathVariable Long orderId) {
        if (!demoMode) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Demo mode is disabled.");
        }
        try {
            orderService.updateOrderStatus(orderId, Order.OrderStatus.PAID);
            return ResponseEntity.ok(Map.of("message", "Payment confirmed and tickets issued successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    @Operation(summary = "PayChangu webhook handler")
    public ResponseEntity<?> webhook(@RequestBody Map<String, Object> payload) {
        System.out.println("Received Webhook Payload: " + payload);
        try {
            String status = (String) payload.get("status");
            Map<String, Object> meta = (Map<String, Object>) payload.get("meta");

            if ("success".equalsIgnoreCase(status) && meta != null && meta.containsKey("order_id")) {
                Long orderId = Long.valueOf(meta.get("order_id").toString());
                orderService.updateOrderStatus(orderId, Order.OrderStatus.PAID);
            }
            return ResponseEntity.ok(Map.of("status", "received"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error_handled")); 
            // Return 200 so PayChangu doesn't retry infinitely
        }
    }
}
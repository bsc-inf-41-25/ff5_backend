package com.ticketmaster.ticketmaster.report;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class SalesReportController {

    private final SalesReportService salesReportService;

    public SalesReportController(
            SalesReportService salesReportService) {
        this.salesReportService = salesReportService;
    }

    @GetMapping("/events/{eventId}/sales")
    public List<SalesReportItem> getSalesReport(
            @PathVariable Long eventId) {
        return salesReportService.getSalesReport(eventId);
    }
}

package com.ticketmaster.ticketmaster.report;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class AttendanceReportController {

    private final AttendanceReportService attendanceReportService;

    public AttendanceReportController(
            AttendanceReportService attendanceReportService) {
        this.attendanceReportService = attendanceReportService;
    }

    @GetMapping("/events/{eventId}/attendance")
    public AttendanceReportResponse getAttendanceReport(
            @PathVariable Long eventId) {
        return attendanceReportService.getAttendanceReport(eventId);
    }
}

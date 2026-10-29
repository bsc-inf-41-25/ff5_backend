
package com.ticketmaster.ticketmaster.report;

public record AttendanceReportResponse(
        Long eventId,
        long ticketsSold,
        long peopleCheckedIn,
        long peopleNotCheckedIn
) {
    public AttendanceReportResponse(
            Long eventId,
            long ticketsSold,
            long peopleCheckedIn
    ) {
        this(
                eventId,
                ticketsSold,
                peopleCheckedIn,
                ticketsSold - peopleCheckedIn
        );
    }
}
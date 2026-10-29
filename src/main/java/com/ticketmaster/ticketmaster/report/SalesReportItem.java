package com.ticketmaster.ticketmaster.report;

import java.math.BigDecimal;

public record SalesReportItem(
        Long ticketTypeId,
        int ticketsSold,
        BigDecimal revenue
) {}

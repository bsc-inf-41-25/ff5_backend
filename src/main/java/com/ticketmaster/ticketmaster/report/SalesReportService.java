package com.ticketmaster.ticketmaster.report;

import com.ticketmaster.ticketmaster.order.entity.Order;
import com.ticketmaster.ticketmaster.order.entity.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesReportService {

    private final SalesReportRepository salesReportRepository;

    public SalesReportService(
            SalesReportRepository salesReportRepository) {
        this.salesReportRepository = salesReportRepository;
    }

    @Transactional(readOnly = true)
    public List<SalesReportItem> getSalesReport(Long eventId) {

        if (eventId == null || eventId <= 0) {
            throw new IllegalArgumentException(
                    "A valid event ID is required"
            );
        }

        List<Order> paidOrders =
                salesReportRepository.findByEventIdAndStatus(
                        eventId,
                        Order.OrderStatus.PAID
                );

        Map<Long, Integer> ticketsSold = new HashMap<>();
        Map<Long, BigDecimal> revenueByTicketType = new HashMap<>();

        for (Order order : paidOrders) {
            for (OrderItem item : order.getItems()) {

                Long ticketTypeId = item.getTicketTypeId();

                int quantity = item.getQuantity();

                BigDecimal itemRevenue = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(quantity));

                ticketsSold.merge(
                        ticketTypeId,
                        quantity,
                        Integer::sum
                );

                revenueByTicketType.merge(
                        ticketTypeId,
                        itemRevenue,
                        BigDecimal::add
                );
            }
        }

        List<SalesReportItem> report = new ArrayList<>();

        for (Long ticketTypeId : ticketsSold.keySet()) {
            report.add(
                    new SalesReportItem(
                            ticketTypeId,
                            ticketsSold.get(ticketTypeId),
                            revenueByTicketType.get(ticketTypeId)
                    )
            );
        }

        report.sort(
                Comparator.comparing(SalesReportItem::ticketTypeId)
        );

        return report;
    }

    public BigDecimal getTotalRevenue(List<SalesReportItem> items) {

        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(SalesReportItem::revenue)
                .filter(revenue -> revenue != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
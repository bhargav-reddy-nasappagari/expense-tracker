package com.expensetracker.util;

import com.expensetracker.dto.MonthlyTrend;
import com.expensetracker.dto.TrendPoint;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartDataBuilder {

    // Helper to build JSON for Line Charts
    public static String buildLineChartData(List<TrendPoint> trendData) {
        if (trendData == null || trendData.isEmpty()) return "{}";

        String labels = trendData.stream()
                .map(t -> "\"" + t.getLabel() + "\"")
                .collect(Collectors.joining(", ", "[", "]"));

        String data = trendData.stream()
                .map(t -> t.getAmount().toString())
                .collect(Collectors.joining(", ", "[", "]"));

        // Manual JSON construction to avoid external dependencies
        return String.format(
            "{" +
            "  \"labels\": %s," +
            "  \"datasets\": [{" +
            "    \"label\": \"Spending\"," +
            "    \"data\": %s," +
            "    \"borderColor\": \"#4CAF50\"," +
            "    \"tension\": 0.3," +
            "    \"fill\": true" +
            "  }]" +
            "}", labels, data);
    }

    // Helper to build JSON for Pie/Doughnut Charts
    public static String buildPieChartData(Map<String, BigDecimal> categoryTotals) {
        if (categoryTotals == null || categoryTotals.isEmpty()) return "{}";

        String labels = categoryTotals.keySet().stream()
                .map(k -> "\"" + k + "\"")
                .collect(Collectors.joining(", ", "[", "]"));

        String data = categoryTotals.values().stream()
                .map(BigDecimal::toString)
                .collect(Collectors.joining(", ", "[", "]"));

        // Basic color palette
        String colors = "[\"#FF6384\", \"#36A2EB\", \"#FFCE56\", \"#4BC0C0\", \"#9966FF\", \"#FF9F40\"]";

        return String.format(
            "{" +
            "  \"labels\": %s," +
            "  \"datasets\": [{" +
            "    \"data\": %s," +
            "    \"backgroundColor\": %s" +
            "  }]" +
            "}", labels, data, colors);
    }
}
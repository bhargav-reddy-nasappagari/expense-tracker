<%
    // Prevent caching
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    
    // Session validation
    if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reports & Analytics • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container">
        
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1>Spending Reports</h1>
                <p class="text-muted">Analyze your financial habits for 
                    <c:choose>
                        <c:when test="${not empty reportData.startDate}">
                            <strong><fmt:parseDate value="${reportData.startDate}" pattern="yyyy-MM-dd" var="sDate" type="date"/>
                            <fmt:formatDate value="${sDate}" pattern="MMM d"/></strong> to 
                            <strong><fmt:parseDate value="${reportData.endDate}" pattern="yyyy-MM-dd" var="eDate" type="date"/>
                            <fmt:formatDate value="${eDate}" pattern="MMM d, yyyy"/></strong>
                        </c:when>
                        <c:otherwise>the selected period</c:otherwise>
                    </c:choose>
                </p>
            </div>
                <div class="d-flex gap-2 mb-4">
                    <a href="${pageContext.request.contextPath}/reports/heatmap" class="btn-olive-link">
                        <i class="bi bi-grid-3x3-gap me-1"></i> Heatmap
                    </a>
                    <a href="${pageContext.request.contextPath}/reports/trends" class="btn-olive-link">
                        <i class="bi bi-graph-up me-1"></i> Trends
                    </a>
                    <a href="${pageContext.request.contextPath}/reports/categories" class="btn-olive-link">
                        <i class="bi bi-pie-chart me-1"></i> Categories
                    </a>
                </div>
        </div>

        <div class="filter-item">
            <div class="command-bar-wrapper mb-4 shadow-sm">
                <form action="${pageContext.request.contextPath}/reports" method="get" class="command-bar">
                    
                    <div class="filter-item">
                        <span class="filter-label">Report Type</span>
                        <div class="filter-input-wrapper">
                            <i class="bi bi-graph-up-arrow"></i>
                            <select name="reportType" id="reportTypeSelect" class="clean-select">
                                <option value="THIS_WEEK" ${selectedReportType == 'THIS_WEEK' ? 'selected' : ''}>This Week</option>
                                <option value="THIS_MONTH" ${selectedReportType == 'THIS_MONTH' ? 'selected' : ''}>This Month</option>
                                <option value="LAST_MONTH" ${selectedReportType == 'LAST_MONTH' ? 'selected' : ''}>Last Month</option>
                                <option value="LAST_3_MONTHS" ${selectedReportType == 'LAST_3_MONTHS' ? 'selected' : ''}>Last 3 Months</option>
                                <option value="LAST_6_MONTHS" ${selectedReportType == 'LAST_6_MONTHS' ? 'selected' : ''}>Last 6 Months</option>
                                <option value="THIS_YEAR" ${selectedReportType == 'THIS_YEAR' ? 'selected' : ''}>This Year</option>
                                <option value="LAST_YEAR" ${selectedReportType == 'LAST_YEAR' ? 'selected' : ''}>Last Year</option>
                                <option value="CUSTOM" ${selectedReportType == 'CUSTOM' ? 'selected' : ''}>Custom Range</option>
                            </select>
                        </div>
                    </div>

                    <div id="customDateContainer" class="d-flex gap-5" style="display: none !important;">
                        <div class="filter-item" style="padding-right: 10px;">
                            <span class="filter-label">Start Date</span>
                            <div class="filter-input-wrapper">
                                <i class="bi bi-calendar-plus"></i>
                                <input type="date" name="startDate" class="clean-input" value="${startDate}">
                            </div>
                        </div>

                        <div class="filter-item">
                            <span class="filter-label">End Date</span>
                            <div class="filter-input-wrapper">
                                <i class="bi bi-calendar-check"></i>
                                <input type="date" name="endDate" class="clean-input" value="${endDate}">
                            </div>
                        </div>
                    </div>

                    <div class="filter-item flex-grow-1">
                        <span class="filter-label">Filter by Category</span>
                        <div class="filter-input-wrapper">
                            <i class="bi bi-tags"></i>
                            <select name="categoryId" class="clean-select">
                                <option value="">All Categories</option>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.id}" ${cat.id == selectedCategoryId ? 'selected' : ''}>${cat.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="filter-actions d-flex gap-2">
                        <button type="submit" class="btn-apply px-4">
                            <i class="bi bi-lightning-charge-fill me-2"></i>Generate
                        </button>
                        
                        <a href="${pageContext.request.contextPath}/reports" class="btn-reset-glass" title="Clear Filters">
                            <i class="bi bi-arrow-counterclockwise"></i>
                        </a>
                    </div>
                </form>
            </div>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger mb-4">${errorMessage}</div>
        </c:if>

        <div class="metrics-grid">
            <div class="metric-card">
                <span class="metric-title">Total Spending</span>
                <span class="metric-value">₹<fmt:formatNumber value="${reportData.totalSpending}" type="number" minFractionDigits="2"/></span>
                <span class="metric-sub text-muted">In ${reportData.transactionCount} transactions</span>
            </div>
            
            <div class="metric-card">
                <span class="metric-title">Daily Average</span>
                <span class="metric-value">₹<fmt:formatNumber value="${reportData.averagePerDay}" type="number" minFractionDigits="0"/></span>
                <span class="metric-sub text-muted">Per day</span>
            </div>

            <div class="metric-card">
                <span class="metric-title">Comparison</span>
                <c:choose>
                    <c:when test="${not empty reportData.comparison and reportData.comparison.percentageChange != 0}">
                        <div class="metric-value ${reportData.comparison.absoluteChange > 0 ? 'trend-up' : 'trend-down'}">
                            <c:if test="${reportData.comparison.absoluteChange > 0}">+</c:if>
                            <fmt:formatNumber value="${reportData.comparison.percentageChange}" maxFractionDigits="1"/>%
                        </div>
                        <span class="metric-sub text-muted">vs previous period</span>
                    </c:when>
                    <c:otherwise>
                        <span class="metric-value text-muted">-</span>
                        <span class="metric-sub text-muted">No prior data</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="metric-card">
                <span class="metric-title">Top Category</span>
                <c:choose>
                    <c:when test="${not empty reportData.categoryBreakdown}">
                        <span class="metric-value" style="font-size: 1.4rem;">${reportData.categoryBreakdown[0].categoryName}</span>
                        <span class="metric-sub text-muted">
                            <fmt:formatNumber value="${reportData.categoryBreakdown[0].percentage}" maxFractionDigits="1"/>% of total
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span class="metric-value">-</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="row">
            <div class="col-md-5 mb-4">
                <div class="card h-100">
                    <div class="card-header">
                        <h3>Spending Insights</h3>
                    </div>
                    <div class="card-body">
                        <div class="insights-list">
                            <c:if test="${empty insights}">
                                <p class="text-muted text-center py-4">No specific insights available for this period.</p>
                            </c:if>
                            <c:forEach var="insight" items="${insights}">
                                <div class="insight-item">
                                    <div class="insight-icon insight-${insight.type}">
                                        <c:choose>
                                            <c:when test="${insight.type == 'warning'}">!</c:when>
                                            <c:when test="${insight.type == 'positive'}">✓</c:when>
                                            <c:when test="${insight.type == 'suggestion'}">💡</c:when>
                                            <c:otherwise>i</c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="insight-text">${insight.message}</div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-7 mb-4">
                <div class="card h-100">
                    <div class="card-header">
                        <h3>Category Breakdown</h3>
                    </div>
                    <div class="card-body d-flex align-items-center justify-content-center">
                        <div class="chart-wrapper-sm" style="max-width: 400px;">
                            <canvas id="categoryChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="card mb-4">
            <div class="card-header">
                <h3>Spending Trend</h3>
            </div>
            <div class="card-body">
                <div class="chart-wrapper">
                    <canvas id="trendChart"></canvas>
                </div>
            </div>
        </div>

        <div class="card mb-4">
            <div class="card-header">
                <h3>Top Expenses</h3>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Description</th>
                                <th>Category</th>
                                <th class="text-end">Amount</th>
                                <th class="text-end">% of Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="expense" items="${reportData.topExpenses}">
                                <tr>
                                    <td>${expense.date.month} ${expense.date.dayOfMonth}</td>
                                    <td>${expense.description}</td>
                                    <td><span class="badge bg-light text-dark">${expense.categoryName}</span></td>
                                    <td class="text-end font-weight-bold">₹<fmt:formatNumber value="${expense.amount}" minFractionDigits="2"/></td>
                                    <td class="text-end text-muted"><fmt:formatNumber value="${expense.percentageOfTotal}" maxFractionDigits="1"/>%</td>
                                </div>
                            </c:forEach>
                            <c:if test="${empty reportData.topExpenses}">
                                <tr><td colspan="5" class="text-center py-3">No expenses found for this period.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </div>
</div>

<script>
    // Prepare Data from JSP to JS
    // Trend Data
    const trendLabels = [
        <c:forEach var="point" items="${reportData.trendData}" varStatus="loop">
            '${point.label}'<c:if test="${!loop.last}">,</c:if>
        </c:forEach>
    ];
    const trendValues = [
        <c:forEach var="point" items="${reportData.trendData}" varStatus="loop">
            ${point.amount}<c:if test="${!loop.last}">,</c:if>
        </c:forEach>
    ];

    // Category Data (Limit to top 5 + Others for cleaner pie chart)
    const catLabels = [];
    const catValues = [];
    const catColors = ['#546236', '#747c92', '#a3b18a', '#dda15e', '#bc6c25', '#e9ecef'];
    
    <c:forEach var="cat" items="${reportData.categoryBreakdown}" varStatus="loop" end="5">
        catLabels.push('${cat.categoryName}');
        catValues.push(${cat.total});
    </c:forEach>

    // 1. Trend Line Chart
    const ctxTrend = document.getElementById('trendChart').getContext('2d');
    new Chart(ctxTrend, {
        type: 'line',
        data: {
            labels: trendLabels,
            datasets: [{
                label: 'Daily Spending (₹)',
                data: trendValues,
                borderColor: '#546236', // Olive leaf
                backgroundColor: 'rgba(84, 98, 54, 0.1)',
                tension: 0.3,
                fill: true,
                pointRadius: 3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: { beginAtZero: true, grid: { borderDash: [5, 5] } },
                x: { grid: { display: false } }
            }
        }
    });

    // 2. Category Pie Chart
    if (catValues.length > 0) {
        const ctxCat = document.getElementById('categoryChart').getContext('2d');
        new Chart(ctxCat, {
            type: 'doughnut',
            data: {
                labels: catLabels,
                datasets: [{
                    data: catValues,
                    backgroundColor: catColors,
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '65%',
                plugins: {
                    legend: { position: 'right', labels: { boxWidth: 12 } }
                }
            }
        });
    }

    document.addEventListener('DOMContentLoaded', function() {
        const reportTypeSelect = document.getElementById('reportTypeSelect');
        const customDateContainer = document.getElementById('customDateContainer');

        function toggleDateFields() {
            if (reportTypeSelect.value === 'CUSTOM') {
                customDateContainer.style.setProperty('display', 'flex', 'important');
            } else {
                customDateContainer.style.setProperty('display', 'none', 'important');
            }
        }

        // Run on page load (to handle cases where CUSTOM is already selected)
        toggleDateFields();

        // Run on change
        reportTypeSelect.addEventListener('change', toggleDateFields);
    });
</script>

</body>
</html>
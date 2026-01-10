<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Spending Trends • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1>Spending Trends</h1>
                <p class="text-muted">Analyze your financial trajectory over time.</p>
            </div>
            <div class="d-flex gap-2">
                 <a href="${pageContext.request.contextPath}/reports" class="btn-olive-link btn-sm">Back to Reports</a>
            </div>
        </div>

        <div class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h3>Monthly History</h3>
                <form action="" method="GET" class="d-flex align-items-center">
                    <label class="mr-2 text-muted small">Show last:</label>
                    <select name="months" class="form-control form-control-sm" style="width: auto;" onchange="this.form.submit()">
                        <option value="6" ${months == 6 ? 'selected' : ''}>6 Months</option>
                        <option value="12" ${months == 12 ? 'selected' : ''}>12 Months</option>
                        <option value="24" ${months == 24 ? 'selected' : ''}>24 Months</option>
                    </select>
                </form>
            </div>
            <div class="card-body">
                <div class="chart-wrapper">
                    <canvas id="monthlyTrendChart"></canvas>
                </div>
            </div>
        </div>

        <div class="row row-cols-1 row-cols-md-3 g-4 mb-4">
            <div class="col">
                <div class="card h-100 border-0 shadow-sm" style="border-radius: 12px; min-height: 160px;">
                    <div class="card-header bg-white border-0 pt-3 pb-0">
                        <h6 class="text-muted text-uppercase font-weight-bold mb-0" style="font-size: 0.75rem; letter-spacing: 0.5px;">Trend Direction</h6>
                    </div>
                    <div class="card-body d-flex flex-column align-items-center justify-content-center text-center">
                        <c:choose>
                            <c:when test="${trendDirection == 'increasing'}">
                                <div class="h3 text-danger mb-2" style="font-weight: 800; text-align: center; display: block; width: 100%;">↗ Increasing</div>
                                <p class="text-muted small mb-0" style="display: block; width: 100%;">Spending is trending upwards recently.</p>
                            </c:when>
                            <c:when test="${trendDirection == 'decreasing'}">
                                <div class="h3 text-success mb-2" style="font-weight: 800; text-align: center; display: block; width: 100%;">↘ Decreasing</div>
                                <p class="text-muted small mb-0" style="display: block; width: 100%;">Good job! Spending is trending downwards.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="h3 text-muted mb-2" style="font-weight: 800; text-align: center; display: block; width: 100%;">→ Stable</div>
                                <p class="text-muted small mb-0" style="display: block; width: 100%;">Your spending habits are consistent.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card h-100 border-0 shadow-sm" style="border-radius: 12px; min-height: 160px;">
                    <div class="card-header bg-white border-0 pt-3 pb-0">
                        <h6 class="text-muted text-uppercase font-weight-bold mb-0" style="font-size: 0.75rem; letter-spacing: 0.5px;">Highest Month</h6>
                    </div>
                    <div class="card-body d-flex flex-column align-items-center justify-content-center text-center">
                        <c:if test="${not empty highestMonth}">
                            <div class="h4 text-dark mb-0">
                                <span class="text-muted fw-normal">${highestMonth.month}</span> 
                                <span class="mx-1 text-light">|</span> 
                                <strong class="text-dark" style="font-size: 1.3rem; font-weight: 700;">₹<fmt:formatNumber value="${highestMonth.total}" maxFractionDigits="0"/></strong>
                            </div>
                        </c:if>
                        <c:if test="${empty highestMonth}">
                            <div class="h3 text-muted mb-0">-</div>
                        </c:if>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card h-100 border-0 shadow-sm" style="border-radius: 12px; min-height: 160px;">
                    <div class="card-header bg-white border-0 pt-3 pb-0">
                        <h6 class="text-muted text-uppercase font-weight-bold mb-0" style="font-size: 0.75rem; text-align: center; letter-spacing: 0.5px;">Average Monthly</h6>
                    </div>
                    <div class="card-body d-flex flex-column align-items-center justify-content-center text-center">
                        <div class="h2 text-dark mb-2" style="font-weight: 700; display: block; width: 100%;">₹<fmt:formatNumber value="${averageSpending}" maxFractionDigits="0"/></div>
                        <p class="text-muted small mb-0" style="display: block; width: 100%;">Over the last ${months} months</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h3>Detailed Breakdown</h3>
            </div>
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Month</th>
                            <th class="text-end">Total Spent</th>
                            <th class="text-end">Change</th>
                            <th class="text-end">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="i" begin="0" end="${trendData.size() - 1}">
                            <c:set var="trend" value="${trendData[trendData.size() - 1 - i]}" />
                            <tr>
                                <td class="font-weight-bold">${trend.month}</td>
                                <td class="text-end">₹<fmt:formatNumber value="${trend.total}" minFractionDigits="2"/></td>
                                <td class="text-end">
                                    <c:choose>
                                        <c:when test="${not empty trend.changeAmount and trend.changeAmount != 0}">
                                            <span class="${trend.changeAmount > 0 ? 'text-danger' : 'text-success'}">
                                                <c:if test="${trend.changeAmount > 0}">+</c:if>
                                                ₹<fmt:formatNumber value="${trend.changeAmount}" maxFractionDigits="0"/>
                                                (<fmt:formatNumber value="${trend.changePercent}" maxFractionDigits="1"/>%)
                                            </span>
                                        </c:when>
                                        <c:otherwise><span class="text-muted">-</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <c:choose>
                                        <c:when test="${trend.total > averageSpending.multiply(1.2)}">
                                            <span class="badge badge-over">High</span>
                                        </c:when>
                                        <c:when test="${trend.total < averageSpending.multiply(0.8)}">
                                            <span class="badge badge-under">Low</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-near">Average</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

    </div>
</div>

<script>
    // Data passed directly from Servlet as JSON strings
    const ctx = document.getElementById('monthlyTrendChart').getContext('2d');
    
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: ${chartLabels}, // Injected JSON array: ['Jan 2024', 'Feb 2024'...]
            datasets: [{
                label: 'Total Spending',
                data: ${chartData},     // Injected JSON array: [1200, 1500...]
                borderColor: '#546236',
                backgroundColor: 'rgba(84, 98, 54, 0.1)',
                borderWidth: 3,
                tension: 0.3,
                fill: true,
                pointBackgroundColor: '#ffffff',
                pointBorderColor: '#546236',
                pointRadius: 5,
                pointHoverRadius: 7
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    callbacks: {
                        label: function(context) {
                            return ' ₹' + context.parsed.y.toLocaleString('en-IN');
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { borderDash: [5, 5] }
                },
                x: {
                    grid: { display: false }
                }
            }
        }
    });
</script>

</body>
</html>
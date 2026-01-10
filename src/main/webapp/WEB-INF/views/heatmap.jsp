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
    <title>Spending Heatmap • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container">
        
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1>Spending Heatmap</h1>
                <p class="text-muted">Visualize your daily spending intensity.</p>
            </div>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/reports" class="btn-olive-link btn-sm">Back to Reports</a>
            </div>
        </div>

        <div class="row g-4 mt-3">
            <div class="col-lg-8">
                <div class="heatmap-main-card p-4"> <div class="d-flex justify-content-between align-items-center mb-4">
                        <a href="?year=${prevYear}&month=${prevMonth}" class="btn-olive-link btn-sm">
                            &larr; Previous
                        </a>
                        <h3 class="m-0 fw-bold text-deep-blue">${monthName} ${currentYear}</h3>
                        <c:choose>
                            <c:when test="${disableNext}">
                                <button class="btn-olive-link btn-sm" disabled>Next &rarr;</button>
                            </c:when>
                            <c:otherwise>
                                <a href="?year=${nextYear}&month=${nextMonth}" class="btn-olive-link btn-sm">
                                    Next &rarr;
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="heatmap-header mb-2">
                        <div>Mon</div>
                        <div>Tue</div>
                        <div>Wed</div>
                        <div>Thu</div>
                        <div>Fri</div>
                        <div>Sat</div>
                        <div>Sun</div>
                    </div>

                    <div class="heatmap-grid">
                        <c:forEach var="entry" items="${heatmapData}" varStatus="status">
                            <c:if test="${status.first}">
                                <c:set var="firstDayOfWeek" value="${entry.key.dayOfWeek.value}" />
                            </c:if>
                            
                            <div class="heatmap-day day-${entry.value.colorLevel}" 
                                <c:if test="${status.first}">style="grid-column-start: ${firstDayOfWeek};"</c:if>
                                title="${entry.key.month} ${entry.key.dayOfMonth}: ₹<fmt:formatNumber value='${entry.value.amount}'/> (${entry.value.transactionCount} txns)">
                                
                                <div class="d-flex justify-content-between align-items-start">
                                    <span class="date-num">${entry.key.dayOfMonth}</span>
                                    <c:if test="${entry.value.transactionCount > 0}">
                                        <small style="font-size:0.6rem; opacity:0.8;">${entry.value.transactionCount}x</small>
                                    </c:if>
                                </div>
                                
                                <span class="day-amount">
                                    <c:if test="${entry.value.amount > 0}">
                                        ₹<fmt:formatNumber value="${entry.value.amount}" maxFractionDigits="0"/>
                                    </c:if>
                                </span>
                            </div>
                        </c:forEach>
                    </div>

                    <div class="mt-5 d-flex flex-wrap justify-content-center gap-3">
                        <div class="legend-item"><span class="legend-box day-none" style="border:1px solid #ddd"></span> <small class="text-muted">No Spend</small></div>
                        <div class="legend-item"><span class="legend-box day-low"></span> <small class="text-muted">Low</small></div>
                        <div class="legend-item"><span class="legend-box day-medium"></span> <small class="text-muted">Medium</small></div>
                        <div class="legend-item"><span class="legend-box day-high"></span> <small class="text-muted">High</small></div>
                        <div class="legend-item"><span class="legend-box day-very-high"></span> <small class="text-muted">Very High</small></div>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="summary-sidebar-card h-100"> <div class="card border-0 shadow-sm h-100">
                        <div class="card-header bg-transparent border-bottom-0 pt-4 px-4">
                            <h3 class="h5 fw-bold mb-0">Month Summary</h3>
                        </div>
                        
                        <div class="card-body">
                            <div class="mb-4 pb-3 border-bottom">
                                <span class="text-muted d-block mb-1 small text-uppercase fw-bold">Total Spending</span>
                                <span class="h2 text-dark font-weight-bold">₹<fmt:formatNumber value="${totalSpent}" minFractionDigits="2"/></span>
                            </div>
                            
                            <div class="mb-4">
                                <span class="text-muted d-block mb-1 small text-uppercase fw-bold">Active Days</span>
                                <div class="d-flex align-items-baseline">
                                    <span class="h4 mb-0 fw-bold">${activeDays}</span>
                                    <small class="text-muted ms-2">/ ${fn:length(heatmapData)} days logged</small>
                                </div>
                            </div>

                            <div class="mb-4">
                                <span class="text-muted d-block mb-1 small text-uppercase fw-bold">Total Transactions</span>
                                <span class="h4 mb-0 fw-bold">${totalTransactions}</span>
                                <small class="text-muted d-block mt-1">Total entries for this month</small>
                            </div>

                            <hr class="my-4">

                            <div class="mb-2">
                                <span class="text-muted d-block mb-1 small text-uppercase fw-bold">Highest Spending Day</span>
                                <c:choose>
                                    <c:when test="${highestDayAmount > 0}">
                                        <span class="h5 text-danger fw-bold">
                                            <fmt:parseDate value="${highestDayDate}" pattern="yyyy-MM-dd" var="hDate" type="date"/>
                                            <fmt:formatDate value="${hDate}" pattern="MMM d, yyyy"/>
                                        </span>
                                        <div class="font-weight-bold mt-1 h5">₹<fmt:formatNumber value="${highestDayAmount}"/></div>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted">-</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                            <div class="tip-box p-3 rounded mx-3 mb-4">
                                <div class="d-flex align-items-start">
                                    <i class="bi bi-info-circle-fill me-2 text-olive-leaf" style="font-size: 0.9rem; margin-top: 2px;"></i>
                                    <small class="text-muted">
                                        <strong class="text-dark">Tip:</strong> 
                                        Hover over any day to see specific transaction details and daily totals.
                                    </small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        </div>
    </div>
</div>

</body>
</html>
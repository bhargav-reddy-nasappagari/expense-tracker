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
    <title>Category Analysis • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1>Category Performance</h1>
                <p class="text-muted">Detailed breakdown of where your money goes.</p>
            </div>
            <div class="d-flex gap-2">
                 <a href="${pageContext.request.contextPath}/reports" class="btn-olive-link btn-sm">Back to Reports</a>
            </div>
        </div>

        <div class="card mb-4">
            <div class="card-body">
                <form action="" method="GET" class="command-bar d-flex flex-wrap gap-3 align-items-end">
                    
                    <div class="filter-item">
                        <span class="filter-label">Start Date</span>
                        <div class="filter-input-wrapper">
                            <i class="bi bi-calendar-range"></i>
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
                    
                    <div class="filter-item flex-grow-1">
                        <span class="filter-label">Sort By</span>
                        <div class="filter-input-wrapper">
                            <i class="bi bi-sort-down"></i>
                            <select name="sortBy" class="clean-select">
                                <option value="amount" ${sortBy == 'amount' ? 'selected' : ''}>Highest Spending</option>
                                <option value="name" ${sortBy == 'name' ? 'selected' : ''}>Name (A-Z)</option>
                                <option value="budget" ${sortBy == 'budget' ? 'selected' : ''}>Highest Budget Usage</option>
                                <option value="change" ${sortBy == 'change' ? 'selected' : ''}>Biggest Change</option>
                            </select>
                        </div>
                    </div>

                    <div class="filter-actions d-flex gap-2">
                        <button type="submit" class="btn-apply px-4">
                            <i class="bi bi-search me-2"></i>Analyze
                        </button>
                        
                        <a href="${pageContext.request.contextPath}/reports/categories" class="btn-reset-glass" title="Reset Filters">
                            <i class="bi bi-arrow-counterclockwise"></i>
                        </a>
                    </div>
                </form>
            </div>
        </div>

        <div class="metrics-grid mb-4">
            <div class="metric-card">
                <span class="metric-title">Active Categories</span>
                <span class="metric-value">${statistics.totalCategories}</span>
                <span class="metric-sub text-muted">With expenses this period</span>
            </div>

            <div class="metric-card" style="border-left-color: #dc3545;">
                <span class="metric-title">Over Budget</span>
                <span class="metric-value text-danger">${statistics.overBudgetCount}</span>
                <span class="metric-sub text-muted">Categories exceeded limits</span>
            </div>

            <div class="metric-card">
                <span class="metric-title">Avg. Category Spend</span>
                <span class="metric-value">₹<fmt:formatNumber value="${statistics.avgSpending}" maxFractionDigits="0"/></span>
            </div>

            <div class="metric-card" style="border-left-color: ${not empty statistics.mostIncreased ? '#dc3545' : '#28a745'};">
                <span class="metric-title">Biggest Mover</span>
                <c:choose>
                    <c:when test="${not empty statistics.mostIncreased}">
                        <span class="metric-value text-danger" style="font-size: 1.4rem;">${statistics.mostIncreased.categoryName}</span>
                        <span class="metric-sub text-danger">
                            +₹<fmt:formatNumber value="${statistics.mostIncreased.changeAmount}" maxFractionDigits="0"/>
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span class="metric-value text-muted">-</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h3>Detailed Analysis</h3>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th>Category</th>
                            <th class="text-end">Spent</th>
                            <th class="text-end" style="width: 25%;">Budget Status</th>
                            <th class="text-end">Previous</th>
                            <th class="text-end">Change</th>
                            <th class="text-end">% Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="cat" items="${categoryReport}">
                            <tr>
                                <td>
                                    <div class="font-weight-bold">${cat.categoryName}</div>
                                    <small class="text-muted">${cat.transactionCount} transactions</small>
                                </td>
                                
                                <td class="text-end">
                                    <div class="font-weight-bold">₹<fmt:formatNumber value="${cat.totalSpent}" minFractionDigits="2"/></div>
                                    <small class="text-muted">Avg ₹<fmt:formatNumber value="${cat.averageTransaction}" maxFractionDigits="0"/></small>
                                </td>
                                
                                <td class="text-end">
                                    <c:choose>
                                        <c:when test="${cat.budgetStatus == 'no-budget'}">
                                            <span class="text-muted small">No budget set</span>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="d-flex justify-content-between text-muted small mb-1">
                                                <span><fmt:formatNumber value="${cat.budgetUsedPercent}" maxFractionDigits="0"/>% Used</span>
                                                <span class="${cat.budgetRemaining < 0 ? 'text-danger' : ''}">
                                                    <c:if test="${cat.budgetRemaining < 0}">Over by </c:if>
                                                    <c:if test="${cat.budgetRemaining >= 0}">Left: </c:if>
                                                    ₹<fmt:formatNumber value="${cat.budgetRemaining}" maxFractionDigits="0"/>
                                                </span>
                                            </div>
                                            <div class="progress-stacked">
                                                <div class="progress-bar-fill ${cat.budgetStatus == 'over' ? 'bg-danger' : (cat.budgetStatus == 'near' ? 'bg-warning' : 'bg-success')}" 
                                                     style="width: ${cat.budgetUsedPercent > 100 ? 100 : cat.budgetUsedPercent}%">
                                                </div>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td class="text-end text-muted">
                                    ₹<fmt:formatNumber value="${cat.previousPeriodSpent}" maxFractionDigits="0"/>
                                </td>

                                <td class="text-end">
                                    <c:choose>
                                        <c:when test="${not empty cat.changeAmount and cat.changeAmount != 0}">
                                            <span class="${cat.changeAmount > 0 ? 'trend-up' : 'trend-down'}">
                                                <c:if test="${cat.changeAmount > 0}">↑</c:if>
                                                <c:if test="${cat.changeAmount < 0}">↓</c:if>
                                                <fmt:formatNumber value="${cat.changePercent}" maxFractionDigits="1"/>%
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="trend-stable">-</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td class="text-end">
                                    <div class="d-flex align-items-center justify-content-end gap-2">
                                        <span><fmt:formatNumber value="${cat.percentOfTotal}" maxFractionDigits="1"/>%</span>
                                        <div style="width: 20px; height: 20px; border-radius: 50%; background: conic-gradient(var(--olive-leaf) ${cat.percentOfTotal}%, #e9ecef 0);"></div>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        
                        <c:if test="${empty categoryReport}">
                            <tr><td colspan="6" class="text-center py-4">No data available for the selected period.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>

    </div>
</div>

</body>
</html>
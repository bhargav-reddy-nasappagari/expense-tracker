<%
    // Prevent caching
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    
    // Session validation
    if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        /* Paste this inside the <style> tag in dashboard.jsp */
        .bg-primary { background-color: var(--olive-leaf) !important; }
        .bg-success { background-color: var(--olive-leaf) !important; }
        .bg-warning { background-color: #f1c40f !important; }
        .bg-danger  { background-color: #d9534f !important; }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }
        .stat-box {
            background: linear-gradient(135deg, var(--white) 0%, #f8f8f8 100%);
            padding: 1.5rem;
            border-radius: 12px;
            border-left: 4px solid var(--olive-leaf);
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
        }
        .stat-label {
            font-size: 0.85rem;
            color: var(--slate-grey);
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .stat-value {
            font-size: 1.8rem;
            font-weight: 700;
            color: var(--deep-space-blue);
            margin-top: 0.5rem;
        }
    </style>
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container" style="max-width: 1400px;">

        <jsp:include page="/WEB-INF/views/layout/message.jsp"/>

        <!-- Welcome Header -->
        <div class="card mb-4">
            <div class="card-header">
                <h2 id="greetingText">Welcome back<c:if test="${not empty user}">, ${user.fullName}</c:if>!</h2>
            </div>
            <div class="card-body">
                <div id="statsGrid" class="stats-grid">
                    <div class="stat-box">
                        <div class="stat-label">Total Expenses</div>
                        <div class="stat-value">${not empty expenses ? expenses.size() : 0}</div>
                    </div>
                </div>
                <div class="text-center mt-3">
                    <a href="expense/add" class="btn-primary">+ Add New Expense</a>
                </div>
            </div>
        </div>

        <!-- Charts Row -->
        <div class="row g-4">
            <!-- Pie Chart -->
            <div class="col-lg-6">
                <div class="card">
                    <div class="card-header">
                        <h4>Spending by Category (This Month)</h4>
                    </div>
                    <div class="card-body">
                        <canvas id="pieChart"></canvas>
                        <div id="pieChartEmpty" style="display: none;" class="text-center text-muted p-5">
                            No expenses this month yet!
                        </div>
                    </div>
                </div>
            </div>

            <!-- Bar Chart -->
            <div class="col-lg-6">
                <div class="card">
                    <div class="card-header">
                        <h4>Last 6 Months Spending</h4>
                    </div>
                    <div class="card-body">
                        <canvas id="barChart"></canvas>
                        <div id="barChartEmpty" style="display: none;" class="text-center text-muted p-5">
                            No expense history yet!
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Top Categories This Month -->
        <div class="row g-4 mt-1">
            
            <div class="col-lg-6">
                <div class="card h-100">
                    <div class="card-header">
                        <h3>Top Categories (This Month)</h3>
                    </div>
                    <div class="card-body">
                        <div id="topCategories">
                            <p class="text-center text-muted">Loading...</p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-6">
                <div class="card h-100">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h3>Budget Health</h3>
                        <a href="${pageContext.request.contextPath}/budgets" class="text-success" style="font-size: 0.9rem;">View All &rarr;</a>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty budgetOverview}">
                                <div class="text-center text-muted py-4">
                                    <p>No active budgets.</p>
                                    <a href="${pageContext.request.contextPath}/budgets" class="btn btn-sm btn-outline-primary">Set a Budget</a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <ul class="list-group list-group-flush">
                                    <c:forEach var="budget" items="${budgetOverview}">
                                        <c:set var="displayPct" value="${budget.amount > 0 ? (budget.spentAmount * 100.0 / budget.amount) : 0}" />

                                        <c:choose>
                                            <c:when test="${displayPct >= 100}"><c:set var="colorClass" value="bg-danger"/></c:when>
                                            <c:when test="${displayPct >= 85}"><c:set var="colorClass" value="bg-warning"/></c:when>
                                            <c:otherwise><c:set var="colorClass" value="bg-primary"/></c:otherwise>
                                        </c:choose>
                                        
                                        <li class="list-group-item px-0 border-0 mb-3">
                                            <div class="d-flex justify-content-between mb-1">
                                                <span class="fw-bold text-dark">${budget.categoryName}</span>
                                                <small class="${displayPct > 100 ? 'text-danger fw-bold' : 'text-muted'}">
                                                    <fmt:formatNumber value="${displayPct}" maxFractionDigits="0"/>%
                                                </small>
                                            </div>
                                            <div class="progress" style="height: 8px; background-color: #f0f2f5;">
                                                <div class="progress-bar ${colorClass}" 
                                                    role="progressbar" 
                                                    style="width: ${displayPct > 100 ? 100 : displayPct}%; height: 100%;" 
                                                    aria-valuenow="${displayPct}" 
                                                    aria-valuemin="0" 
                                                    aria-valuemax="100">
                                                </div>
                                            </div>
                                            <div class="d-flex justify-content-between mt-1">
                                                <small class="text-muted" style="font-size: 0.75rem;">
                                                    Spent: ₹<fmt:formatNumber value="${budget.spentAmount}" maxFractionDigits="0"/>
                                                </small>
                                                <small class="text-muted" style="font-size: 0.75rem;">
                                                    Limit: ₹<fmt:formatNumber value="${budget.amount}" maxFractionDigits="0"/>
                                                </small>
                                            </div>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            <div class="row mb-4">
                <div class="col-12">
                    <div class="card analytics-widget-card">
                        <div class="card-body d-flex flex-wrap align-items-center justify-content-between">
                            
                            <div class="pe-4" style="flex: 1; min-width: 200px;">
                                <div class="live-indicator">
                                    <span class="live-dot"></span> LIVE INSIGHTS
                                </div>
                                
                                <div class="analytics-value">
                                    ₹<fmt:formatNumber value="${not empty currentMonthTotal ? currentMonthTotal : 0}" maxFractionDigits="0" />
                                </div>

                                <div class="analytics-subtext mt-1">
                                    Spent this month 
                                    
                                    <c:choose>
                                        <c:when test="${not empty trendPercentage}">
                                            <span class="${trendPercentage > 0 ? 'trend-positive' : 'trend-negative'} fw-bold">
                                                ${trendPercentage > 0 ? '↑' : '↓'} 
                                                <fmt:formatNumber value="${trendPercentage < 0 ? -trendPercentage : trendPercentage}" maxFractionDigits="1"/>%
                                            </span> 
                                            vs last month
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted fw-bold">--%</span> vs last month
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="mt-3">
                                    <a href="${pageContext.request.contextPath}/reports" class="btn-sm btn-outline-primary" style="border:none; color: var(--olive-leaf);">
                                        View Full Analysis &rarr;
                                    </a>
                                </div>
                            </div>

                            <div style="flex: 1; min-width: 250px; max-width: 400px;">
                                <div class="sparkline-container">
                                    <canvas id="liveSparkline"></canvas>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent Expenses -->
        <div class="card mt-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h3>Recent Expenses</h3>
                <a href="expenses" class="text-success">View All →</a>
            </div>
            <c:choose>
                <c:when test="${empty expenses}">
                    <div class="p-5 text-center text-muted">
                        <p>No expenses yet. Start tracking!</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Description</th>
                                    <th>Amount</th>
                                    <th>Category</th>
                                    <th class="text-center">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="exp" items="${expenses}" begin="0" end="4">
                                    <tr>
                                        <td>${exp.expenseDate}</td>
                                        <td><c:out value="${exp.description}"/></td>
                                        <td class="text-success fw-bold">₹${exp.amount}</td>
                                        <td>
                                            <c:forEach var="cat" items="${categories}">
                                                <c:if test="${cat.id == exp.categoryId}">
                                                    <c:out value="${cat.name}"/>
                                                </c:if>
                                            </c:forEach>
                                        </td>
                                        <td class="text-center">
                                            <a href="expense/edit?id=${exp.id}" class="text-primary me-3">Edit</a>
                                            <!-- In dashboard.jsp, replace the delete link with: -->
                                            <a href="#" 
                                            class="text-danger delete-expense-btn" 
                                            data-url="expense/delete?id=${exp.id}">
                                                Delete
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="text-center mt-5">
            <p class="tagline fs-4">Your personal finance. Your rules. Your masterpiece.</p>
        </div>
    </div>
</div>

<!-- Hidden data containers for safe JavaScript parsing -->
<div id="expensesData" style="display:none;">
<c:forEach var="exp" items="${expenses}" varStatus="status">
<div class="expense-item" 
     data-date="${exp.expenseDate}" 
     data-amount="${exp.amount}" 
     data-category="${exp.categoryId}" 
     data-description="<c:out value='${exp.description}'/>">
</div>
</c:forEach>
</div>

<div id="categoriesData" style="display:none;">
<c:forEach var="cat" items="${categories}">
<div class="category-item" 
     data-id="${cat.id}" 
     data-name="<c:out value='${cat.name}'/>">
</div>
</c:forEach>
</div>
<!-- Delete Expense Modal -->
<div id="deleteModal" class="modal-overlay" style="display: none;">
    <div class="modal">
        <h3 class="modal-title">Delete Expense</h3>
        <p class="modal-text">Are you sure you want to delete this expense? This action cannot be undone.</p>
        
        <div class="modal-actions">
            <button class="btn-secondary modal-cancel">Cancel</button>
            <a id="deleteConfirmBtn" href="#" class="btn-primary modal-delete">Delete</a>
        </div>
    </div>
</div>
<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

<!-- Enhanced Dynamic Analytics Script -->
<script>
(function() {
    'use strict';
    
    // Dynamic Greeting based on time of day
    function setGreeting() {
        const greetingElement = document.getElementById('greetingText');
        if (!greetingElement) return;
        
        const hour = new Date().getHours();
        // Extract fullName from the greeting text (between ", " and "!")
        const username = greetingElement.textContent.match(/,\s*(.+)!/);
        const name = username ? username[1] : '';
        
        let greeting = '';
        let emoji = '';
        
        if (hour >= 5 && hour < 12) {
            greeting = 'Good morning';
            emoji = '☀️';
        } else if (hour >= 12 && hour < 17) {
            greeting = 'Good afternoon';
            emoji = '🌤️';
        } else if (hour >= 17 && hour < 21) {
            greeting = 'Good evening';
            emoji = '🌆';
        } else {
            greeting = 'Good night';
            emoji = '🌙';
        }
        
        if (name) {
            greetingElement.innerHTML = greeting + ' ' + emoji + ', <span style="color: var(--olive-leaf);">' + name + '</span>!';
        } else {
            greetingElement.textContent = greeting + ' ' + emoji + '!';
        }
    }
    
    // Set greeting on page load
    setGreeting();
    
    // Color Palette
    const colors = {
        primary: '#546236',
        secondary: '#43512e',
        tertiary: '#747C92',
        accent: '#06273C',
        dark: '#232528',
        light: '#eae1df'
    };

    const chartColors = [
        colors.primary, colors.tertiary, colors.accent, colors.secondary, 
        colors.dark, '#8a9aa0', '#3d4d2a', '#5d6374', '#2a3f52', '#4a5228'
    ];

    // Configure Chart.js defaults
    Chart.defaults.font.family = "'Inter', 'Segoe UI', system-ui, sans-serif";
    Chart.defaults.color = colors.dark;
    Chart.defaults.borderColor = 'rgba(116, 124, 146, 0.1)';

    // Parse expenses from DOM
    const expenses = [];
    const expenseElements = document.querySelectorAll('#expensesData .expense-item');
    expenseElements.forEach(function(el) {
        expenses.push({
            date: el.getAttribute('data-date'),
            amount: parseFloat(el.getAttribute('data-amount')) || 0,
            categoryId: parseInt(el.getAttribute('data-category')) || 0,
            description: el.getAttribute('data-description') || ''
        });
    });

    // Parse categories from DOM
    const categories = [];
    const categoryElements = document.querySelectorAll('#categoriesData .category-item');
    categoryElements.forEach(function(el) {
        categories.push({
            id: parseInt(el.getAttribute('data-id')) || 0,
            name: el.getAttribute('data-name') || 'Unknown'
        });
    });

    console.log('Loaded expenses:', expenses.length);
    console.log('Loaded categories:', categories.length);

    function getCategoryName(categoryId) {
        const category = categories.find(function(cat) {
            return cat.id === categoryId;
        });
        return category ? category.name : 'Unknown';
    }

    // Calculate statistics
    function calculateStats() {
        if (expenses.length === 0) {
            document.getElementById('statsGrid').innerHTML = 
                '<p class="text-center text-muted p-4">Add expenses to see analytics!</p>';
            document.getElementById('topCategories').innerHTML = 
                '<p class="text-center text-muted p-4">No data available</p>';
            return;
        }

        const now = new Date();
        const currentMonth = now.getMonth();
        const currentYear = now.getFullYear();

        let totalExpenses = 0;
        let thisMonthExpenses = 0;
        const categorySet = new Set();
        let maxExpense = 0;

        expenses.forEach(function(exp) {
            totalExpenses += exp.amount;
            categorySet.add(exp.categoryId);
            if (exp.amount > maxExpense) maxExpense = exp.amount;

            const expDate = new Date(exp.date);
            if (expDate.getMonth() === currentMonth && expDate.getFullYear() === currentYear) {
                thisMonthExpenses += exp.amount;
            }
        });

        const avgExpense = expenses.length > 0 ? (totalExpenses / expenses.length) : 0;

        const statsHTML = 
            '<div class="stat-box">' +
                '<div class="stat-label">Total Expenses</div>' +
                '<div class="stat-value">₹' + totalExpenses.toLocaleString('en-IN', {maximumFractionDigits: 2}) + '</div>' +
            '</div>' +
            '<div class="stat-box">' +
                '<div class="stat-label">This Month</div>' +
                '<div class="stat-value">₹' + thisMonthExpenses.toLocaleString('en-IN', {maximumFractionDigits: 2}) + '</div>' +
            '</div>' +
            '<div class="stat-box">' +
                '<div class="stat-label">Average Expense</div>' +
                '<div class="stat-value">₹' + avgExpense.toLocaleString('en-IN', {maximumFractionDigits: 2}) + '</div>' +
            '</div>' +
            '<div class="stat-box">' +
                '<div class="stat-label">Categories Used</div>' +
                '<div class="stat-value">' + categorySet.size + '</div>' +
            '</div>';

        document.getElementById('statsGrid').innerHTML = statsHTML;
    }

    // Pie Chart
    function createPieChart() {
        const ctxPie = document.getElementById('pieChart');
        if (!ctxPie) return;

        if (expenses.length === 0) {
            document.getElementById('pieChartEmpty').style.display = 'block';
            ctxPie.style.display = 'none';
            return;
        }

        const now = new Date();
        const currentMonth = now.getMonth();
        const currentYear = now.getFullYear();
        const categoryTotals = {};

        expenses.forEach(function(exp) {
            const expDate = new Date(exp.date);
            if (expDate.getMonth() === currentMonth && expDate.getFullYear() === currentYear) {
                const catName = getCategoryName(exp.categoryId);
                categoryTotals[catName] = (categoryTotals[catName] || 0) + exp.amount;
            }
        });

        const labels = Object.keys(categoryTotals);
        const data = Object.values(categoryTotals);

        if (labels.length === 0) {
            document.getElementById('pieChartEmpty').style.display = 'block';
            ctxPie.style.display = 'none';
            document.getElementById('topCategories').innerHTML = 
                '<p class="text-center text-muted p-4">No expenses this month</p>';
            return;
        }

        // Create top categories list
        const sortedCategories = labels.map(function(label, i) {
            return { label: label, value: data[i] };
        }).sort(function(a, b) {
            return b.value - a.value;
        });

        const total = data.reduce(function(a, b) { return a + b; }, 0);
        let topCatHTML = '<div style="display: grid; gap: 1rem;">';
        
        sortedCategories.forEach(function(cat, index) {
            const percentage = total > 0 ? ((cat.value / total) * 100).toFixed(1) : 0;
            topCatHTML += 
                '<div style="display: flex; align-items: center; justify-content: space-between; padding: 0.75rem; background: #f8f8f8; border-radius: 8px;">' +
                    '<div style="display: flex; align-items: center; gap: 0.75rem;">' +
                        '<div style="width: 12px; height: 12px; background: ' + chartColors[index % chartColors.length] + '; border-radius: 3px;"></div>' +
                        '<span style="font-weight: 600; color: var(--shadow-grey);">' + cat.label + '</span>' +
                    '</div>' +
                    '<div style="text-align: right;">' +
                        '<div style="font-weight: 700; color: var(--deep-space-blue);">₹' + cat.value.toLocaleString('en-IN', {maximumFractionDigits: 2}) + '</div>' +
                        '<div style="font-size: 0.85rem; color: var(--slate-grey);">' + percentage + '%</div>' +
                    '</div>' +
                '</div>';
        });
        
        topCatHTML += '</div>';
        document.getElementById('topCategories').innerHTML = topCatHTML;

        new Chart(ctxPie, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: chartColors.slice(0, labels.length),
                    borderWidth: 3,
                    borderColor: '#ffffff',
                    hoverBorderWidth: 4,
                    hoverOffset: 15
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 15,
                            font: { size: 12, weight: '500' },
                            usePointStyle: true,
                            pointStyle: 'circle'
                        }
                    },
                    tooltip: {
                        backgroundColor: colors.dark,
                        padding: 12,
                        cornerRadius: 8,
                        callbacks: {
                            label: function(context) {
                                const value = context.parsed;
                                const total = context.dataset.data.reduce(function(a, b) { return a + b; }, 0);
                                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
                                return context.label + ': ₹' + value.toLocaleString('en-IN') + ' (' + percentage + '%)';
                            }
                        }
                    }
                },
                cutout: '65%',
                animation: { duration: 1500, easing: 'easeInOutQuart' }
            }
        });
    }

    // Bar Chart - FIXED VERSION
    function createBarChart() {
        const ctxBar = document.getElementById('barChart');
        if (!ctxBar) return;

        if (expenses.length === 0) {
            document.getElementById('barChartEmpty').style.display = 'block';
            ctxBar.style.display = 'none';
            return;
        }

        // ✅ BULLETPROOF: Generate last 6 months with consistent keys
        const monthlyData = [];
        const now = new Date();
        
        for (let i = 5; i >= 0; i--) {
            const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
            const year = date.getFullYear();
            const month = date.getMonth(); // 0-11
            
            // Consistent key format: "2025-01" (YYYY-MM with zero-padding)
            const monthKey = year + '-' + String(month + 1).padStart(2, '0');
            const monthLabel = date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
            
            monthlyData.push({
                key: monthKey,      // "2025-01"
                label: monthLabel,  // "Jan 2025"
                total: 0,
                count: 0            // Track number of expenses for debugging
            });
        }

        console.log('📊 Bar Chart: Generated month buckets:', monthlyData.map(m => m.key));

        // ✅ BULLETPROOF: Parse expense dates with multiple fallback strategies
        function parseExpenseDate(dateString) {
            if (!dateString) {
                console.warn('⚠️ Empty date string encountered');
                return null;
            }

            // Strategy 1: Direct YYYY-MM-DD format (most common)
            // Input: "2025-01-15" → Output: "2025-01"
            if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
                return dateString.substring(0, 7);
            }

            // Strategy 2: Single-digit month/day (e.g., "2025-1-5")
            // Input: "2025-1-5" → Output: "2025-01"
            if (/^\d{4}-\d{1,2}-\d{1,2}$/.test(dateString)) {
                const parts = dateString.split('-');
                const year = parts[0];
                const month = parts[1].padStart(2, '0');
                return year + '-' + month;
            }

            // Strategy 3: ISO datetime (e.g., "2025-01-15T10:30:00")
            // Input: "2025-01-15T10:30:00" → Output: "2025-01"
            if (dateString.includes('T')) {
                const datePart = dateString.split('T')[0];
                if (/^\d{4}-\d{2}-\d{2}$/.test(datePart)) {
                    return datePart.substring(0, 7);
                }
            }

            // Strategy 4: JavaScript Date object fallback
            // Try to parse as Date and extract year-month
            try {
                const date = new Date(dateString);
                if (!isNaN(date.getTime())) {
                    const year = date.getFullYear();
                    const month = String(date.getMonth() + 1).padStart(2, '0');
                    return year + '-' + month;
                }
            } catch (e) {
                console.error('❌ Failed to parse date:', dateString, e);
            }

            console.error('❌ Unrecognized date format:', dateString);
            return null;
        }

        // ✅ Aggregate expenses into correct month buckets
        let processedCount = 0;
        let skippedCount = 0;

        expenses.forEach(function(exp) {
            const monthKey = parseExpenseDate(exp.date);
            
            if (!monthKey) {
                console.warn('⚠️ Skipping expense with invalid date:', exp);
                skippedCount++;
                return;
            }

            // Find matching month bucket
            const monthData = monthlyData.find(m => m.key === monthKey);
            
            if (monthData) {
                monthData.total += exp.amount;
                monthData.count++;
                processedCount++;
                console.log('✅ Added ₹' + exp.amount + ' to ' + monthKey + ' (Description: ' + exp.description + ')');
            } else {
                // Expense is outside the 6-month window (older or future)
                console.log('ℹ️ Expense outside date range:', exp.date, '→', monthKey);
            }
        });

        console.log('📊 Bar Chart Summary:');
        console.log('   - Total expenses processed:', processedCount);
        console.log('   - Expenses skipped (invalid dates):', skippedCount);
        console.log('   - Monthly totals:', monthlyData);

        // ✅ Prepare chart data
        const labels = monthlyData.map(m => m.label);
        const data = monthlyData.map(m => m.total);
        const counts = monthlyData.map(m => m.count);

        console.log('📊 Chart Data:');
        console.log('   Labels:', labels);
        console.log('   Amounts:', data);
        console.log('   Counts:', counts);

        // ✅ Show warning if no data in visible range
        if (processedCount === 0) {
            document.getElementById('barChartEmpty').style.display = 'block';
            ctxBar.style.display = 'none';
            document.getElementById('barChartEmpty').innerHTML = 
                '<p class="text-center text-muted p-4">No expenses in the last 6 months.<br>' +
                '<small>Total expenses in system: ' + expenses.length + '</small></p>';
            return;
        }

        // ✅ Render chart with enhanced tooltips
        new Chart(ctxBar, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Total Spent',
                    data: data,
                    backgroundColor: colors.primary,
                    borderRadius: 8,
                    hoverBackgroundColor: colors.secondary,
                    borderWidth: 2,
                    borderColor: 'rgba(84, 98, 54, 0.3)'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { 
                        display: false 
                    },
                    tooltip: {
                        backgroundColor: colors.dark,
                        padding: 12,
                        cornerRadius: 8,
                        titleFont: { size: 14, weight: 'bold' },
                        bodyFont: { size: 13 },
                        callbacks: {
                            title: function(context) {
                                return context[0].label;
                            },
                            label: function(context) {
                                const amount = context.parsed.y;
                                const count = counts[context.dataIndex];
                                return [
                                    'Spent: ₹' + amount.toLocaleString('en-IN', {maximumFractionDigits: 2}),
                                    'Expenses: ' + count
                                ];
                            },
                            footer: function(context) {
                                const idx = context[0].dataIndex;
                                if (idx > 0) {
                                    const current = data[idx];
                                    const previous = data[idx - 1];
                                    if (previous > 0) {
                                        const change = ((current - previous) / previous * 100).toFixed(1);
                                        const arrow = change > 0 ? '↑' : '↓';
                                        return arrow + ' ' + Math.abs(change) + '% vs last month';
                                    }
                                }
                                return '';
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { 
                            color: 'rgba(116, 124, 146, 0.1)',
                            drawBorder: false
                        },
                        ticks: {
                            callback: function(value) {
                                if (value >= 1000) {
                                    return '₹' + (value / 1000).toFixed(1) + 'k';
                                }
                                return '₹' + value;
                            },
                            font: { size: 11 }
                        }
                    },
                    x: {
                        grid: { 
                            display: false 
                        },
                        ticks: {
                            font: { size: 11 }
                        }
                    }
                },
                animation: {
                    duration: 1500,
                    easing: 'easeInOutQuart'
                }
            }
        });

        console.log('✅ Bar chart rendered successfully');
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            calculateStats();
            createPieChart();
            createBarChart();
        });
    } else {
        calculateStats();
        createPieChart();
        createBarChart();
    }
})();

(function() {
    console.log("Modal script starting...");
    
    function initModal() {
        const modal = document.getElementById("deleteModal");
        const confirmBtn = document.getElementById("deleteConfirmBtn");
        const cancelBtn = document.querySelector(".modal-cancel");
        const deleteButtons = document.querySelectorAll(".delete-expense-btn");
        
        console.log("Modal element:", modal);
        console.log("Delete buttons found:", deleteButtons.length);
        
        if (!modal || !confirmBtn || !cancelBtn) {
            console.error("Modal elements not found!");
            return;
        }
        
        deleteButtons.forEach(function(btn) {
            btn.addEventListener("click", function(e) {
                e.preventDefault();
                console.log("Delete clicked, URL:", btn.dataset.url);
                confirmBtn.href = btn.dataset.url;
                modal.style.display = "flex";
            });
        });
        
        cancelBtn.addEventListener("click", function() {
            console.log("Cancel clicked");
            modal.style.display = "none";
        });
        
        // Close on overlay click
        modal.addEventListener("click", function(e) {
            if (e.target === modal) {
                console.log("Overlay clicked");
                modal.style.display = "none";
            }
        });
        
        console.log("Modal initialized successfully");
    }
    
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initModal);
    } else {
        initModal();
    }
})();
// Live Analytics Sparkline
document.addEventListener('DOMContentLoaded', function() {
    const ctx = document.getElementById('liveSparkline').getContext('2d');
    
    // Gradient for the area under the line
    let gradient = ctx.createLinearGradient(0, 0, 0, 80);
    gradient.addColorStop(0, 'rgba(84, 98, 54, 0.2)'); // Olive Leaf with opacity
    gradient.addColorStop(1, 'rgba(84, 98, 54, 0.0)');

    new Chart(ctx, {
        type: 'line',
        data: {
            // Last 7 days or weeks labels
            labels: ['M', 'T', 'W', 'T', 'F', 'S', 'S'], 
            datasets: [{
                data: [120, 190, 30, 50, 20, 300, 150], // Inject your live data array here
                borderColor: '#546236', // var(--olive-leaf)
                borderWidth: 2,
                backgroundColor: gradient,
                fill: true,
                tension: 0.4, // Smooth curves
                pointRadius: 0, // Hide points for clean look
                pointHoverRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false }, tooltip: { enabled: true } },
            scales: {
                x: { display: false }, // Hide axes for "widget" look
                y: { display: false, min: 0 }
            },
            layout: { padding: 0 }
        }
    });
});
</script>
</body>
</html>
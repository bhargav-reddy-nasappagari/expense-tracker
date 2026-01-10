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
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Budgets • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* Paste this inside the <style> tag in budgets.jsp */
        .bg-primary { background-color: var(--olive-leaf) !important; }
        .bg-success { background-color: var(--olive-leaf) !important; } /* Unified Green */
        .bg-warning { background-color: #f1c40f !important; }
        .bg-danger  { background-color: #d9534f !important; }
        /* --- Stats Grid Container --- */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }

        /* --- Individual Stat Box (Card Look) --- */
        .stat-box {
            background: #ffffff;
            padding: 1.5rem;
            border-radius: 12px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05); /* Soft shadow like the reference */
            border-left: 5px solid #ccc; /* Default thick left border */
            transition: transform 0.2s ease;
        }

        .stat-box:hover {
            transform: translateY(-3px); /* Slight lift effect */
        }

        /* --- Typography (Matching the Reference) --- */
        .stat-label {
            font-size: 0.85rem;
            color: #6c757d; /* Muted gray */
            text-transform: uppercase;
            letter-spacing: 0.5px;
            font-weight: 600;
            margin-bottom: 0.5rem;
        }

        .stat-value {
            font-size: 1.75rem; /* Large and bold */
            font-weight: 700;
            line-height: 1.2;
        }

        /* --- Specific Coloring --- */

        /* 1. First Box: "Total Budgeted" -> Deep Blue (Matches Reference 'Budgeted') */
        .stat-box:nth-child(1) {
            border-left-color: var(--deep-space-blue, #0d6efd); 
        }
        .stat-box:nth-child(1) .stat-value {
            color: var(--deep-space-blue, #0d6efd);
        }

        /* 2. Second Box: "Active Budgets" -> Olive/Green (Matches Reference 'Remaining') */
        .stat-box:nth-child(2) {
            border-left-color: var(--olive-leaf, #198754);
        }
        .stat-box:nth-child(2) .stat-value {
            color: var(--olive-leaf, #198754);
        }
        /* ===== MODAL OVERLAY - NUCLEAR Z-INDEX ===== */
        .modal-overlay {
            display: none; /* Hidden by default */
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            background-color: rgba(0, 0, 0, 0.6);
            z-index: 999999 !important; /* NUCLEAR - Above everything */
            overflow-y: auto;
            align-items: flex-start;
            justify-content: center;
            padding: 50px 20px;
        }

        .modal {
            position: relative;
            background: #fff;
            width: 90%;
            max-width: 500px;
            border-radius: 12px;
            padding: 2rem;
            box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);
            z-index: 1000000 !important; /* NUCLEAR - Above overlay */
            margin: auto;
            animation: modalSlideIn 0.3s ease-out;
        }

        @keyframes modalSlideIn {
            from {
                opacity: 0;
                transform: translateY(-30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        /* Rest of your styles... */
        .budget-card {
            transition: transform 0.2s;
            height: 100%;
        }
        .budget-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
        }
        .progress-container {
            height: 10px;
            background-color: #eee;
            border-radius: 5px;
            margin: 15px 0;
            overflow: hidden;
        }
        .progress-bar {
            height: 100%;
            transition: width 0.5s ease-in-out;
        }
        .bg-success { background-color: var(--olive-leaf); }
        .bg-warning { background-color: #f0ad4e; }
        .bg-danger { background-color: #d9534f; }
        
        .budget-meta {
            font-size: 0.9rem;
            color: var(--slate-grey);
            margin-bottom: 0.5rem;
        }
</style>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container" style="max-width: 1200px;">

        <jsp:include page="/WEB-INF/views/layout/message.jsp"/>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>My Budgets</h2>
            <button id="createBudgetBtn" class="btn-primary">+ Set New Budget</button>
        </div>

        <div class="stats-grid mb-4">
            <div class="stat-box">
                <div class="stat-label">Total Budgeted</div>
                <div class="stat-value">
                    <c:set var="totalBudget" value="0"/>
                    <c:forEach items="${budgets}" var="b"><c:set var="totalBudget" value="${totalBudget + b.amount}"/></c:forEach>
                    ₹<fmt:formatNumber value="${totalBudget}" maxFractionDigits="0"/>
                </div>
            </div>
            <div class="stat-box">
                <div class="stat-label">Active Budgets</div>
                <div class="stat-value">${not empty budgets ? budgets.size() : 0}</div>
            </div>
        </div>

        <div class="row g-4">
            <c:forEach var="budget" items="${budgets}">
                <c:set var="rawPct" value="${budget.amount > 0 ? (budget.spentAmount * 100.0 / budget.amount) : 0}" />
                <c:set var="cleanWidth" value="${rawPct > 100 ? 100 : rawPct}" />

                <c:choose>
                    <c:when test="${rawPct >= 100}"><c:set var="barColor" value="bg-danger"/></c:when>
                    <c:when test="${rawPct >= 85}"><c:set var="barColor" value="bg-warning"/></c:when>
                    <c:otherwise><c:set var="barColor" value="bg-primary"/></c:otherwise>
                </c:choose>

                <fmt:formatNumber var="cleanWidth" value="${cssWidth}" maxFractionDigits="0" />

                <div class="col-md-6 col-lg-4">
                    <div class="card budget-card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <h4 class="mb-0 text-primary">${budget.categoryName}</h4>
                            <span class="badge" style="background: #eee; color: #555; padding-left: 5px;">
                                ${budget.recurring ? 'Recurring' : 'One-time'}
                            </span>
                        </div>
                        <div class="card-body">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="fw-bold text-dark">₹<fmt:formatNumber value="${budget.spentAmount}" maxFractionDigits="0"/></span>
                                <span class="text-muted">of ₹<fmt:formatNumber value="${budget.amount}" maxFractionDigits="0"/></span>
                            </div>

                            <div class="progress-container">
                                <div class="progress-bar ${barColor}" style="width: ${cleanWidth}%;"></div>
                            </div>

                            <p class="budget-meta">
                                <i class="bi bi-calendar"></i> 
                                ${budget.periodStart} 
                                <c:if test="${not empty budget.periodEnd}"> - ${budget.periodEnd}</c:if>
                            </p>

                            <div class="d-flex justify-content-end gap-3 mt-3">
                                <a href="${pageContext.request.contextPath}/budgets?action=detail&id=${budget.id}" 
                                style="color: #546236; text-decoration: none; font-weight: 600;">View Details</a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>

            <c:if test="${empty budgets}">
                <div class="col-12">
                    <div class="card p-5 text-center text-muted">
                        <h3>No active budgets</h3>
                        <p>Create a budget to start tracking your spending goals.</p>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</div>

<div id="createBudgetModal" class="modal-overlay">
    <div class="modal" style="max-width: 500px;">
        <h3 class="modal-title">Set New Budget</h3>
        <form action="${pageContext.request.contextPath}/budgets" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
            <input type="hidden" name="action" value="create">
            
            <div class="mb-3">
                <label class="form-label">Category</label>
                <select name="categoryId" class="form-control" required>
                    <option value="">-- Select Category --</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.id}">${cat.name}</option>
                    </c:forEach>
                </select>
            </div>
            
            <div class="mb-3">
                <label class="form-label">Amount Limit (₹)</label>
                <input type="number" step="0.01" name="amount" class="form-control" required min="0.01" placeholder="5000">
            </div>
            
            <div class="row">
                <div class="col-6 mb-3">
                    <label class="form-label">Start Date</label>
                    <input type="date" name="periodStart" class="form-control" required value="<%= java.time.LocalDate.now() %>">
                </div>
                <div class="col-6 mb-3">
                    <label class="form-label">End Date (Optional)</label>
                    <input type="date" name="periodEnd" class="form-control">
                    <small class="text-muted">Leave empty for ongoing</small>
                </div>
            </div>
            
            <div class="mb-4">
                <label class="d-flex align-items-center gap-2">
                    <input type="checkbox" name="recurring" id="isRecurring" checked>
                    <span>Recurring Monthly</span>
                </label>
            </div>

            <div class="d-flex justify-content-end gap-3">
                <button type="button" class="btn modal-close-edit me-3" 
                 style="background: transparent; border: none; color: #6c757d; font-weight: 500;">
                    Cancel
                </button>
                <button type="submit"  id="createBudgetBtn" class="btn-primary">Save Budget</button>
            </div>
        </form>
    </div>
</div>



<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

<script>
document.addEventListener('DOMContentLoaded', function() {
    
    const header = document.querySelector('.main-header');
    
    // ===== CREATE MODAL LOGIC =====
    const createModal = document.getElementById("createBudgetModal");
    const createBtn = document.getElementById("createBudgetBtn");
    const createCancel = document.querySelector(".modal-close-edit");

    function openCreateModal() {
        if (createModal) {
            createModal.style.display = "flex";
            document.body.style.overflow = "hidden";
            if (header) header.style.display = "none"; // ✅ HIDE HEADER
        }
    }

    function closeCreateModal() {
        if (createModal) {
            createModal.style.display = "none";
            document.body.style.overflow = "";
            if (header) header.style.display = ""; // ✅ SHOW HEADER
        }
    }

    if (createBtn) {
        createBtn.addEventListener("click", openCreateModal);
    }

    if (createCancel) {
        createCancel.addEventListener("click", closeCreateModal);
    }

    // ===== CLOSE ON OVERLAY CLICK =====
    window.onclick = function(event) {
        if (event.target === createModal) {
            closeCreateModal();
        }
        if (event.target === deleteModal) {
            closeDeleteModal();
        }
    };

    // ===== CLOSE ON ESCAPE KEY =====
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            if (createModal && createModal.style.display === 'flex') {
                closeCreateModal();
            }
            if (deleteModal && deleteModal.style.display === 'flex') {
                closeDeleteModal();
            }
        }
    });

    console.log("✅ Budget modals initialized (with header hide)");
});
</script>

</body>
</html>
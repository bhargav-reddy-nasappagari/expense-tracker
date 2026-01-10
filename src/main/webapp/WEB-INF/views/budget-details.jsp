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
    <title>${selectedBudget.categoryName} Details • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* Specific Styles for Budget Details */
        .progress-large {
            height: 24px;
            background-color: #e9ecef;
            border-radius: 12px;
            overflow: hidden;
            margin: 1.5rem 0;
            box-shadow: inset 0 1px 3px rgba(0,0,0,0.1);
        }
        .progress-bar-large {
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
            font-size: 0.85rem;
            transition: width 0.6s ease;
        }
        
        .stat-card-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 1.5rem;
            margin-top: 1rem;
        }
        
        .stat-item {
            text-align: center;
            padding: 1rem;
            background: #f8f9fa;
            border-radius: 8px;
            border-left: 4px solid #ccc;
        }
        
        .stat-item.budgeted { border-color: var(--deep-space-blue); }
        .stat-item.spent { border-color: #d9534f; }
        .stat-item.remaining { border-color: var(--olive-leaf); }
        
        .stat-label { font-size: 0.85rem; color: #666; text-transform: uppercase; letter-spacing: 0.5px; }
        .stat-val { font-size: 1.25rem; font-weight: bold; margin-top: 5px; }
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

        /* Fix for Task #1: Centered Total Spent Row */
        .total-row-highlight {
            background-color: rgba(84, 98, 54, 0.08); /* Light Olive Leaf tint for professional look */
            border-top: 2px solid var(--olive-leaf);
            border-bottom: 2px solid var(--olive-leaf);
        }

        .total-label {
            color: var(--slate-grey);
            text-transform: uppercase;
            letter-spacing: 1px;
            font-size: 0.95rem;
        }

        .total-amount {
            font-size: 1.15rem;
            display: inline-block;
            padding: 4px 12px;
            background: var(--white);
            border-radius: 6px;
            box-shadow: inset 0 0 0 1px rgba(217, 83, 79, 0.2); /* Subtle red border glow */
        }

        /* Ensure the table columns have a balanced width for centering to look right */
        .custom-table th:last-child, 
        .custom-table td:last-child {
            width: 180px; /* Constrains the amount column so centering is obvious */
        }
    </style>
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container" style="max-width: 1000px;">

        <div class="mb-4">
            <a href="${pageContext.request.contextPath}/budgets" class="text-primary" style="text-decoration: none; font-weight: 500;">
                &larr; Back to Budgets
            </a>
        </div>

        <div class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="m-0">${selectedBudget.categoryName}</h2>
                    <small class="text-muted">
                        <i class="bi bi-calendar3"></i> 
                        <c:choose>
                            <c:when test="${selectedBudget.recurring}">
                                ${currentMonthStartStr} – ${currentMonthEndStr}
                            </c:when>
                            <c:otherwise>
                                ${selectedBudget.periodStart} – ${selectedBudget.periodEnd}
                            </c:otherwise>
                        </c:choose>
                    </small>
                </div>
                <div class="d-flex gap-4">
                    <button id="editBtn" class="btn btn-sm" style="background: transparent; border: none; color: var(--olive-leaf); font-weight: 600; padding-right: 15px;">
                        <i class="bi bi-pencil-square"></i> Edit Budget
                    </button>

                    <button id="deleteBtn" class="btn btn-sm" style="background: transparent; border: none; color: #dc3545; font-weight: 600;">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </div>
            </div>

            <div class="card-body">
                <%-- Calculations --%>
                <c:set var="rawPct" value="${selectedBudget.amount > 0 ? (selectedBudget.spentAmount / selectedBudget.amount * 100) : 0}" />
                <c:set var="displayPct" value="${rawPct > 100 ? 100 : rawPct}" />
                <c:set var="remaining" value="${selectedBudget.amount - selectedBudget.spentAmount}" />
                
                <%-- Color Logic --%>
                <c:set var="barColor" value="${rawPct > 100 ? 'bg-danger' : (rawPct > 85 ? 'bg-warning' : 'bg-success')}" />
                <c:set var="bgColor" value="${rawPct > 100 ? '#dc3545' : (rawPct > 85 ? '#ffc107' : '#28a745')}" />

                <c:if test="${rawPct > 100}">
                    <div class="alert alert-danger mb-3" style="background: #f8d7da; color: #721c24; padding: 10px; border-radius: 6px; border: 1px solid #f5c6cb;">
                        <strong>Warning:</strong> You have exceeded this budget by 
                        <fmt:formatNumber value="${selectedBudget.spentAmount - selectedBudget.amount}" type="currency" currencySymbol="₹" maxFractionDigits="0"/>!
                    </div>
                </c:if>

                <div class="stat-card-grid">
                    <div class="stat-item budgeted">
                        <div class="stat-label">Budgeted</div>
                        <div class="stat-val text-primary">₹<fmt:formatNumber value="${selectedBudget.amount}" maxFractionDigits="0"/></div>
                    </div>
                    <div class="stat-item spent">
                        <div class="stat-label">Spent</div>
                        <div class="stat-val text-danger">₹<fmt:formatNumber value="${selectedBudget.spentAmount}" maxFractionDigits="0"/></div>
                    </div>
                    <div class="stat-item remaining">
                        <div class="stat-label">Remaining</div>
                        <div class="stat-val ${remaining < 0 ? 'text-danger' : 'text-success'}">
                            ₹<fmt:formatNumber value="${remaining}" maxFractionDigits="0"/>
                        </div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-label">Used</div>
                        <div class="stat-val text-muted">
                            <fmt:formatNumber value="${rawPct}" maxFractionDigits="1"/>%
                        </div>
                    </div>
                </div>

                <div class="progress-large">
                    <div class="progress-bar-large" style="width: ${displayPct}%; background-color: ${bgColor};">
                        <c:if test="${displayPct > 10}">
                            <fmt:formatNumber value="${rawPct}" maxFractionDigits="0"/>%
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h3>Expenses in this Budget</h3>
            </div>
            
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Description</th>
                            <th class="text-end">Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="expense" items="${relatedExpenses}">
                            <tr>
                                <td>${expense.expenseDate}</td>
                                <td><c:out value="${expense.description}"/></td>
                                <td class="text-end fw-bold">₹<fmt:formatNumber value="${expense.amount}" maxFractionDigits="2"/></td>
                            </tr>
                        </c:forEach>
                        
                        <c:if test="${empty relatedExpenses}">
                            <tr>
                                <td colspan="3" class="text-center text-muted py-4">
                                    No expenses recorded in this budget period yet.
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                    <c:if test="${not empty relatedExpenses}">
                        <tfoot style="background-color: #f8f9fa; font-weight: bold;">
                            <%-- Bottom of the expenses table inside budget-details.jsp --%>
                            <tr class="total-row-highlight">
                                <%-- Center the label across the first two columns --%>
                                <td colspan="2" class="text-center fw-bold py-3">
                                    <span class="total-label">Total Spent</span>
                                </td>
                                <%-- Center the amount specifically within its own column --%>
                                <td class="text-center fw-bold py-3">
                                    <span class="total-amount text-danger">
                                        ₹<fmt:formatNumber value="${selectedBudget.spentAmount}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </span>
                                </td>
                            </tr>
                        </tfoot>
                    </c:if>
                </table>
            </div>
        </div>

    </div>
</div>

<div id="editModal" class="modal-overlay">
    <div class="modal" style="max-width: 500px;">
        <h3 class="modal-title mb-3">Edit Budget</h3>
        <form action="${pageContext.request.contextPath}/budgets" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="budgetId" value="${selectedBudget.id}">
            
            <div class="mb-3">
                <label class="form-label">Category</label>
                <input type="text" class="form-control" value="${selectedBudget.categoryName}" disabled style="background: #e9ecef;">
            </div>

            <div class="mb-3">
                <label class="form-label">Amount Limit (₹)</label>
                <input type="number" step="0.01" name="amount" class="form-control" 
                       value="${selectedBudget.amount}" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Period End Date</label>
                <input type="date" name="periodEnd" class="form-control" 
                       value="${selectedBudget.periodEnd}">
                <small class="text-muted">Leave empty for indefinite</small>
            </div>

            <div class="mb-4">
                <label class="d-flex align-items-center gap-2">
                    <input type="checkbox" name="recurring" ${selectedBudget.recurring ? 'checked' : ''}>
                    <span>Recurring Monthly</span>
                </label>
            </div>
                
            <div class="d-flex justify-content-end gap-3">
                <button type="button" class="btn modal-close-edit me-3" 
                 style="background: transparent; border: none; color: #6c757d; font-weight: 500;">
                    Cancel
                </button>
                <button type="submit" class="btn-primary">Save Changes</button>
            </div>

        </form>
    </div>
</div>

<div id="deleteModal" class="modal-overlay">
    <div class="modal">
        <h3 class="modal-title text-danger">Delete Budget?</h3>
        <p>Are you sure you want to delete the budget for <strong>${selectedBudget.categoryName}</strong>?</p>
        <p class="text-muted small">This will NOT delete the expenses, only the budget limit tracking.</p>
        
        <div class="d-flex justify-content-end gap-2 mt-4">
            <button type="button" class="btn modal-close-delete me-3" 
             style="background: transparent; border: none; color: #6c757d; font-weight: 500;">
                Cancel
            </button>
            
            <form action="${pageContext.request.contextPath}/budgets" method="post">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="budgetId" value="${selectedBudget.id}">
                <button type="submit" id="deleteBtn" class="btn-danger" style="background: #dc3545; color: white; border: none; padding: 0.5rem 1rem; border-radius: 4px;">Confirm Delete</button>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

<script>
    document.addEventListener("DOMContentLoaded", function() {
        
        const header = document.querySelector('.main-header');

        // --- 1. EDIT MODAL LOGIC ---
        const editModal = document.getElementById("editModal");
        const editBtn = document.getElementById("editBtn");
        const editCloseBtns = document.querySelectorAll(".modal-close-edit");

        // Open Edit Modal
        if (editBtn) {
            editBtn.addEventListener("click", function() {
                editModal.style.display = "flex";
                document.body.style.overflow = "hidden";
            if (header) header.style.display = "none"; // ✅ HIDE HEADER
            });
        }

        // Close Edit Modal (Cancel button)
        editCloseBtns.forEach(function(btn) {
            btn.addEventListener("click", function() {
                editModal.style.display = "none";
                document.body.style.overflow = "";
            if (header) header.style.display = ""; // ✅ SHOW HEADER
            });
        });

        // --- 2. DELETE MODAL LOGIC ---
        const deleteModal = document.getElementById("deleteModal");
        const deleteBtn = document.getElementById("deleteBtn");
        const deleteCloseBtns = document.querySelectorAll(".modal-close-delete");

        // Open Delete Modal
        if (deleteBtn) {
            deleteBtn.addEventListener("click", function() {
                deleteModal.style.display = "flex";
            });
        }

        // Close Delete Modal (Cancel button)
        deleteCloseBtns.forEach(function(btn) {
            btn.addEventListener("click", function() {
                deleteModal.style.display = "none";
            });
        });

        // --- 3. GLOBAL OVERLAY CLICK TO CLOSE ---
        window.onclick = function(event) {
            if (event.target === editModal) {
                editModal.style.display = "none";
                document.body.style.overflow = "";
                if (header) header.style.display = ""; // ✅ SHOW HEADER
            }
            if (event.target === deleteModal) {
                deleteModal.style.display = "none";
            }
        };

        // --- 4. ESCAPE KEY TO CLOSE ---
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                if (editModal && editModal.style.display === 'flex') {
                    editModal.style.display = 'none';
                    document.body.style.overflow = "";
                    if (header) header.style.display = ""; // ✅ SHOW HEADER
                }
                if (deleteModal && deleteModal.style.display === 'flex') {
                    deleteModal.style.display = 'none';
                }
            }
        });
    });
</script>

</body>
</html>
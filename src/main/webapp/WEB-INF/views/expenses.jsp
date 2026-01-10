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

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>All Expenses • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container">

        <jsp:include page="/WEB-INF/views/layout/message.jsp"/>

        <div class="card mb-5">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h2 class="h3 mb-0">All Expenses</h2>
                
                <div class="d-flex align-items-center" style="gap: 12px;">

                    <a href="${pageContext.request.contextPath}/expense/add" class="btn-header-action">
                        <i class="bi bi-plus-lg" style="margin-right: 8px;"></i> Add New Expense
                    </a>
                    
                    <div class="dropdown" style="position: relative;">
                        <button class="btn-header-action" type="button" onclick="toggleExportMenu()" id="exportBtn">
                            <i class="bi bi-download" style="margin-right: 8px;"></i> Export
                        </button>
                        
                        <div id="exportDropdown" class="custom-dropdown-menu">
                            <a href="javascript:void(0)" onclick="handleExport('csv')">
                                <i class="bi bi-filetype-csv me-2"></i> Save as CSV
                            </a>
                            <a href="javascript:void(0)" onclick="handleExport('pdf')">
                                <i class="bi bi-file-pdf me-2"></i> Save as PDF
                            </a>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Filters -->
            <div class="command-bar-wrapper mb-4 shadow-sm">
                <form method="get" id="filterForm" class="command-bar d-flex flex-wrap gap-3 align-items-end">
                    
                    <div class="filter-item" style="min-width: 200px;">
                        <span class="filter-label">Search</span>
                        <div class="filter-input-wrapper">
                            <i class="bi bi-search"></i>
                            <input type="text" name="keyword" class="clean-input" 
                                   placeholder="Description..." value="${filterKeyword}">
                        </div>
                    </div>

                    <div class="filter-item" style="width: 130px;">
                        <span class="filter-label">Min Amount</span>
                        <div class="filter-input-wrapper">
                            <i class="bi bi-currency-rupee"></i>
                            <input type="number" name="minAmount" class="clean-input" 
                                   placeholder="0" step="1" value="${filterMinAmount}">
                        </div>
                    </div>

                    <div class="filter-item" style="width: 130px;">
                        <span class="filter-label">Max Amount</span>
                        <div class="filter-input-wrapper">
                            <i class="bi bi-currency-rupee"></i>
                            <input type="number" name="maxAmount" class="clean-input" 
                                   placeholder="Max" step="1" value="${filterMaxAmount}">
                        </div>
                    </div>
                    
                    <div class="filter-item">
                        <span class="filter-label">From</span>
                        <div class="filter-input-wrapper">
                            <i class="bi bi-calendar-event"></i>
                            <input type="date" name="from" class="clean-input" value="${filterFrom}">
                        </div>
                    </div>

                    <div class="filter-item">
                        <span class="filter-label">To</span>
                        <div class="filter-input-wrapper">
                            <i class="bi bi-calendar-check"></i>
                            <input type="date" name="to" class="clean-input" value="${filterTo}">
                        </div>
                    </div>

                    <div class="filter-item flex-grow-1">
                        <span class="filter-label">Category</span>
                        <div class="filter-input-wrapper">
                            <i class="bi bi-tag"></i>
                            <select name="categoryId" class="clean-select">
                                <option value="">All Transactions</option>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.id}" ${cat.id == filterCategoryId ? 'selected' : ''}>${cat.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="filter-actions d-flex gap-2">
                        <button type="submit" class="btn-apply px-4">
                            <i class="bi bi-funnel-fill me-2"></i>Filter
                        </button>
                        <a href="expenses" class="btn-reset-glass">
                            <i class="bi bi-arrow-counterclockwise"></i>
                        </a>
                    </div>
                </form>
            </div>

            <!-- Results Summary & Page Size Selector -->
            <div class="p-4 bg-light" style="border-top: 1px solid rgba(116, 124, 146, 0.1);">
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                    <div class="page-info" style="padding-left: 5px;">
                        <c:choose>
                            <c:when test="${totalItems > 0}">
                                Showing <strong>${startIndex}</strong> to <strong>${endIndex}</strong> of <strong>${totalItems}</strong> expenses
                            </c:when>
                            <c:otherwise>
                                No expenses found
                            </c:otherwise>
                        </c:choose>
                    </div>
                    
                    <div class="page-size-selector" style="padding-right: 5px;">
                        <label>Items per page:</label>
                        <select id="pageSizeSelect" onchange="changePageSize(this.value)">
                            <option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option>
                            <option value="20" ${pageSize == 20 ? 'selected' : ''}>20</option>
                            <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
                            <option value="100" ${pageSize == 100 ? 'selected' : ''}>100</option>
                        </select>
                    </div>
                </div>
            </div>

            <!-- Results Table -->
            <c:choose>
                <c:when test="${empty expenses}">
                    <div class="p-5 text-center text-muted">
                        <p class="fs-4">No expenses found.</p>
                        <a href="${pageContext.request.contextPath}/expense/add" class="btn-primary">Add Your First Expense</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th onclick="handleSort('date')" style="cursor: pointer;" 
                                        title="${sortBy == 'date' && sortOrder == 'asc' ? 'Click to sort Descending' : 'Click to sort Ascending'}">
                                        Date
                                        <c:if test="${sortBy == 'date'}">
                                            <i class="bi bi-arrow-${sortOrder == 'asc' ? 'up' : 'down'} text-primary"></i>
                                        </c:if>
                                        <c:if test="${sortBy != 'date'}">
                                            <i class="bi bi-arrow-down-up text-muted" style="font-size: 0.8em; opacity: 0.5;"></i>
                                        </c:if>
                                    </th>

                                    <th>
                                        Description
                                    </th>

                                    <th onclick="handleSort('amount')" style="cursor: pointer;"
                                        title="${sortBy == 'amount' && sortOrder == 'asc' ? 'Click to sort Descending' : 'Click to sort Ascending'}">
                                        Amount
                                        <c:if test="${sortBy == 'amount'}">
                                            <i class="bi bi-arrow-${sortOrder == 'asc' ? 'up' : 'down'} text-primary"></i>
                                        </c:if>
                                    </th>

                                    <th onclick="handleSort('category')" style="cursor: pointer;"
                                        title="${sortBy == 'category' && sortOrder == 'asc' ? 'Click to sort Descending' : 'Click to sort Ascending'}">
                                        Category
                                        <c:if test="${sortBy == 'category'}">
                                            <i class="bi bi-arrow-${sortOrder == 'asc' ? 'up' : 'down'} text-primary"></i>
                                        </c:if>
                                    </th>

                                    <th class="text-center">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="exp" items="${expenses}">
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
                                            <a href="${pageContext.request.contextPath}/expense/edit?id=${exp.id}" 
                                               class="text-primary me-3">Edit</a>
                                            <a href="#" 
                                               class="text-danger delete-expense-btn" 
                                               data-id="${exp.id}">
                                                Delete
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <!-- Pagination Controls -->
                    <c:if test="${totalPages > 1}">
                        <div class="pagination">
                            <!-- First Page -->
                            <a href="?page=1${filterParams}" 
                               class="${currentPage == 1 ? 'disabled' : ''}"
                               title="First Page">
                                « First
                            </a>
                            
                            <!-- Previous Page -->
                            <a href="?page=${currentPage - 1}${filterParams}" 
                               class="${!hasPrevious ? 'disabled' : ''}"
                               title="Previous Page">
                                ‹ Prev
                            </a>
                            
                            <!-- Page Numbers -->
                            <c:forEach var="i" begin="${currentPage - 2 > 0 ? currentPage - 2 : 1}" 
                                       end="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}">
                                <c:choose>
                                    <c:when test="${i == currentPage}">
                                        <span class="current-page">${i}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="?page=${i}${filterParams}">${i}</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            
                            <!-- Next Page -->
                            <a href="?page=${currentPage + 1}${filterParams}" 
                               class="${!hasNext ? 'disabled' : ''}"
                               title="Next Page">
                                Next ›
                            </a>
                            
                            <!-- Last Page -->
                            <a href="?page=${totalPages}${filterParams}" 
                               class="${currentPage == totalPages ? 'disabled' : ''}"
                               title="Last Page">
                                Last »
                            </a>
                            
                            <!-- Page Info -->
                            <span class="page-info">
                                Page ${currentPage} of ${totalPages}
                            </span>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="text-center mt-5">
            <p class="tagline fs-4">Your personal finance. Your rules. Your masterpiece.</p>
        </div>
    </div>
</div>

<!-- Delete Expense Modal -->
<div id="deleteModal" class="modal-overlay" style="display: none;">
    <div class="modal">
        <h3 class="modal-title">Delete Expense</h3>
        <p class="modal-text">Are you sure you want to delete this expense? This action cannot be undone.</p>
        
        <div class="modal-actions">
            <button class="btn-secondary modal-cancel">Cancel</button>
            <form id="deleteForm" method="POST" action="${pageContext.request.contextPath}/expense/delete">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />           
                <input type="hidden" name="id" id="deleteExpenseId" value="">

                <button type="submit" class="btn-primary modal-delete">Delete</button>
            </form>       
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

<script>
// Build filter params for pagination links
(function() {
    const urlParams = new URLSearchParams(window.location.search);
    const filterParams = [];
    
    if (urlParams.get('from')) filterParams.push('from=' + urlParams.get('from'));
    if (urlParams.get('to')) filterParams.push('to=' + urlParams.get('to'));
    if (urlParams.get('categoryId')) filterParams.push('categoryId=' + urlParams.get('categoryId'));

    // NEW: Add these to pagination links
    if (urlParams.get('keyword')) filterParams.push('keyword=' + encodeURIComponent(urlParams.get('keyword')));
    if (urlParams.get('minAmount')) filterParams.push('minAmount=' + urlParams.get('minAmount'));
    if (urlParams.get('maxAmount')) filterParams.push('maxAmount=' + urlParams.get('maxAmount'));

    // -- NEW: Preserve Sorting in Pagination Links --
    if (urlParams.get('sortBy')) filterParams.push('sortBy=' + urlParams.get('sortBy'));
    if (urlParams.get('sortOrder')) filterParams.push('sortOrder=' + urlParams.get('sortOrder'));
    
    if (urlParams.get('pageSize')) filterParams.push('pageSize=' + urlParams.get('pageSize'));
    
    const filterString = filterParams.length > 0 ? '&' + filterParams.join('&') : '';
    
    // Update all pagination links
    document.querySelectorAll('.pagination a').forEach(function(link) {
        if (!link.classList.contains('disabled')) {
            const href = link.getAttribute('href');
            if (href && href.startsWith('?page=')) {
                link.setAttribute('href', href + filterString);
            }
        }
    });
})();

// -- Handle Sorting Logic --
function handleSort(column) {
    const urlParams = new URLSearchParams(window.location.search);
    const currentSortBy = urlParams.get('sortBy');
    const currentSortOrder = urlParams.get('sortOrder');
    
    let newOrder = 'asc';
    
    // If clicking the same column that is currently active...
    if (currentSortBy === column) {
        // Toggle the order
        if (currentSortOrder === 'asc') {
            newOrder = 'desc';
        } else {
            newOrder = 'asc';
        }
    }
    // If clicking a new column, default is 'asc' (already set)

    // Update URL parameters
    urlParams.set('sortBy', column);
    urlParams.set('sortOrder', newOrder);
    urlParams.set('page', '1'); // Always reset to page 1 when sorting changes
    
    // Reload page
    window.location.search = urlParams.toString();
}

// Change page size
function changePageSize(newSize) {
    const urlParams = new URLSearchParams(window.location.search);
    urlParams.set('pageSize', newSize);
    urlParams.set('page', '1'); // Reset to page 1
    window.location.search = urlParams.toString();
}

// Delete modal functionality
(function() {
    function initModal() {
        const modal = document.getElementById("deleteModal");
        const cancelBtn = document.querySelector(".modal-cancel");
        const deleteButtons = document.querySelectorAll(".delete-expense-btn");
        
        // NEW: Select the hidden input field inside the form
        const deleteInput = document.getElementById("deleteExpenseId");
        
        if (!modal || !cancelBtn) return;
        
        deleteButtons.forEach(function(btn) {
            btn.addEventListener("click", function(e) {
                e.preventDefault();
                
                // 1. Get the ID from the button's data-id attribute
                const expenseId = btn.dataset.id;
                
                // 2. Set that ID into the hidden input value
                if (deleteInput) {
                    deleteInput.value = expenseId;
                }
                
                // 3. Show the modal
                modal.style.display = "flex";
            });
        });
        
        cancelBtn.addEventListener("click", function() {
            modal.style.display = "none";
        });
        
        modal.addEventListener("click", function(e) {
            if (e.target === modal) {
                modal.style.display = "none";
            }
        });
    }
    
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initModal);
    } else {
        initModal();
    }
})();    // Toggle the dropdown visibility
    function toggleExportMenu() {
        document.getElementById("exportDropdown").classList.toggle("show");
    }

    // Close dropdown if clicked outside
    window.onclick = function(event) {
        if (!event.target.matches('#exportBtn') && !event.target.closest('#exportBtn')) {
            var dropdown = document.getElementById("exportDropdown");
            if (dropdown && dropdown.classList.contains('show')) {
                dropdown.classList.remove('show');
            }
        }
    }

    // Handle the redirect with current filters
    function handleExport(format) {
        // Capture current filters from URL (e.g., ?startDate=2024-01-01)
        const urlParams = new URLSearchParams(window.location.search);
        urlParams.set('format', format);
        
        // Redirect to Servlet
        const baseUrl = '${pageContext.request.contextPath}/expenses/export';
        window.location.href = baseUrl + '?' + urlParams.toString();
    }
</script>

</body>
</html>
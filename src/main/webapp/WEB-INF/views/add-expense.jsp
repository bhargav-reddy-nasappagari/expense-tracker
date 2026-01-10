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
    <title>Add Expense • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container" style="max-width: 600px;">

        <jsp:include page="/WEB-INF/views/layout/message.jsp"/>

        <div class="card">
            <div class="card-header">
                <h2>Add New Expense</h2>
            </div>

            <div class="card-body p-5">
                <form action="${pageContext.request.contextPath}/expense/add" method="post" novalidate>
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />

                    <div class="form-group mb-3">
                        <label for="description">Description *</label>
                        <input type="text" 
                            id="description" 
                            name="description" 
                            class="form-control ${not empty errors.description ? 'is-invalid' : ''}" 
                            maxlength="255" 
                            placeholder="e.g., Grocery shopping" 
                            value="${param.description}"> <c:if test="${not empty errors.description}">
                            <div class="invalid-feedback">${errors.description}</div>
                        </c:if>
                    </div>

                    <div class="form-group mb-3">
                        <label for="amount">Amount (₹) *</label>
                        <input type="number" 
                            id="amount" 
                            name="amount" 
                            class="form-control ${not empty errors.amount ? 'is-invalid' : ''}" 
                            step="0.01" 
                            placeholder="0.00" 
                            value="${param.amount}">
                            
                        <c:if test="${not empty errors.amount}">
                            <div class="invalid-feedback">${errors.amount}</div>
                        </c:if>
                    </div>

                    <div class="form-group mb-3">
                        <label for="categoryId">Category *</label>
                        <select id="categoryId" 
                                name="categoryId" 
                                class="form-control ${not empty errors.categoryId ? 'is-invalid' : ''}">
                            <option value="">-- Select Category --</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.id}" 
                                    <c:if test="${cat.id.toString() == param.categoryId}">selected</c:if>>
                                    ${cat.name}
                                </option>
                            </c:forEach>
                        </select>
                        
                        <c:if test="${not empty errors.categoryId}">
                            <div class="invalid-feedback">${errors.categoryId}</div>
                        </c:if>
                    </div>

                    <div class="form-group mb-3">
                        <label for="expenseDate">Date *</label>
                        <input type="date" 
                            id="expenseDate" 
                            name="expenseDate" 
                            class="form-control ${not empty errors.expenseDate ? 'is-invalid' : ''}" 
                            value="${not empty param.expenseDate ? param.expenseDate : java.time.LocalDate.now()}">
                            
                        <c:if test="${not empty errors.expenseDate}">
                            <div class="invalid-feedback">${errors.expenseDate}</div>
                        </c:if>
                    </div>

                    <div class="d-flex justify-content-between mt-5">
                        <a href="${pageContext.request.contextPath}/expenses" class="btn-olive-link btn-sm">← Back</a>
                        <button type="submit" class="btn-primary">Save Expense</button>
                    </div>
                </form>
            </div>
        </div>

        <div class="text-center mt-5">
            <p class="tagline fs-4">Your personal finance. Your rules. Your masterpiece.</p>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>
</body>
</html>
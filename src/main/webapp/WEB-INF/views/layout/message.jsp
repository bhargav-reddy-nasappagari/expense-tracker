<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${not empty sessionScope.successMessage}">
    <div class="alert alert-success">
        <strong>Success!</strong> ${sessionScope.successMessage}
    </div>
    <% session.removeAttribute("successMessage"); %>
</c:if>

<c:if test="${not empty sessionScope.errorMessage}">
    <div class="alert alert-danger">
        <strong>Error!</strong> ${sessionScope.errorMessage}
    </div>
    <% session.removeAttribute("errorMessage"); %>
</c:if>

<c:if test="${sessionScope.needsVerification}">
    <div style="background-color: #fff3cd; color: #856404; padding: 15px; border: 1px solid #ffeeba; border-radius: 5px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between;">
        <div>
            <strong>Action Required:</strong> Please verify your email to unlock full access (Exports, Budgets).
            <br>
            <small>We sent a verification link to your inbox.</small>
        </div>
        
        <form action="${pageContext.request.contextPath}/resend-verification" method="POST" style="margin: 0;">
             <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" /> 
            <input type="hidden" name="username" value="${sessionScope.user.username}" />
            
            <button type="submit" style="background: #856404; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; font-size: 0.9rem;">
                Resend Link
            </button>
        </form>
    </div>
</c:if>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<header class="main-header">
    <div class="container header-container">
        <div class="logo">
            <a href="${pageContext.request.contextPath}/dashboard">ExpenseTracker</a>
        </div>

        <nav class="main-nav">
            <ul>
                <li><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/expenses">Expenses</a></li>
                <li><a href="${pageContext.request.contextPath}/budgets">Budgets</a></li>
                <li><a href="${pageContext.request.contextPath}/reports">Reports</a></li> 
                
                <li><a href="${pageContext.request.contextPath}/category">Categories</a></li>
                <li><a href="${pageContext.request.contextPath}/profile">Profile</a></li>
            </ul>
        </nav>

        <div class="user-section">
            <c:choose>
                <c:when test="${sessionScope.user != null}">
                    <span class="welcome-text">Welcome, ${sessionScope.user.username}!</span>
                    <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Logout</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login.jsp" class="btn-login">Login</a>
                    <a href="${pageContext.request.contextPath}/register.jsp" class="btn-register">Register</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>
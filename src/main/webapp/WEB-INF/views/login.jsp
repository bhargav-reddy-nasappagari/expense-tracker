<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <!--<link rel="stylesheet" href="css/auth.css">-->
</head>
<body class="auth-page">
    
    <div class="auth-container">
        <div class="auth-card">
            <!-- Brand Section -->
            <div class="auth-brand">
                <div class="auth-brand-icon"></div>
                <h1>Welcome Back</h1>
                <p>Sign in to manage your expenses</p>
            </div>

            <!-- ✅ Registration Success -->
            <c:if test="${param.registered == 'true'}">
                <div class="auth-success">
                    Registration successful! Please sign in.
                </div>
            </c:if>

            <!-- ✅ Email Verified Success -->
            <c:if test="${param.verified == 'true'}">
                <div class="auth-success">
                    ✅ Email verified successfully! You can now log in.
                </div>
            </c:if>

            <!-- ✅ Logout Success -->
            <c:if test="${param.logout == 'true'}">
                <div class="auth-success">
                    You have been logged out successfully.
                </div>
            </c:if>


            <!-- Error Message (if any) -->
            <%-- We use the 'c:if' tag or a simple EL check --%>
            <c:if test="${not empty error}">
                <div class="auth-error" style="color: #d9534f; background-color: #f2dede; border: 1px solid #ebccd1; padding: 10px; border-radius: 4px; margin-bottom: 15px;">
                    ${error}
                </div>
            </c:if>

            <c:if test="${showResend}">
                <div style="margin-bottom: 20px; text-align: center;">
                    <p style="font-size: 0.9rem; color: #666; margin-bottom: 10px;">
                        Didn't receive the link?
                    </p>
                    
                    <form action="${pageContext.request.contextPath}/resend-verification" method="POST">
                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                        
                        <input type="hidden" name="username" value="${username}" />
                        
                        <button type="submit" style="background: none; border: none; color: var(--olive-leaf, #546236); text-decoration: underline; cursor: pointer; font-weight: bold; font-size: 0.95rem;">
                            Resend Verification Email
                        </button>
                    </form>
                </div>
            </c:if>

            <!-- Login Form -->
            <form action="${pageContext.request.contextPath}/login" method="POST" class="auth-form">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                
                <div class="form-group has-icon-username">
                    <label for="username">Username</label>
                    <input 
                        type="text" 
                        id="username" 
                        name="username" 
                        placeholder="Enter your username"
                        required
                        autocomplete="username"
                    >
                </div>

                <div class="form-group has-icon-password">
                    <label for="password">Password</label>
                    <input 
                        type="password" 
                        id="password" 
                        name="password" 
                        placeholder="Enter your password"
                        required
                        autocomplete="current-password"
                    >
                </div>

                <div class="form-extras">
                    <div class="checkbox-wrapper">
                        <input type="checkbox" id="rememberMe" name="rememberMe">
                        <label for="rememberMe">Remember me</label>
                    </div>
                    <a href="${pageContext.request.contextPath}/forgot-password" class="forgot-link">Forgot Password?</a>
                </div>

                <button type="submit" class="auth-submit">
                    Sign In
                </button>

            </form>

            <!-- Footer -->
            <div class="auth-footer">
                <p>Don't have an account? <a href="${pageContext.request.contextPath}/register">Create Account</a></p>
            </div>
        </div>

        <!-- Back to Home 
        <div class="auth-back-home">
            <a href="index.jsp">
                ← Back to Home
            </a>
        </div>-->
    </div>

    <script>
        // Optional: Add loading state on form submit
        document.querySelector('.auth-form').addEventListener('submit', function(e) {
            const submitBtn = document.querySelector('.auth-submit');
            submitBtn.classList.add('loading');
        });
    </script>
</body>
</html>
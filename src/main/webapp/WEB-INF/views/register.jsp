<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
    
    <div class="auth-container">
        <div class="auth-card">
            <!-- Brand Section -->
            <div class="auth-brand">
                <div class="auth-brand-icon"></div>
                <h1>Create Account</h1>
                <p>Start tracking your expenses today</p>
            </div>

            <!-- Error Message (if validation failed) -->
            <c:if test="${not empty error}">
                <div class="auth-error">
                    ${error}
                </div>
            </c:if>

            <!-- Registration Form -->
            <form action="${pageContext.request.contextPath}/register" method="POST" class="auth-form">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                <div class="form-group has-icon-user">
                    <label for="fullname">Full Name</label>
                    <input
                        type="text"
                        id="fullname"
                        name="fullName"
                        placeholder="John Doe"
                        value="${registerRequest.fullName}"
                        required
                        autocomplete="name"
                    >
                </div>

                <div class="form-group has-icon-user">
                    <label for="username">Username</label>
                    <input
                        type="text"
                        id="username"
                        name="username"
                        placeholder="john_doe"
                        value="${registerRequest.username}"
                        required
                        autocomplete="username"
                    >
                </div>


                <div class="form-group has-icon-email">
                    <label for="email">Email Address</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        placeholder="your.email@example.com"
                        value="${registerRequest.email}"
                        required
                        autocomplete="email"
                    >
                </div>

                <div class="form-group has-icon-user">
                    <label for="phone">Phone</label>
                    <input
                        type="text"
                        id="phone"
                        name="phone"
                        placeholder="9876543210"
                        value="${registerRequest.phone}"
                        required
                        autocomplete="tel"
                    >
                </div>


                <div class="form-group has-icon-password">
                    <label for="password">Password</label>
                    <input 
                        type="password" 
                        id="password" 
                        name="password" 
                        placeholder="Create a strong password"
                        required
                        autocomplete="new-password"
                        minlength="8"
                    >
                </div>

                <div class="form-group has-icon-password">
                    <label for="confirm-password">Confirm Password</label>
                    <input 
                        type="password" 
                        id="confirm-password" 
                        name="confirmPassword" 
                        placeholder="Re-enter your password"
                        required
                        autocomplete="new-password"
                    >
                </div>

                <div class="form-extras">
                    <div class="checkbox-wrapper">
                        <input type="checkbox" id="terms" name="terms" required>
                        <label for="terms">I agree to Terms & Conditions</label>
                    </div>
                </div>

                <button type="submit" class="auth-submit">
                    Create Account
                </button>

            </form>

            <!-- Footer -->
            <div class="auth-footer">
                <p>Already have an account? <a href="${pageContext.request.contextPath}/login">Sign In</a></p>
            </div>
        </div>

        <!-- Back to Home
        <div class="auth-back-home">
            <a href="index.jsp">
                ← Back to Home
            </a>
        </div> -->
    </div>

    <script>
        // Form validation
        const form = document.querySelector('.auth-form');
        const password = document.getElementById('password');
        const confirmPassword = document.getElementById('confirm-password');

        form.addEventListener('submit', function(e) {
            // Check if passwords match
            if (password.value !== confirmPassword.value) {
                e.preventDefault();
                
                // Show error (you can create a proper error div)
                alert('Passwords do not match!');
                confirmPassword.focus();
                return false;
            }

            // Add loading state
            const submitBtn = document.querySelector('.auth-submit');
            submitBtn.classList.add('loading');
        });

        // Real-time password match indicator (optional enhancement)
        confirmPassword.addEventListener('input', function() {
            if (this.value !== password.value && this.value.length > 0) {
                this.style.borderColor = '#d9534f';
            } else if (this.value === password.value && this.value.length > 0) {
                this.style.borderColor = '#546236';
            } else {
                this.style.borderColor = '';
            }
        });
    </script>
</body>
</html>
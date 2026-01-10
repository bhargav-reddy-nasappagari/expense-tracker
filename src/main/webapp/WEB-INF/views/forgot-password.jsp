<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recover Account • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* --- Page Specific Styles --- */
        
        /* The Lock Icon Container */
        .lock-icon-container {
            width: 80px;
            height: 80px;
            background: #f0f2f5;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1.5rem;
            border: 2px solid #e1e4e8;
        }

        /* SVG Icon styling */
        .lock-icon {
            width: 40px;
            height: 40px;
            stroke: #4a5568;
            stroke-width: 2;
            fill: none;
            stroke-linecap: round;
            stroke-linejoin: round;
        }

        /* Instruction Text */
        .auth-description {
            color: #64748b;
            font-size: 0.95rem;
            line-height: 1.5;
            margin-bottom: 2rem;
            text-align: center;
        }

        /* Success Message Box (Green) */
        .alert-success {
            background-color: #d1e7dd;
            color: #0f5132;
            border: 1px solid #badbcc;
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1.5rem;
            text-align: center;
            font-size: 0.9rem;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
        }

        /* Back to Login Button styling */
        .back-to-login {
            display: block;
            text-align: center;
            margin-top: 1.5rem;
            color: #4a5568;
            text-decoration: none;
            font-size: 0.9rem;
            font-weight: 500;
            padding: 10px;
            border-radius: 6px;
            transition: background 0.2s;
        }
        .back-to-login:hover {
            background-color: #f8f9fa;
            color: #1a202c;
        }
    </style>
</head>
<body class="auth-page">
    
    <div class="auth-container">
        <div class="auth-card">
            
            <div class="auth-brand">
                <div class="lock-icon-container">
                    <svg class="lock-icon" viewBox="0 0 24 24">
                        <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                        <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                    </svg>
                </div>
                <h1>Trouble logging in?</h1>
                <p class="auth-description">
                    Enter your email, and we'll send you a secure link to get back into your account.
                </p>
            </div>

            <c:choose>
                <%-- Success State --%>
                <c:when test="${not empty message}">
                    <div class="alert-success">
                        <svg style="width:20px;height:20px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                            <polyline points="22 4 12 14.01 9 11.01"></polyline>
                        </svg>
                        <span>${message}</span>
                    </div>
                </c:when>

                <%-- Error State (e.g. System error) --%>
                <c:when test="${not empty error}">
                    <div class="auth-error">
                        ${error}
                    </div>
                </c:when>
            </c:choose>

            <%-- Only show form if we haven't just sent a success message (Optional UX choice), 
                 OR keep it to allow retry. Let's keep it for retry. --%>
            <form action="${pageContext.request.contextPath}/forgot-password" method="POST" class="auth-form" id="forgotPasswordForm" onsubmit="return handleSubmit(event)">
                
                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                
                <div class="form-group has-icon-email">
                    <label for="email">Email Address</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        placeholder="your.email@example.com"
                        required
                        autocomplete="email"
                        autofocus
                    >
                </div>

                <button type="submit" id="submitBtn" class="auth-submit">
                    Send Reset Link
                </button>

            </form>

            <a href="${pageContext.request.contextPath}/login" class="back-to-login">
                ← Back to Login
            </a>
            
        </div>
    </div>

    <script>
    let isSubmitting = false;

    function handleSubmit(event) {
        // Prevent double submission
        if (isSubmitting) {
            event.preventDefault();
            console.log("Form already submitting, preventing duplicate request");
            return false;
        }
        
        // Mark as submitting
        isSubmitting = true;
        
        // Disable the submit button
        const submitBtn = document.getElementById('submitBtn');
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.textContent = 'Sending...';
        submitBtn.classList.add('loading');
        
        // Re-enable after 5 seconds as a safety measure
        setTimeout(function() {
            isSubmitting = false;
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
            submitBtn.classList.remove('loading');
        }, 5000);
        
        return true;
    }

    // Also prevent multiple rapid clicks on the button itself
    document.addEventListener('DOMContentLoaded', function() {
        const form = document.getElementById('forgotPasswordForm');
        const submitBtn = document.getElementById('submitBtn');
        
        submitBtn.addEventListener('click', function(e) {
            if (isSubmitting) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            }
        });
    });
    </script>

</body>
</html>
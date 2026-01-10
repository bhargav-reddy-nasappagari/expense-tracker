<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Set New Password • ExpenseTracker</title>
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

        /* Error Message (Red) */
        .auth-error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1.5rem;
            text-align: center;
            font-size: 0.9rem;
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
                <h1>New Password</h1>
                <p class="auth-description">
                    Your identity is verified. Create a strong new password below.
                </p>
            </div>

            <c:if test="${not empty error}">
                <div class="auth-error">
                    ${error}
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/reset-password" method="POST" class="auth-form">
                
                <input type="hidden" name="csrfToken" value="${csrfToken}" />

                <input type="hidden" name="token" value="${token}">

                <div class="form-group has-icon-password">
                    <label for="password">New Password</label>
                    <input 
                        type="password" 
                        id="password" 
                        name="password" 
                        placeholder="Enter new password"
                        required 
                        minlength="8"
                        autofocus
                        autocomplete="new-password"
                    >
                </div>

                <div class="form-group has-icon-password">
                    <label for="confirmPassword">Confirm Password</label>
                    <input 
                        type="password" 
                        id="confirmPassword" 
                        name="confirmPassword" 
                        placeholder="Repeat new password"
                        required 
                        autocomplete="new-password"
                    >
                </div>

                <button type="submit" class="auth-submit">
                    Update Password
                </button>
            </form>

        </div>
    </div>

    <script>
        // UX: Basic client-side validation to prevent obvious mismatches before submit
        const form = document.querySelector('.auth-form');
        const p1 = document.getElementById('password');
        const p2 = document.getElementById('confirmPassword');

        form.addEventListener('submit', function(e) {
            if (p1.value !== p2.value) {
                e.preventDefault();
                alert("Passwords do not match!");
                return false;
            }
            
            // Visual feedback
            const btn = document.querySelector('.auth-submit');
            btn.innerHTML = 'Updating...';
            btn.style.opacity = '0.7';
            btn.style.cursor = 'wait';
        });
    </script>

</body>
</html>
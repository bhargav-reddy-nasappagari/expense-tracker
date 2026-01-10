<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify Email • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    
    <style>
        /* Page-Specific Styles for a "Creative" look */
        .verification-icon-container {
            margin: 0 auto 1.5rem;
            width: 80px;
            height: 80px;
            background: #f0fdf4; /* Light Green background */
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--olive-leaf, #546236);
            animation: pulse-green 2s infinite;
        }

        .email-highlight {
            background: #f8f9fa;
            border: 1px dashed #ced4da;
            padding: 12px 20px;
            border-radius: 8px;
            margin: 1.5rem 0;
            display: inline-block;
            max-width: 100%;
        }

        .email-text {
            color: var(--deep-space-blue, #333);
            font-weight: 600;
            font-size: 1.1rem;
            word-break: break-all;
        }

        .spam-notice {
            font-size: 0.9rem;
            color: #6c757d;
            margin-top: 1rem;
            line-height: 1.5;
        }

        @keyframes pulse-green {
            0% { box-shadow: 0 0 0 0 rgba(84, 98, 54, 0.2); }
            70% { box-shadow: 0 0 0 10px rgba(84, 98, 54, 0); }
            100% { box-shadow: 0 0 0 0 rgba(84, 98, 54, 0); }
        }
    </style>
</head>
<body class="auth-page">

    <div class="auth-container">
        <div class="auth-card" style="text-align: center; max-width: 450px;">
            
            <div class="verification-icon-container">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                    <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
            </div>

            <div class="auth-brand" style="margin-bottom: 0;">
                <h1 style="font-size: 1.75rem;">Check your Inbox</h1>
                <p style="margin-top: 0.5rem;">We've sent a verification link to:</p>
            </div>

            <div class="email-highlight">
                <span class="email-text">${email}</span>
            </div>

            <div style="padding: 0 10px;">
                <p style="margin-bottom: 5px;">
                    Click the link in the email to activate your account.
                </p>
                <p class="spam-notice">
                    Can't find it? Check your <strong>Spam</strong> or <strong>Junk</strong> folder.<br>
                    <small>The link expires in 24 hours.</small>
                </p>
            </div>

            <div class="auth-footer" style="margin-top: 2rem; border-top: 1px solid #eee; padding-top: 1.5rem;">
                <a href="${pageContext.request.contextPath}/login.jsp" class="btn-link" style="color: var(--olive-leaf, #546236); font-weight: bold; text-decoration: none;">
                    &larr; Back to Login
                </a>
            </div>
        </div>
    </div>

</body>
</html>
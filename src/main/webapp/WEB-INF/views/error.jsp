<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .error-container {
            min-height: calc(100vh - 200px);
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 2rem;
        }
        
        .error-content {
            text-align: center;
            max-width: 600px;
        }
        
        .error-icon {
            width: 120px;
            height: 120px;
            background: linear-gradient(135deg, var(--olive-leaf) 0%, var(--olive-hover) 100%);
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 2rem;
            box-shadow: 0 8px 24px rgba(84, 98, 54, 0.3);
            animation: pulse 2s ease-in-out infinite;
        }
        
        .error-icon::before {
            content: '!';
            font-size: 4rem;
            font-weight: 700;
            color: var(--white);
        }
        
        @keyframes pulse {
            0%, 100% {
                transform: scale(1);
                box-shadow: 0 8px 24px rgba(84, 98, 54, 0.3);
            }
            50% {
                transform: scale(1.05);
                box-shadow: 0 12px 32px rgba(84, 98, 54, 0.4);
            }
        }
        
        .error-title {
            font-size: 2.5rem;
            font-weight: 700;
            color: var(--deep-space-blue);
            margin-bottom: 1rem;
            letter-spacing: -0.5px;
        }
        
        .error-message {
            font-size: 1.1rem;
            color: var(--slate-grey);
            margin-bottom: 2rem;
            line-height: 1.6;
        }
        
        .error-details {
            background: rgba(217, 83, 79, 0.1);
            border-left: 4px solid #d9534f;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            text-align: left;
        }
        
        .error-details-title {
            font-weight: 700;
            color: #c9302c;
            margin-bottom: 0.5rem;
        }
        
        .error-details-text {
            color: var(--shadow-grey);
            font-size: 0.95rem;
            font-family: 'Courier New', monospace;
            word-break: break-word;
        }
        
        .error-actions {
            display: flex;
            gap: 1rem;
            justify-content: center;
            flex-wrap: wrap;
        }
        
        .btn-secondary {
            display: inline-block;
            padding: 0.95rem 2rem;
            background: var(--slate-grey);
            color: var(--white);
            font-weight: 600;
            font-size: 1rem;
            border: none;
            border-radius: 10px;
            cursor: pointer;
            text-decoration: none;
            text-align: center;
            transition: all 0.3s ease;
        }
        
        .btn-secondary:hover {
            background: #5d6374;
            transform: translateY(-2px);
        }
    </style>
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="error-container">
        <div class="error-content">
            <div class="error-icon"></div>
            
            <h1 class="error-title">Oops! Something went wrong</h1>
            
            <p class="error-message">
                We encountered an unexpected error while processing your request. 
                Don't worry, your data is safe!
            </p>
            
            <!-- Display error details if available -->
            <c:if test="${not empty error}">
                <div class="error-details">
                    <div class="error-details-title">Error Details:</div>
                    <div class="error-details-text">
                        <c:out value="${error}"/>
                    </div>
                </div>
            </c:if>
            
            <c:if test="${not empty exception}">
                <div class="error-details">
                    <div class="error-details-title">Technical Information:</div>
                    <div class="error-details-text">
                        <c:out value="${exception.message}"/>
                    </div>
                </div>
            </c:if>
            
            <!-- Action buttons -->
            <div class="error-actions">
                <a href="javascript:history.back()" class="btn-secondary">
                    ← Go Back
                </a>
                <a href="${pageContext.request.contextPath}/dashboard" class="btn-primary">
                    Go to Dashboard
                </a>
            </div>
            
            <p style="margin-top: 2rem; color: var(--slate-grey); font-size: 0.9rem;">
                If this problem persists, please contact support.
            </p>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

</body>
</html>
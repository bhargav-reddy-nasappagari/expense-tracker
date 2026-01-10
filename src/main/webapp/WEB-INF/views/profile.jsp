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
    <title>My Profile • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container" style="max-width: 900px;">

        <jsp:include page="/WEB-INF/views/layout/message.jsp"/>

        <div class="card">
            <!-- Profile Header -->
            <div class="profile-header">
                <div class="profile-avatar">
                    ${user.fullName != null && !user.fullName.isEmpty() ? 
                      user.fullName.substring(0, 1).toUpperCase() : 
                      user.username.substring(0, 1).toUpperCase()}
                </div>
                <div class="profile-name">
                    <c:choose>
                        <c:when test="${not empty user.fullName}">
                            ${user.fullName}
                        </c:when>
                        <c:otherwise>
                            ${user.username}
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="profile-meta">
                    @${user.username} • Member since 
                    ${user.createdAt.toLocalDate().getMonth().toString().substring(0,3)} 
                    ${user.createdAt.getYear()}
                </div>
            </div>

            <div class="card-body p-5">
                <!-- Tab Navigation -->
                <div class="section-tabs">
                    <button class="section-tab active" onclick="switchTab('info')">
                        Profile Information
                    </button>
                    <button class="section-tab" onclick="switchTab('edit')">
                        Edit Profile
                    </button>
                    <button class="section-tab" onclick="switchTab('password')">
                        Change Password
                    </button>
                </div>
                            <!-- Error Message (if validation failed) -->
            <c:if test="${not empty error}">
                <div class="auth-error">
                    ${error}
                </div>
            </c:if>

                <!-- Section 1: Profile Information (View Only) -->
                <div id="section-info" class="section-content active">
                    <h3 style="margin-bottom: 1.5rem; color: var(--deep-space-blue);">
                        Account Details
                    </h3>
                    
                    <div class="info-row">
                        <span class="info-label">Username</span>
                        <span class="info-value">${user.username}</span>
                    </div>
                    
                    <div class="info-row">
                        <span class="info-label">Full Name</span>
                        <span class="info-value">
                            <c:choose>
                                <c:when test="${not empty user.fullName}">
                                    ${user.fullName}
                                </c:when>
                                <c:otherwise>
                                    <em style="color: var(--slate-grey);">Not set</em>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    
                    <div class="info-row">
                        <span class="info-label">Email Address</span>
                        <span class="info-value">
                            <c:choose>
                                <c:when test="${not empty user.email}">
                                    ${user.email}
                                </c:when>
                                <c:otherwise>
                                    <em style="color: var(--slate-grey);">Not set</em>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    
                    <div class="info-row">
                        <span class="info-label">Phone Number</span>
                        <span class="info-value">
                            <c:choose>
                                <c:when test="${not empty user.phone}">
                                    ${user.phone}
                                </c:when>
                                <c:otherwise>
                                    <em style="color: var(--slate-grey);">Not set</em>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    
                    <div class="info-row">
                        <span class="info-label">Account Created</span>
                        <span class="info-value">${user.createdAt}</span>
                    </div>
                    
                    <div class="info-row">
                        <span class="info-label">Last Updated</span>
                        <span class="info-value">
                            <c:choose>
                                <c:when test="${not empty user.updatedAt}">
                                    ${user.updatedAt}
                                </c:when>
                                <c:otherwise>
                                    Never
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div style="text-align: center; margin-top: 2rem;">
                        <button onclick="switchTab('edit')" class="btn-primary">
                            Edit Profile Information
                        </button>
                    </div>
                </div>

                <!-- Section 2: Edit Profile -->
                <div id="section-edit" class="section-content">
                    <h3 style="margin-bottom: 1.5rem; color: var(--deep-space-blue);">
                        Edit Profile Information
                    </h3>
                    
                    <form action="${pageContext.request.contextPath}/profile" method="post">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                        <input type="hidden" name="action" value="updateProfile">
                        
                        <div class="form-group">
                            <label for="fullName">Full Name</label>
                            <input type="text" 
                                   id="fullName" 
                                   name="fullName" 
                                   class="form-control"
                                   value="${user.fullName}"
                                   maxlength="100"
                                   placeholder="Enter your full name">
                            <small style="color: var(--slate-grey); display: block; margin-top: 0.5rem;">
                                This is how your name will appear in the app
                            </small>
                        </div>

                        <div class="form-group">
                            <label for="email">Email Address</label>
                            <input type="email" 
                                   id="email" 
                                   name="email" 
                                   class="form-control"
                                   value="${user.email}"
                                   maxlength="100"
                                   placeholder="your.email@example.com">
                            <small style="color: var(--slate-grey); display: block; margin-top: 0.5rem;">
                                We'll never share your email with anyone else
                            </small>
                        </div>

                        <div class="form-group">
                            <label for="phone">Phone Number</label>
                            <input type="tel" 
                                   id="phone" 
                                   name="phone" 
                                   class="form-control"
                                   value="${user.phone}"
                                   maxlength="10"
                                   placeholder="10-digit number (no +91)">
                            <small style="color: var(--slate-grey); display: block; margin-top: 0.5rem;">
                                Enter 10 digits only (e.g., 9876543210)
                            </small>
                        </div>

                        <div class="alert" style="background: rgba(84, 98, 54, 0.1); border-left: 3px solid var(--olive-leaf); padding: 1rem; border-radius: 6px; margin-top: 1.5rem;">
                            <strong>Note:</strong> Your username cannot be changed. Contact support if you need to change it.
                        </div>

                        <div class="button-group">
                            <button type="button" onclick="switchTab('info')" class="btn-back">
                                Cancel
                            </button>
                            <button type="submit" class="btn-primary">
                                Save Changes
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Section 3: Change Password -->
                <div id="section-password" class="section-content">
                    <h3 style="margin-bottom: 1.5rem; color: var(--deep-space-blue);">
                        Change Password
                    </h3>
                    
                    <form action="${pageContext.request.contextPath}/profile" 
                          method="post" 
                          id="passwordChangeForm" 
                          onsubmit="return handlePasswordSubmit(event)">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                        <input type="hidden" name="action" value="changePassword">
                        
                        <div class="form-group">
                            <label for="currentPassword">Current Password *</label>
                            <input type="password" 
                                   id="currentPassword" 
                                   name="currentPassword" 
                                   class="form-control"
                                   required
                                   placeholder="Enter your current password">
                        </div>

                        <div class="form-group">
                            <label for="newPassword">New Password *</label>
                            <input type="password" 
                                   id="newPassword" 
                                   name="newPassword" 
                                   class="form-control"
                                   required
                                   placeholder="Enter new password">
                        </div>

                        <div class="form-group">
                            <label for="confirmPassword">Confirm New Password *</label>
                            <input type="password" 
                                   id="confirmPassword" 
                                   name="confirmPassword" 
                                   class="form-control"
                                   required
                                   placeholder="Re-enter new password">
                        </div>

                        <div class="password-requirements">
                            <strong>Password Requirements:</strong>
                            <ul>
                                <li>At least 8 characters long</li>
                                <li>Contains at least one uppercase letter (A-Z)</li>
                                <li>Contains at least one lowercase letter (a-z)</li>
                                <li>Contains at least one number (0-9)</li>
                                <li>Contains at least one special character (!@#$%^&*)</li>
                            </ul>
                        </div>

                        <div class="alert alert-danger" style="display: none; margin-top: 1.5rem;" id="passwordError"></div>

                        <div class="alert" style="background: rgba(217, 83, 79, 0.1); border-left: 3px solid #d9534f; padding: 1rem; border-radius: 6px; margin-top: 1.5rem;">
                            <strong>⚠️ Security Notice:</strong> You will be logged out after changing your password and will need to log in again with your new password.
                        </div>

                        <div class="button-group">
                            <button type="button" onclick="switchTab('info')" class="btn-back">
                                Cancel
                            </button>
                            <button type="submit" class="btn-primary">
                                Change Password
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="text-center mt-5">
            <p class="tagline fs-4">Your personal finance. Your rules. Your masterpiece.</p>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

<!-- Password Change Confirmation Modal -->
<div id="passwordChangeModal" class="modal-overlay" style="display: none;">
    <div class="modal">
        <h3 class="modal-title">⚠️ Confirm Password Change</h3>
        <p class="modal-text">
            You are about to change your password. After this change:
        </p>
        <ul style="text-align: left; margin: 1rem 0 1.5rem 2rem; color: var(--shadow-grey);">
            <li>You will be logged out immediately</li>
            <li>You'll need to log in again with your new password</li>
            <li>All other active sessions will be terminated</li>
        </ul>
        <p class="modal-text" style="font-weight: 600; color: var(--deep-space-blue);">
            Are you sure you want to continue?
        </p>
        
        <div class="modal-actions">
            <button class="btn-secondary modal-cancel" onclick="closePasswordModal()">
                Cancel
            </button>
            <button class="btn-primary modal-delete" onclick="confirmPasswordChange()" style="background: var(--olive-leaf);">
                Yes, Change Password
            </button>
        </div>
    </div>
</div>

<script>
// Tab switching
function switchTab(tabName) {
    // Hide all sections
    document.querySelectorAll('.section-content').forEach(function(section) {
        section.classList.remove('active');
    });
    
    // Remove active class from all tabs
    document.querySelectorAll('.section-tab').forEach(function(tab) {
        tab.classList.remove('active');
    });
    
    // Show selected section
    document.getElementById('section-' + tabName).classList.add('active');
    
    // Activate corresponding tab
    event.target.classList.add('active');
}

// Password validation (client-side only)
function validatePasswordForm() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const errorDiv = document.getElementById('passwordError');
    
    // Check if passwords match
    if (newPassword !== confirmPassword) {
        errorDiv.textContent = 'New passwords do not match!';
        errorDiv.style.display = 'block';
        document.getElementById('confirmPassword').focus();
        return false;
    }
    
    // Check password strength (basic client-side validation)
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;
    if (!passwordRegex.test(newPassword)) {
        errorDiv.textContent = 'Password does not meet the requirements. Please check the requirements below.';
        errorDiv.style.display = 'block';
        document.getElementById('newPassword').focus();
        return false;
    }
    
    // All validations passed
    errorDiv.style.display = 'none';
    return true;
}

// Handle password form submission with modal confirmation
function handlePasswordSubmit(event) {
    // Prevent default form submission
    event.preventDefault();
    
    // First validate the form
    if (!validatePasswordForm()) {
        return false;
    }
    
    // Show confirmation modal
    document.getElementById('passwordChangeModal').style.display = 'flex';
    return false; // Don't submit yet
}

// Confirm password change (called when user clicks "Yes" in modal)
function confirmPasswordChange() {
    // Close modal
    closePasswordModal();
    
    // Submit the form
    document.getElementById('passwordChangeForm').submit();
}

// Close password change modal
function closePasswordModal() {
    document.getElementById('passwordChangeModal').style.display = 'none';
}

// Close modal when clicking outside
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('passwordChangeModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closePasswordModal();
            }
        });
    }
    
    // Close modal on Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closePasswordModal();
        }
    });
});

// Auto-switch to correct tab on page load (if redirected with error)
(function() {
    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get('tab');
    
    if (tab === 'edit') {
        switchTabDirect('edit');
    } else if (tab === 'password') {
        switchTabDirect('password');
    }
})();

// Helper function for tab switching without event object
function switchTabDirect(tabName) {
    // Hide all sections
    document.querySelectorAll('.section-content').forEach(function(section) {
        section.classList.remove('active');
    });
    
    // Remove active class from all tabs
    document.querySelectorAll('.section-tab').forEach(function(tab) {
        tab.classList.remove('active');
    });
    
    // Show selected section
    document.getElementById('section-' + tabName).classList.add('active');
    
    // Activate corresponding tab button
    const tabs = document.querySelectorAll('.section-tab');
    if (tabName === 'info') tabs[0].classList.add('active');
    else if (tabName === 'edit') tabs[1].classList.add('active');
    else if (tabName === 'password') tabs[2].classList.add('active');
}
</script>

</body>
</html>
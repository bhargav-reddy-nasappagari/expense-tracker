package com.expensetracker.service;

import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.http.HttpSession;

public class UserService {

    private final UserRepository userRepo;
    private final CategoryRepository catRepo;

    private static final String[] DEFAULT_CATEGORIES = {
        "Food", "Transport", "Entertainment", "Shopping", "Bills", "Health", "Other"
    };

    // Add constructor for dependency injection
    public UserService(UserRepository userRepo, CategoryRepository catRepo) {
        this.userRepo = userRepo;
        this.catRepo = catRepo;
    }

    // Keep default constructor for backward compatibility
    public UserService() {
        this(new UserRepository(), new CategoryRepository());
    }

    public User register(String username, String password, String fullName, String email, String phone) {
        username = ValidationService.validateAndNormalizeUsername(username);
        ValidationService.validatePassword(password);
        fullName = ValidationService.normalizeFullName(fullName);
        email = ValidationService.validateAndNormalizeEmail(email);
        phone = ValidationService.validateAndNormalizePhone(phone);

        // Check uniqueness
        if (userRepo.findByUsernameIgnoreCase(username).isPresent())
            throw new IllegalArgumentException("Username already taken");
        if (email != null && userRepo.findByEmailIgnoreCase(email).isPresent())
            throw new IllegalArgumentException("Email already registered");

        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(12)));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);

        TransactionManager.executeInTransaction(conn -> {
            User saved = userRepo.save(user);
            for (String name : DEFAULT_CATEGORIES) {
                Category cat = new Category(name, saved.getId(), true);
                catRepo.save(cat);
            }
        });

        return user; 

    }

    /**
     * Authenticate a user.
     * UPDATED: 
     * 1. Removed HttpSession (The Servlet now handles that).
     * 2. Added "Grandfather Clause" verification check.
     */
    public User login(String username, String password) {
        
        // 1. Validation
        username = ValidationService.validateAndNormalizeUsername(username);
        
        // 2. Find User
        User user = userRepo.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // 3. Check Password
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // ================= NEW GATEKEEPER LOGIC =================
        // Logic: Allow login ONLY IF (Email is Verified) OR (User is Legacy/Grandfathered)
        boolean isVerified = user.isEmailVerified();
        boolean isLegacy   = user.isLegacyUnverified();

        if (!isVerified && !isLegacy) {
            // This specific exception is what LoginServlet catches to show the "Check Email" error
            throw new IllegalStateException("Email not verified. Please check your inbox.");
        }
        // ========================================================
        
        // 4. Return the User
        // The LoginServlet will take this object and put it into the Session.
        return user;
    }

    /**
     * Get user by ID (for profile display)
     */
    public User getUserById(Long userId) {
        return userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * Update user profile (name, email, phone)
     * Validates all fields and checks email uniqueness
     */
    public void updateProfile(Long userId, String fullName, String email, String phone) {
        // Get existing user
        User user = getUserById(userId);
        
        // Validate inputs
        fullName = ValidationService.normalizeFullName(fullName);
        email = ValidationService.validateAndNormalizeEmail(email);
        phone = ValidationService.validateAndNormalizePhone(phone);
        
        // Check if email is taken by another user
        if (email != null && !email.equalsIgnoreCase(user.getEmail())) {
            if (userRepo.isEmailTakenByAnotherUser(email, userId)) {
                throw new IllegalArgumentException("Email already in use by another account");
            }
        }
        
        // Update user object
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        
        // Save to database
        userRepo.updateProfile(user);
    }

    /**
     * Change user password with security validation
     * Requires current password verification
     */
    public void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        // Validate inputs
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Current password is required");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password confirmation is required");
        }
        
        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New passwords do not match");
        }
        
        // Get user from database
        User user = getUserById(userId);
        
        // Verify current password
        if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password strength
        ValidationService.validatePassword(newPassword);
        
        // Check if new password is different from current
        if (BCrypt.checkpw(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
        
        // Hash and update password
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        userRepo.updatePassword(userId, hashedPassword);
    }

    /**
     * Re-generates a token and sends the email for an existing user.
     */
    public void resendVerification(String username) {
        // 1. Find User
        User user = userRepo.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Security Check: Don't resend if already verified
        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("User is already verified.");
        }

        // 3. Generate NEW Token
        String token = com.expensetracker.util.TokenUtil.generateToken();
        String tokenHash = com.expensetracker.util.TokenUtil.hashToken(token);

        // 4. Update DB
        user.setVerificationTokenHash(tokenHash);
        user.setTokenCreatedAt(java.time.LocalDateTime.now());
        userRepo.updateVerificationStatus(user);

        // 5. Send Email
        com.expensetracker.util.EmailUtil.sendVerificationEmail(user.getEmail(), token);
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}
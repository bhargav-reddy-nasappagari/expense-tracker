## 🧪 Writing Tests - Component Guide

This section provides detailed descriptions of each component to help write comprehensive JUnit tests.

---

## 📖 Component Testing Guide

### **1. Service Layer**

#### **`ValidationService.java`**

**Purpose**: Centralized input validation for all user inputs.

**Methods to Test**:
```java
// Username Validation
public static String validateAndNormalizeUsername(String username)
// Rules:
// - 4-30 characters
// - Must start with letter or underscore
// - Only letters, digits, underscores
// - Returns lowercase normalized username
// - Throws IllegalArgumentException on invalid input

// Password Validation
public static String validatePassword(String password)
// Rules:
// - Minimum 8 characters
// - At least 1 uppercase letter (A-Z)
// - At least 1 lowercase letter (a-z)
// - At least 1 digit (0-9)
// - At least 1 special character (!@#$%^&*)
// - Throws IllegalArgumentException on invalid input

// Email Validation
public static String validateAndNormalizeEmail(String email)
// Rules:
// - Valid email format (RFC 5322)
// - Returns lowercase normalized email
// - Throws IllegalArgumentException on invalid format

// Phone Validation
public static String validateAndNormalizePhone(String phone)
// Rules:
// - Exactly 10 digits
// - Removes non-digit characters
// - Returns normalized 10-digit string
// - Throws IllegalArgumentException on invalid length

// Amount Validation
public static BigDecimal validateAndRoundAmount(BigDecimal amount)
// Rules:
// - Must be greater than 0
// - Rounds to 2 decimal places (HALF_UP)
// - Throws IllegalArgumentException if <= 0

// Date Validation
public static LocalDate validateExpenseDate(LocalDate date)
// Rules:
// - Cannot be null
// - Cannot be in the future
// - Returns the validated date
// - Throws IllegalArgumentException on violation
```

**Test Examples**:
```java
@Test
void testValidUsername() {
    String result = ValidationService.validateAndNormalizeUsername("john_doe123");
    assertEquals("john_doe123", result);
}

@Test
void testUsernameWithUpperCase() {
    String result = ValidationService.validateAndNormalizeUsername("JohnDoe");
    assertEquals("johndoe", result); // Normalized to lowercase
}

@Test
void testUsernameTooShort() {
    assertThrows(IllegalArgumentException.class, () -> {
        ValidationService.validateAndNormalizeUsername("abc");
    });
}

@Test
void testUsernameWithInvalidChars() {
    assertThrows(IllegalArgumentException.class, () -> {
        ValidationService.validateAndNormalizeUsername("john-doe"); // Hyphen not allowed
    });
}

@Test
void testValidPassword() {
    String result = ValidationService.validatePassword("SecurePass123!");
    assertEquals("SecurePass123!", result);
}

@Test
void testPasswordMissingUppercase() {
    assertThrows(IllegalArgumentException.class, () -> {
        ValidationService.validatePassword("weakpass123!");
    });
}

@Test
void testValidEmail() {
    String result = ValidationService.validateAndNormalizeEmail("John.Doe@Example.COM");
    assertEquals("john.doe@example.com", result);
}

@Test
void testInvalidEmailFormat() {
    assertThrows(IllegalArgumentException.class, () -> {
        ValidationService.validateAndNormalizeEmail("not-an-email");
    });
}

@Test
void testValidPhone() {
    String result = ValidationService.validateAndNormalizePhone("(123) 456-7890");
    assertEquals("1234567890", result);
}

@Test
void testPhoneWithInvalidLength() {
    assertThrows(IllegalArgumentException.class, () -> {
        ValidationService.validateAndNormalizePhone("12345"); // Only 5 digits
    });
}

@Test
void testValidAmount() {
    BigDecimal result = ValidationService.validateAndRoundAmount(new BigDecimal("123.456"));
    assertEquals(new BigDecimal("123.46"), result); // Rounded to 2 decimals
}

@Test
void testZeroAmount() {
assertThrows(IllegalArgumentException.class, () -> {
ValidationService.validateAndRoundAmount(BigDecimal.ZERO);
});
}

@Test
void testFutureDate() {
LocalDate futureDate = LocalDate.now().plusDays(1);
assertThrows(IllegalArgumentException.class, () -> {
ValidationService.validateExpenseDate(futureDate);
});
}

---

#### **`UserService.java`**

**Purpose**: User authentication, registration, and profile management.

**Methods to Test**:
```java
// User Registration
public User register(String username, String password, String fullName, 
                     String email, String phone)
// Behavior:
// 1. Validates all inputs using ValidationService
// 2. Checks username uniqueness
// 3. Checks email uniqueness
// 4. Hashes password with BCrypt
// 5. Creates 7 default categories
// 6. Uses TransactionManager for atomicity
// 7. Returns User with generated ID
// Throws:
// - IllegalArgumentException: Validation failure, duplicate username/email

// User Login
public User login(String username, String password)
// Behavior:
// 1. Validates username format
// 2. Fetches user by username (case-insensitive)
// 3. Verifies password with BCrypt
// 4. Checks email verification status (if not legacy)
// 5. Returns authenticated User
// Throws:
// - IllegalArgumentException: Invalid credentials
// - IllegalStateException: Email not verified

// Profile Update
public void updateProfile(Long userId, String fullName, String email, String phone)
// Behavior:
// 1. Validates all inputs
// 2. Checks email uniqueness (excluding current user)
// 3. Updates user record
// Throws:
// - IllegalArgumentException: Validation or duplicate email

// Password Change
public void changePassword(Long userId, String currentPassword, 
                           String newPassword, String confirmPassword)
// Behavior:
// 1. Validates current password
// 2. Checks new passwords match
// 3. Validates new password strength
// 4. Ensures new password differs from current
// 5. Hashes and updates password
// Throws:
// - IllegalArgumentException: Validation or mismatch errors

// Resend Verification
public void resendVerification(String username)
// Behavior:
// 1. Finds user by username
// 2. Checks if already verified
// 3. Generates new verification token
// 4. Sends verification email
// Throws:
// - IllegalArgumentException: User not found or already verified
```

**Test Examples**:
```java
@Test
void testSuccessfulRegistration() {
    User user = userService.register("testuser", "SecurePass123!", 
                                      "Test User", "test@example.com", "1234567890");
    assertNotNull(user.getId());
    assertEquals("testuser", user.getUsername());
    assertTrue(BCrypt.checkpw("SecurePass123!", user.getPassword()));
}

@Test
void testRegistrationWithDuplicateUsername() {
    userService.register("duplicate", "Pass123!", "User1", "u1@ex.com", "1111111111");
    
    assertThrows(IllegalArgumentException.class, () -> {
        userService.register("duplicate", "Pass456!", "User2", "u2@ex.com", "2222222222");
    });
}

@Test
void testSuccessfulLogin() {
    userService.register("logintest", "SecurePass123!", "Test", "l@ex.com", "1234567890");
    
    User user = userService.login("logintest", "SecurePass123!");
    assertNotNull(user);
    assertEquals("logintest", user.getUsername());
}

@Test
void testLoginWithWrongPassword() {
    userService.register("wrongpass", "CorrectPass123!", "Test", "w@ex.com", "1234567890");
    
    assertThrows(IllegalArgumentException.class, () -> {
        userService.login("wrongpass", "WrongPass123!");
    });
}

@Test
void testUpdateProfile() {
    User user = userService.register("profile", "Pass123!", "Old Name", "old@ex.com", "1111111111");
    
    userService.updateProfile(user.getId(), "New Name", "new@ex.com", "2222222222");
    
    User updated = userService.getUserById(user.getId());
    assertEquals("New Name", updated.getFullName());
    assertEquals("new@ex.com", updated.getEmail());
}

@Test
void testChangePasswordWithWrongCurrent() {
    User user = userService.register("passchange", "OldPass123!", "Test", "pc@ex.com", "1234567890");
    
    assertThrows(IllegalArgumentException.class, () -> {
        userService.changePassword(user.getId(), "WrongPass123!", "NewPass123!", "NewPass123!");
    });
}
```

---

#### **`ExpenseService.java`**

**Purpose**: Expense CRUD operations with validation and authorization.

**Methods to Test**:
```java
// Add Expense
public Expense addExpense(Long userId, String description, BigDecimal amount,
                          Integer categoryId, LocalDate expenseDate)
// Behavior:
// 1. Validates description (1-255 chars)
// 2. Validates amount (>0, rounds to 2 decimals)
// 3. Validates date (not future)
// 4. Verifies category ownership
// 5. Creates and saves expense
// Throws:
// - IllegalArgumentException: Invalid input or unauthorized category

// Edit Expense
public Expense editExpense(Long userId, Long expenseId, String description,
                           BigDecimal amount, Integer categoryId, LocalDate expenseDate)
// Behavior:
// 1. Validates all inputs
// 2. Verifies expense ownership
// 3. Verifies category ownership
// 4. Updates expense
// Throws:
// - IllegalArgumentException: Invalid input, unauthorized access

// Delete Expense
public void deleteExpense(Long userId, Long expenseId)
// Behavior:
// 1. Verifies expense ownership
// 2. Deletes expense
// Throws:
// - IllegalArgumentException: Expense not found or not owned

// List Expenses (Paginated)
public PagedResult<Expense> listExpensesPaginated(Long userId, LocalDate from, 
                                                   LocalDate to, Integer categoryId,
                                                   BigDecimal minAmount, BigDecimal maxAmount,
                                                   String keyword, int page, int pageSize,
                                                   String sortBy, String sortOrder)
// Behavior:
// 1. Applies filters (date, category, amount range, keyword)
// 2. Applies sorting (date/amount/description/category)
// 3. Returns paginated results with metadata
// 4. Page numbers are 1-indexed
// 5. Page size clamped to 5-100
```

**Test Examples**:
```java
@Test
void testAddValidExpense() {
    Expense expense = expenseService.addExpense(userId, "Groceries", 
                                                 new BigDecimal("50.00"), categoryId, 
                                                 LocalDate.now());
    assertNotNull(expense.getId());
    assertEquals("Groceries", expense.getDescription());
    assertEquals(new BigDecimal("50.00"), expense.getAmount());
}

@Test
void testAddExpenseWithUnauthorizedCategory() {
    // Create category for different user
    Integer otherCategoryId = categoryService.addCategory(otherUserId, "Other").getId();
    
    assertThrows(IllegalArgumentException.class, () -> {
        expenseService.addExpense(userId, "Test", new BigDecimal("10"), otherCategoryId, LocalDate.now());
    });
}

@Test
void testEditExpenseWithOwnershipCheck() {
    Expense expense = expenseService.addExpense(userId, "Original", new BigDecimal("10"), 
                                                 categoryId, LocalDate.now());
    
    // Try to edit with different userId
    assertThrows(IllegalArgumentException.class, () -> {
        expenseService.editExpense(otherUserId, expense.getId(), "Hacked", 
                                    new BigDecimal("100"), categoryId, LocalDate.now());
    });
}

@Test
void testPaginationFirstPage() {
    // Create 25 expenses
    for (int i = 0; i < 25; i++) {
        expenseService.addExpense(userId, "Expense " + i, new BigDecimal("10"), 
                                   categoryId, LocalDate.now());
    }
    
    PagedResult<Expense> result = expenseService.listExpensesPaginated(userId, null, null, 
                                                                         null, null, null, null,
                                                                         1, 10, "date", "desc");
    assertEquals(10, result.getItems().size());
    assertEquals(1, result.getCurrentPage());
    assertEquals(3, result.getTotalPages());
    assertEquals(25, result.getTotalItems());
}
```

---

#### **`BudgetService.java`**

**Purpose**: Budget management with validation and spending tracking.

**Methods to Test**:
```java
// Create Budget
public Budget createBudget(Long userId, Integer categoryId, BigDecimal amount,
                           LocalDate periodStart, LocalDate periodEnd, boolean recurring)
// Behavior:
// 1. Validates amount > 0
// 2. Validates periodEnd >= periodStart (if provided)
// 3. Verifies category ownership
// 4. Checks for duplicate (same category + start date)
// 5. Creates budget
// Throws:
// - IllegalArgumentException: Validation or duplicate

// Get Active Budgets
public List<Budget> getActiveBudgets(Long userId)
// Behavior:
// 1. Fetches active budgets with category names
// 2. Calculates spent amount for each budget
// 3. Calculates percentage used
// 4. Returns enriched budgets

// Update Budget
public void updateBudget(Long userId, Long budgetId, BigDecimal amount,
                         LocalDate periodEnd, boolean recurring)
// Behavior:
// 1. Verifies budget ownership
// 2. Validates amount (if provided)
// 3. Validates date range
// 4. Updates budget
// Throws:
// - IllegalArgumentException: Invalid input or unauthorized

// Delete Budget
public void deleteBudget(Long userId, Long budgetId)
// Behavior:
// 1. Verifies budget ownership
// 2. Soft deletes budget (sets is_active = false)
// Throws:
// - IllegalArgumentException: Budget not found or not owned
```

**Test Examples**:
```java
@Test
void testCreateValidBudget() {
    Budget budget = budgetService.createBudget(userId, categoryId, new BigDecimal("1000"),
                                                LocalDate.now().withDayOfMonth(1), null, true);
    assertNotNull(budget.getId());
    assertEquals(new BigDecimal("1000.00"), budget.getAmount());
    assertTrue(budget.isRecurring());
}

@Test
void testCreateDuplicateBudget() {
    LocalDate start = LocalDate.now().withDayOfMonth(1);
    budgetService.createBudget(userId, categoryId, new BigDecimal("1000"), start, null, true);
    
    assertThrows(IllegalArgumentException.class, () -> {
        budgetService.createBudget(userId, categoryId, new BigDecimal("2000"), start, null, false);
    });
}

@Test
void testGetActiveBudgetsWithSpentCalculation() {
    // Create budget
    Budget budget = budgetService.createBudget(userId, categoryId, new BigDecimal("1000"),
                                                LocalDate.now().withDayOfMonth(1), 
                                                LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()), 
                                                false);
    
    // Add expense
    expenseService.addExpense(userId, "Test", new BigDecimal("250"), categoryId, LocalDate.now());
    
    // Fetch budgets
    List<Budget> budgets = budgetService.getActiveBudgets(userId);
    Budget retrieved = budgets.stream().filter(b -> b.getId().equals(budget.getId())).findFirst().get();
    
    assertEquals(new BigDecimal("250.00"), retrieved.getSpentAmount());
    assertEquals(25.0, retrieved.getPercentageUsed(), 0.01);
}
```

---

#### **`ReportService.java`**

**Purpose**: Complex analytics, trend analysis, and report generation.

**Key Methods**:
```java
// Generate Custom Report
public ReportData generateCustomReport(Long userId, LocalDate startDate, 
                                        LocalDate endDate, Integer categoryId)
// Returns: ReportData with:
// - Total spending
// - Average per day/week/month
// - Category breakdown (top categories with percentages)
// - Top 10 expenses
// - Daily trend data
// - Day-of-week distribution
// - Period comparison (vs previous period)

// Generate Monthly Trend Data
public List<MonthlyTrend> generateMonthlyTrendData(Long userId, int numberOfMonths)
// Returns: List of MonthlyTrend objects with:
// - Month label
// - Total spending
// - Category breakdown
// - Change vs previous month (amount + percentage)

// Generate Heatmap Data
public Map<LocalDate, DaySpending> generateHeatmapData(Long userId, int year, int month)
// Returns: Map of date to DaySpending with:
// - Amount spent
// - Transaction count
// - Color level (none/low/medium/high/very-high)

// Generate Category Performance Report
public List<CategoryPerformance> generateCategoryPerformanceReport(Long userId, 
                                                                     LocalDate start, 
                                                                     LocalDate end)
// Returns: List of CategoryPerformance with:
// - Category name
// - Total spent
// - Budget allocated/remaining/used%
// - Transaction count + average
// - Previous period spent + change%
// - Percentage of total
```

**Test Examples**:
```java
@Test
void testGenerateReportWithNoData() {
    ReportData report = reportService.generateCustomReport(userId, 
                                                             LocalDate.now().minusDays(30),
                                                             LocalDate.now(), null);
    assertEquals(BigDecimal.ZERO, report.getTotalSpending());
    assertEquals(0, report.getTransactionCount());
    assertTrue(report.getCategoryBreakdown().isEmpty());
}

@Test
void testGenerateReportWithData() {
    // Create expenses
    expenseService.addExpense(userId, "Expense 1", new BigDecimal("100"), categoryId, LocalDate.now());
    expenseService.addExpense(userId, "Expense 2", new BigDecimal("200"), categoryId, LocalDate.now());
    
    ReportData report = reportService.generateCustomReport(userId, LocalDate.now(), LocalDate.now(), null);
    
    assertEquals(new BigDecimal("300.00"), report.getTotalSpending());
    assertEquals(2, report.getTransactionCount());
    assertFalse(report.getCategoryBreakdown().isEmpty());
}

@Test
void testHeatmapDataGeneration() {
    // Add expenses on different days
    expenseService.addExpense(userId, "Day 1", new BigDecimal("50"), categoryId, LocalDate.now().withDayOfMonth(1));
    expenseService.addExpense(userId, "Day 15", new BigDecimal("200"), categoryId, LocalDate.now().withDayOfMonth(15));
    
    Map<LocalDate, DaySpending> heatmap = reportService.generateHeatmapData(userId, 
                                                                              LocalDate.now().getYear(), 
                                                                              LocalDate.now().getMonthValue());
    
    LocalDate day1 = LocalDate.now().withDayOfMonth(1);
    LocalDate day15 = LocalDate.now().withDayOfMonth(15);
    
    assertEquals(new BigDecimal("50.00"), heatmap.get(day1).getAmount());
    assertEquals("low", heatmap.get(day1).getColorLevel());
    
    assertEquals(new BigDecimal("200.00"), heatmap.get(day15).getAmount());
    assertEquals("high", heatmap.get(day15).getColorLevel());
}
```

---

### **2. Repository Layer**

#### **`UserRepository.java`**

**Purpose**: User data access with authentication helpers.

**Methods to Test**:
```java
// Basic CRUD
public User save(User user)                    // Insert new user, returns with ID
public Optional<User> findById(Long id)        // Find by primary key
public Optional<User> findByUsernameIgnoreCase(String username)
public Optional<User> findByEmailIgnoreCase(String email)

// Authentication Tokens
public void updateRememberToken(Long userId, String tokenHash, LocalDateTime expiresAt)
public Optional<User> findByRememberToken(String tokenHash)  // Only returns if not expired

public void updateResetToken(Long userId, String tokenHash, LocalDateTime expiresAt)
public Optional<User> findByValidResetToken(String tokenHash)  // Only if not expired

public void updateVerificationStatus(User user)
public Optional<User> findByTokenHash(String tokenHash)

// Profile Management
public void updateProfile(User user)
public void updatePassword(Long userId, String hashedPassword)
public boolean isEmailTakenByAnotherUser(String email, Long excludeUserId)
```

**Test Examples**:
```java
@Test
void testSaveUserReturnsGeneratedId() {
    User user = new User();
    user.setUsername("testuser");
    user.setPassword("hashed");
    user.setEmail("test@example.com");
    
    User saved = userRepository.save(user);
    assertNotNull(saved.getId());
    assertTrue(saved.getId() > 0);
}

@Test
void testFindByUsernameCaseInsensitive() {
    User user = new User();
    user.setUsername("JohnDoe");
    user.setPassword("hashed");
    userRepository.save(user);
    
    Optional<User> found = userRepository.findByUsernameIgnoreCase("johndoe");
    assertTrue(found.isPresent());
    assertEquals("JohnDoe", found.get().getUsername());
}

@Test
void testRememberTokenExpiry() {
    User user = userRepository.save(createTestUser());
    String token = "hashed_token";
    LocalDateTime expired = LocalDateTime.now().minusDays(1);
    
    userRepository.updateRememberToken(user.getId(), token, expired);
    
    Optional<User> found = userRepository.findByRememberToken(token);
    assertFalse(found.isPresent()); // Expired token should not be found
}
```

---

#### **`ExpenseRepository.java`**

**Purpose**: Expense data access with advanced filtering and pagination.

**Methods to Test**:
```java
// Basic CRUD
public Expense save(Expense expense)
public Optional<Expense> findByIdAndUserId(Long expenseId, Long userId)
public Expense update(Expense expense)
public void delete(Long expenseId, Long userId)

// Filtering & Pagination
public PagedResult<Expense> findByUserIdAndFiltersPaginated(
    Long userId, LocalDate from, LocalDate to, Integer categoryId,
    BigDecimal minAmount, BigDecimal maxAmount, String keyword,
    int page, int pageSize, String sortBy, String sortOrder
)

// Aggregations
public BigDecimal sumAmountByCategoryAndDateRange(Long userId, Integer categoryId, 
                                                    LocalDate from, LocalDate to)
public Map<LocalDate, BigDecimal> calculateDailyTotals(Long userId, LocalDate start, LocalDate end)
public Map<String, BigDecimal> calculateDayOfWeekDistribution(Long userId, LocalDate start, LocalDate end)
public List<Expense> findTopExpensesByAmount(Long userId, LocalDate start, LocalDate end, int limit)
```

**Test Examples**:
```java
@Test
void testSaveExpense() {
    Expense expense = new Expense();
    expense.setUserId(userId);
    expense.setDescription("Test");
    expense.setAmount(new BigDecimal("50.00"));
    expense.setCategoryId(categoryId);
    expense.setExpenseDate(LocalDate.now());
    
    Expense saved = expenseRepository.save(expense);
    assertNotNull(saved.getId());
}

@Test
void testFilterByDateRange() {
    LocalDate start = LocalDate.now().minusDays(10);
    LocalDate end = LocalDate.now();
    
    // Create expenses outside range
    Expense old = createExpense(userId, LocalDate.now().minusDays(20));
    Expense inRange = createExpense(userId, LocalDate.now().minusDays(5));
    
    List<Expense> results = expenseRepository.findByUserIdAndFilters(userId, start, end, 
                                                                       null, null, null, null);
    
    assertEquals(1, results.size());
    assertEquals(inRange.getId(), results.get(0).getId());
}

@Test
void testPaginationMetadata() {
    // Create 25 expenses
    for (int i = 0; i < 25; i++) {
        createExpense(userId, LocalDate.now());
    }
    
    PagedResult<Expense> page1 = expenseRepository.findByUserIdAndFiltersPaginated(
        userId, null, null, null, null, null, null, 1, 10, "date", "desc"
    );
    
    assertEquals(10, page1.getItems().size());
    assertEquals(1, page1.getCurrentPage());
    assertEquals(3, page1.getTotalPages());
    assertEquals(25, page1.getTotalItems());
    assertTrue(page1.hasNext());
    assertFalse(page1.hasPrevious());
}

@Test
void testSortingByAmount() {
    createExpense(userId, new BigDecimal("100"), LocalDate.now());
    createExpense(userId, new BigDecimal("50"), LocalDate.now());
    createExpense(userId, new BigDecimal("75"), LocalDate.now());
    
    PagedResult<Expense> ascending = expenseRepository.findByUserIdAndFiltersPaginated(
        userId, null, null, null, null, null, null, 1, 10, "amount", "asc"
    );
    
    assertEquals(new BigDecimal("50.00"), ascending.getItems().get(0).getAmount());
    assertEquals(new BigDecimal("75.00"), ascending.getItems().get(1).getAmount());
    assertEquals(new BigDecimal("100.00"), ascending.getItems().get(2).getAmount());
}
```

---

### **3. Utility Classes**

#### **`TokenUtil.java`**

**Purpose**: Token generation, hashing, and expiry validation.

**Methods to Test**:
```java
public static String generateToken()              // Returns UUID string
public static String generateNewToken()           // Returns 32-byte Base64 string
public static String hashToken(String token)      // Returns SHA-256 hex string
public static boolean isTokenExpired(LocalDateTime tokenCreatedAt)  // Checks 24h expiry
public static boolean isResetTokenExpired(LocalDateTime tokenCreatedAt)  // Checks 1h expiry
```

**Test Examples**:
```java
@Test
void testGenerateTokenReturnsValidUUID() {
    String token = TokenUtil.generateToken();
    assertNotNull(token);
    // UUID format: 8-4-4-4-12 characters
    assertTrue(token.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"));
}

@Test
void testHashTokenIsConsistent() {
    String token = "test-token";
    String hash1 = TokenUtil.hashToken(token);
    String hash2 = TokenUtil.hashToken(token);
    
    assertEquals(hash1, hash2);
    assertEquals(64, hash1.length()); // SHA-256 = 32 bytes = 64 hex chars
}

@Test
void testHashTokenIsDifferentForDifferentInputs() {
    String hash1 = TokenUtil.hashToken("token1");
    String hash2 = TokenUtil.hashToken("token2");
    
    assertNotEquals(hash1, hash2);
}

@Test
void testTokenNotExpiredWithinWindow() {
    LocalDateTime recent = LocalDateTime.now().minusHours(12);
    assertFalse(TokenUtil.isTokenExpired(recent));
}

@Test
void testTokenExpiredAfter24Hours() {
    LocalDateTime old = LocalDateTime.now().minusHours(25);
    assertTrue(TokenUtil.isTokenExpired(old));
}

@Test
void testResetTokenExpiredAfter1Hour() {
    LocalDateTime old = LocalDateTime.now().minusMinutes(61);
    assertTrue(TokenUtil.isResetTokenExpired(old));
}
```

---

#### **`ConfigLoader.java`**

**Purpose**: Centralized configuration management with environment variable overrides.

**Methods to Test**:
```java
public static String get(String key, String defaultValue)  // Returns config value or default
public static String get(String key)                        // Returns config value or throws
public static int getInt(String key, int defaultValue)     // Parses integer
public static boolean getBoolean(String key, boolean defaultValue)  // Parses boolean
```

**Test Examples**:
```java
@Test
void testGetExistingProperty() {
    String value = ConfigLoader.get("db.url");
    assertNotNull(value);
    assertTrue(value.contains("expense_tracker"));
}

@Test
void testGetWithDefaultValue() {
    String value = ConfigLoader.get("nonexistent.key", "default_value");
    assertEquals("default_value", value);
}

@Test
void testGetThrowsOnMissingRequiredKey() {
    assertThrows(RuntimeException.class, () -> {
        ConfigLoader.get("this.key.does.not.exist");
    });
}

@Test
void testGetIntParsing() {
    int timeout = ConfigLoader.getInt("security.token.expiry.hours", 24);
    assertEquals(24, timeout);
}

@Test
void testGetBooleanParsing() {
    boolean enabled = ConfigLoader.getBoolean("feature.email.verification.enabled", false);
    assertTrue(enabled);
}

@Test
void testEnvironmentVariableOverride() {
    // Set environment variable: DB_URL=jdbc:mysql://override:3306/db
    // This test requires system property or environment setup
    
    String original = ConfigLoader.get("db.url");
    
    // Simulate environment variable (requires test setup)
    // System.setProperty would work in controlled test environment
    
    // Verify override takes precedence
    // String overridden = ConfigLoader.get("db.url");
    // assertNotEquals(original, overridden);
}
```

---

## 🧪 Test Coverage Goals

| Component | Target Coverage | Priority |
|-----------|----------------|----------|
| `ValidationService` | 100% | HIGH |
| `UserService` | 90% | HIGH |
| `ExpenseService` | 90% | HIGH |
| `BudgetService` | 85% | MEDIUM |
| `ReportService` | 75% | MEDIUM |
| `UserRepository` | 80% | HIGH |
| `ExpenseRepository` | 80% | HIGH |
| `TokenUtil` | 100% | HIGH |
| `ConfigLoader` | 90% | MEDIUM |

---

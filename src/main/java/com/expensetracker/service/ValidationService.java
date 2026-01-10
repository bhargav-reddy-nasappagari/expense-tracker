package com.expensetracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.regex.Pattern;

public final class ValidationService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{3,29}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private ValidationService() {} // utility class

    public static String validateAndNormalizeUsername(String username) {
        if (username == null || (username = username.trim()).isEmpty())
            throw new IllegalArgumentException("Username is required");
        if (!USERNAME_PATTERN.matcher(username).matches())
            throw new IllegalArgumentException("Username must be 4–30 chars, start with letter/_, only letters/digits/_");
        return username.toLowerCase();
    }

    public static String validatePassword(String password) {
        if (password == null || password.trim().isEmpty())
            throw new IllegalArgumentException("Password is required");
        if (!PASSWORD_PATTERN.matcher(password).matches())
            throw new IllegalArgumentException("Password must be 8+ chars with uppercase, lowercase, digit and special char");
        return password; // no normalization except trim
    }

    public static String normalizeFullName(String fullName) {
        if (fullName == null || (fullName = fullName.trim()).isEmpty())
            throw new IllegalArgumentException("Full name is required");
        return fullName;
    }

    public static String validateAndNormalizeEmail(String email) {
        if (email == null || (email = email.trim()).isEmpty())
            throw new IllegalArgumentException("Email is required");
        email = email.toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("Invalid email format");
        return email;
    }

    public static String validateAndNormalizePhone(String phone) {
        if (phone == null || (phone = phone.replaceAll("\\D", "")).isEmpty())
            throw new IllegalArgumentException("Phone is required");
        if (phone.length() != 10)
            throw new IllegalArgumentException("Phone must be exactly 10 digits");
        return phone;
    }

    public static String validateCategoryName(String name) {
        if (name == null || (name = name.trim()).isEmpty())
            throw new IllegalArgumentException("Category name is required");
        if (name.length() > 50)
            throw new IllegalArgumentException("Category name too long");
        return name;
    }

    public static String validateDescription(String desc) {
        if (desc == null || (desc = desc.trim()).isEmpty())
            throw new IllegalArgumentException("Description is required");
        if (desc.length() > 255)
            throw new IllegalArgumentException("Description too long");
        return desc;
    }

    public static BigDecimal validateAndRoundAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0");
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static LocalDate validateExpenseDate(LocalDate date) {
        if (date == null)
            throw new IllegalArgumentException("Expense date is required");
        if (date.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Expense date cannot be in the future");
        return date;
    }
}
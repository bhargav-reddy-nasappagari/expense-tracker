package com.expensetracker.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    // --- Username Tests ---
    @Test
    void testValidateUsername_Valid() {
        assertEquals("john_doe123", ValidationService.validateAndNormalizeUsername("John_Doe123"));
    }

    @Test
    void testValidateUsername_TooShort() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            ValidationService.validateAndNormalizeUsername("abc"));
        // Actual message: "Username must be 4–30 chars, start with letter/_, only letters/digits/_"
        assertTrue(exception.getMessage().contains("4") && exception.getMessage().contains("30"));
    }

    @Test
    void testValidateUsername_InvalidStartChar() {
        assertThrows(IllegalArgumentException.class, () -> 
            ValidationService.validateAndNormalizeUsername("1user"));
    }

    @Test
    void testValidateUsername_InvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> 
            ValidationService.validateAndNormalizeUsername("user-name")); // Hyphen not allowed
    }

    // --- Password Tests ---
    @Test
    void testValidatePassword_Valid() {
        String validPass = "SecurePass1!";
        assertEquals(validPass, ValidationService.validatePassword(validPass));
    }

    @Test
    void testValidatePassword_TooShort() {
        assertThrows(IllegalArgumentException.class, () -> 
            ValidationService.validatePassword("Pass1!"));
    }

    @Test
    void testValidatePassword_MissingCriteria() {
        assertAll("Password Complexity Checks",
            () -> assertThrows(IllegalArgumentException.class, () -> ValidationService.validatePassword("lowercase1!")), // No Upper
            () -> assertThrows(IllegalArgumentException.class, () -> ValidationService.validatePassword("UPPERCASE1!")), // No Lower
            () -> assertThrows(IllegalArgumentException.class, () -> ValidationService.validatePassword("NoDigits!!")),   // No Digit
            () -> assertThrows(IllegalArgumentException.class, () -> ValidationService.validatePassword("NoSpecial1"))   // No Special
        );
    }

    // --- Email Tests ---
    @Test
    void testValidateEmail_ValidAndNormalized() {
        assertEquals("user@example.com", ValidationService.validateAndNormalizeEmail("User@Example.COM"));
    }

    @Test
    void testValidateEmail_InvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> 
            ValidationService.validateAndNormalizeEmail("user.example.com")); // Missing @
    }

    // --- Phone Tests ---
    @Test
    void testValidatePhone_ValidAndNormalized() {
        assertEquals("1234567890", ValidationService.validateAndNormalizePhone("(123) 456-7890"));
    }

    @Test
    void testValidatePhone_InvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> 
            ValidationService.validateAndNormalizePhone("12345"));
    }

    // --- Amount Tests ---
    @Test
    void testValidateAmount_ValidAndRounding() {
        BigDecimal input = new BigDecimal("100.555");
        BigDecimal expected = new BigDecimal("100.56"); // Rounds HALF_UP
        assertEquals(expected, ValidationService.validateAndRoundAmount(input));
    }

    @Test
    void testValidateAmount_ZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> ValidationService.validateAndRoundAmount(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> ValidationService.validateAndRoundAmount(new BigDecimal("-10")));
    }

    // --- Date Tests ---
    @Test
    void testValidateDate_FutureDateThrows() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class, () -> ValidationService.validateExpenseDate(tomorrow));
    }
}
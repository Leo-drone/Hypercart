package com.hypercart.utils

import org.junit.Test
import org.junit.Assert.*

class ValidatorsTest {

    @Test
    fun `isValidEmail should return true for valid email`() {
        // Given
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org"
        )

        // When & Then
        validEmails.forEach { email ->
            assertTrue("Email $email should be valid", Validators.isValidEmail(email))
        }
    }

    @Test
    fun `isValidEmail should return false for invalid email`() {
        // Given
        val invalidEmails = listOf(
            "invalid-email",
            "@example.com",
            "user@",
            "user@.com",
            ""
        )

        // When & Then
        invalidEmails.forEach { email ->
            assertFalse("Email $email should be invalid", Validators.isValidEmail(email))
        }
    }

    @Test
    fun `isValidPassword should return Success for valid password`() {
        // Given
        val validPassword = "ValidPass123"

        // When
        val result = Validators.isValidPassword(validPassword)

        // Then
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `isValidPassword should return Error for short password`() {
        // Given
        val shortPassword = "Short1"

        // When
        val result = Validators.isValidPassword(shortPassword)

        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Le mot de passe doit contenir au moins 8 caractères", (result as ValidationResult.Error).message)
    }

    @Test
    fun `isValidPassword should return Error for password without lowercase`() {
        // Given
        val passwordWithoutLowercase = "VALIDPASS123"

        // When
        val result = Validators.isValidPassword(passwordWithoutLowercase)

        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Le mot de passe doit contenir au moins une minuscule", (result as ValidationResult.Error).message)
    }

    @Test
    fun `isValidPassword should return Error for password without uppercase`() {
        // Given
        val passwordWithoutUppercase = "validpass123"

        // When
        val result = Validators.isValidPassword(passwordWithoutUppercase)

        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Le mot de passe doit contenir au moins une majuscule", (result as ValidationResult.Error).message)
    }

    @Test
    fun `isValidPassword should return Error for password without digit`() {
        // Given
        val passwordWithoutDigit = "ValidPassword"

        // When
        val result = Validators.isValidPassword(passwordWithoutDigit)

        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Le mot de passe doit contenir au moins un chiffre", (result as ValidationResult.Error).message)
    }

    @Test
    fun `isValidPasswordConfirmation should return Success for matching passwords`() {
        // Given
        val password = "ValidPass123"
        val confirmation = "ValidPass123"

        // When
        val result = Validators.isValidPasswordConfirmation(password, confirmation)

        // Then
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `isValidPasswordConfirmation should return Error for non-matching passwords`() {
        // Given
        val password = "ValidPass123"
        val confirmation = "DifferentPass123"

        // When
        val result = Validators.isValidPasswordConfirmation(password, confirmation)

        // Then
        assertTrue(result is ValidationResult.Error)
        assertEquals("Les mots de passe ne correspondent pas", (result as ValidationResult.Error).message)
    }

    @Test
    fun `isValidToken should return true for valid token`() {
        // Given
        val validToken = "valid_token_12345"

        // When
        val result = Validators.isValidToken(validToken)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isValidToken should return false for invalid token`() {
        // Given
        val invalidTokens = listOf(
            "",
            "short",
            "   "
        )

        // When & Then
        invalidTokens.forEach { token ->
            assertFalse("Token '$token' should be invalid", Validators.isValidToken(token))
        }
    }
} 
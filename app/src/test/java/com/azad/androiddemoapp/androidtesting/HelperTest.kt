package com.azad.androiddemoapp.androidtesting

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HelperTest {
    lateinit var stringUtils: Helper

    @Before
    fun setup() {
        stringUtils = Helper()
        println("Before every test case")
    }

    @After
    fun tearDown() {
        println("After every test case")
    }

    @Test
    fun isPalindrome() {
        // Arrange

        val input = "madam"
        val expected = true
        // Act
        val actual = stringUtils.isPalindrome(input)
        val result = stringUtils.isPalindrome(input)
        // Assert
        assertEquals(expected, actual)
        // assertTrue(result)
        // assertFalse(result)

       // assertNotNull(input)
        // assertNull(input)

    }
    @Test
    fun isPalindrome_input_level_expected_true() {
        // Arrange
        val stringUtils = Helper()
        val input = "level"
        val expected = true
        // Act
        val actual = stringUtils.isPalindrome(input)
        // Assert
        assertEquals(expected, actual)
    }
    @Test
    fun isPalindrome_input_leve_expected_false() {
        // Arrange
        val stringUtils = Helper()
        val input = "leve"
        val expected = false
        // Act
        val actual = stringUtils.isPalindrome(input)
        // Assert
        assertEquals(expected, actual)
    }


}
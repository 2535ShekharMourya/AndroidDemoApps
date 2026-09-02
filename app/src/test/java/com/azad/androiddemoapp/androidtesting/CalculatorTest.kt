package com.azad.androiddemoapp.androidtesting

import org.junit.Assert.*
import org.junit.Test

class CalculatorTest {
    @Test
    fun add() {
        // Arrange
        val calculator = Calculator()
        val a = 10
        val b = 20
        val expected = 30

        // Act
        val actual = calculator.add(a, b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun multiply() {
        // Arrange
        val calculator = Calculator()
        val a = 10
        val b = 20
        val expected = 200

        // Act
        val actual = calculator.multiply(a, b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun checkEven() {
        // Arrange
        val calculator = Calculator()
        val a = 10
        val expected = true
        // Act
        val actual = calculator.checkEven(a)
        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun checkOdd() {
        // Arrange
        val calculator = Calculator()
        val a = 11
        val expected = true
        // Act
        val actual = calculator.checkOdd(a)
        // Assert
        assertEquals(expected, actual)
    }



}
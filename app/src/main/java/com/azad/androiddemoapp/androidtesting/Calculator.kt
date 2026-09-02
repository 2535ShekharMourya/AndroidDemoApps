package com.azad.androiddemoapp.androidtesting

class Calculator {
    fun add(a: Int, b: Int): Int {
        return a + b
    }

    fun multiply(a: Int, b: Int): Int {
        return a * b
    }
    fun checkEven(a: Int): Boolean {
        return a % 2 == 0
    }
    fun checkOdd(a: Int): Boolean {
        return a % 2 != 0
    }

}
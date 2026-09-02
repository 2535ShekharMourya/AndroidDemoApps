package com.azad.androiddemoapp.androidtesting

class Helper {
    fun isPalindrome(str: String): Boolean {
        val n = str.length
        for (i in 0 until n / 2) {
            if (str[i] != str[n - i - 1]) {
                return false
            }
        }
        return true
    }

    }
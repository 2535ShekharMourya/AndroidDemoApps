package com.azad.androiddemoapp.utils

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ParameterizedExample(val input: String, val expected: Boolean) {

    @Test
    fun test() {
        val helper = Helper()
        val result = helper.isPalindrome(input)
        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: isPalindrome({0})={1}")
        fun data(): List<Array<Any>> {
            return listOf(
                arrayOf("madam", true),
                arrayOf("level", true),
                arrayOf("hello", false),
                arrayOf("m", true),
                arrayOf("", true)
            )
        }
    }


}
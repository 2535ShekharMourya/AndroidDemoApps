package com.azad.androiddemoapp

import android.util.Log

object Util {
    fun String.logFlowC1(tag: String = "FLOW") {
        Log.d(tag, this)
    }
    fun String.logFlowC2(tag: String = "FLOW") {
        Log.e(tag, this)
    }
    fun String.logFlowC3(tag: String = "FLOW") {
        Log.i(tag, this)
    }

}
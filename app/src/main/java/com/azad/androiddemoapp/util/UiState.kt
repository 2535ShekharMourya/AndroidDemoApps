package com.azad.androiddemoapp.util

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val errorType: ErrorType, val message: String? = null) : UiState<Nothing>
}

sealed interface ErrorType {
    object NoInternet : ErrorType
    object Timeout : ErrorType
    object ServerError : ErrorType
    object EmptyResponse : ErrorType
    data class HttpError(val code: Int) : ErrorType
    object Unknown : ErrorType
}

package com.azad.androiddemoapp.model

sealed interface ResourceState<out T> {
    data object Loading : ResourceState<Nothing>
    
    data class Success<out T>(val data: T) : ResourceState<T>
    
    sealed interface Error : ResourceState<Nothing> {
        data object NoInternet : Error
        data object Timeout : Error
        data class HttpError(val code: Int, val message: String) : Error
        data object EmptyResponse : Error
        data class ServerError(val message: String?) : Error
        data class UnknownError(val throwable: Throwable) : Error
    }
}

sealed interface PermissionState {
    data object Granted : PermissionState
    data object Rationale : PermissionState
    data object Denied : PermissionState
}

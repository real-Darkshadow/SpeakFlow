package com.app.speak.api

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String?): Resource<T> {
            return Resource(Status.ERROR, null, msg)
        }

        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, null, null)
        }

        fun <T> idle(): Resource<T> {
            return Resource(Status.IDLE, null, null)
        }

    }

}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    IDLE
}

enum class PaymentStatus {
    NOT_INITIATED,
    SUCCESS,
    FAILURE,
    CANCELED
}
package com.example.photoweather.network.models

data class Resource<out T>(val data : T?, val status: Status, val message: String?) {

    companion object {
        fun <T> success(data : T): Resource<T> =
                Resource(data = data , status = Status.SUCCESS, message = null)

        fun <T> error(data : T?,message: String): Resource<T> =
                Resource(data = data , status = Status.ERROR, message = message)

        fun <T> loading(data :T?): Resource<T> =
                Resource(data = data, status = Status.LOADING, message = null)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}
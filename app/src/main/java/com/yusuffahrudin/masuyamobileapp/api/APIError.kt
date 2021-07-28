package com.yusuffahrudin.masuyamobileapp.api

class APIError {
    private val statusCode = 0
    private val message: String = "Unknown error"

    fun status(): Int {
        return statusCode
    }

    fun message(): String {
        return message
    }
}
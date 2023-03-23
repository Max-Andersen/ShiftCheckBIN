package com.example.shiftcheckbin.network.models

@kotlinx.serialization.Serializable
data class ErrorResponse(
    val code: Int,
    val message: String
)
package com.example.shiftcheckbin.network.models

@kotlinx.serialization.Serializable
data class Bank(
    val name: String,
    val url: String?,
    val phone: String?,
    val city: String?
)
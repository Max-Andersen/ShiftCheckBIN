package com.example.shiftcheckbin.network.models

@kotlinx.serialization.Serializable
data class Number(
    val length: Int,
    val luhn: Boolean?
)
package com.example.shiftcheckbin.network.models

@kotlinx.serialization.Serializable
data class BinInfoResponse (
    val number : Number,
    val scheme : String,
    val type : String?,
    val brand : String,
    val prepaid : Boolean?,
    val country : Country,
    val bank : Bank
)
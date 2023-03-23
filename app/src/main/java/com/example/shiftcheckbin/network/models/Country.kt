package com.example.shiftcheckbin.network.models

@kotlinx.serialization.Serializable
data class Country(
    val numeric: Int,
    val alpha2: String,
    val name: String,
    val emoji: String,
    val currency: String,
    val latitude: Float,
    val longitude: Float
)
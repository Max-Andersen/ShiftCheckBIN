package com.example.shiftcheckbin.network.api

import com.example.shiftcheckbin.network.models.BinInfoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface BinApi {

    @GET("{bin}")
    suspend fun getInfoByBin(@Path("bin") enteredBin: String): Response<BinInfoResponse>
}
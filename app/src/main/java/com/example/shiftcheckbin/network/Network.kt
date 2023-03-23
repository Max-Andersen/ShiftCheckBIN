package com.example.shiftcheckbin.network

import com.example.shiftcheckbin.network.api.BinApi
import com.example.shiftcheckbin.network.models.ApiResponse
import com.example.shiftcheckbin.network.models.ErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Network {
    private const val BASE_URL = "https://lookup.binlist.net/"

    private fun getHttpClient(): OkHttpClient {
        val client = OkHttpClient.Builder().apply {
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
            val logLevel = HttpLoggingInterceptor.Level.BODY
            addInterceptor(HttpLoggingInterceptor().setLevel(logLevel))
        }
        return client.build()
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .client(getHttpClient())
            .build()
    }

    private val retrofit: Retrofit = getRetrofit()

    fun getBinApi(): BinApi = retrofit.create(BinApi::class.java)

}
fun <T> apiRequestFlow(call: suspend () -> Response<T>): Flow<ApiResponse<T>> = flow {
    emit(ApiResponse.Loading)

    withTimeoutOrNull(15000L) {
        val response = call()

        try {
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    emit(ApiResponse.Success(data))
                }
            } else {
                emit(ApiResponse.Failure(response.code()))
                response.errorBody()?.close()
            }
        } catch (e: Exception) {
            emit(ApiResponse.Failure(400))
        }
    } ?: emit(ApiResponse.Failure(408))
}.flowOn(Dispatchers.IO)
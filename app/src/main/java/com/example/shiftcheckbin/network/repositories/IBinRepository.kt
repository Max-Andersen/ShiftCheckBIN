package com.example.shiftcheckbin.network.repositories

import com.example.shiftcheckbin.network.models.ApiResponse
import com.example.shiftcheckbin.network.models.BinInfoResponse
import kotlinx.coroutines.flow.Flow

interface IBinRepository {

    fun getInfoByBin(bin: String): Flow<ApiResponse<BinInfoResponse>>
}
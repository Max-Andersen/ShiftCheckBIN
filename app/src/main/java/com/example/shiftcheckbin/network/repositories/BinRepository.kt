package com.example.shiftcheckbin.network.repositories

import com.example.shiftcheckbin.network.api.BinApi
import com.example.shiftcheckbin.network.Network
import com.example.shiftcheckbin.network.apiRequestFlow
import com.example.shiftcheckbin.network.models.ApiResponse
import com.example.shiftcheckbin.network.models.BinInfoResponse
import kotlinx.coroutines.flow.Flow

class BinRepository: IBinRepository {
    private val binApi: BinApi = Network.getBinApi()

    override fun getInfoByBin(bin: String): Flow<ApiResponse<BinInfoResponse>> = apiRequestFlow {
        binApi.getInfoByBin(bin)
    }
}
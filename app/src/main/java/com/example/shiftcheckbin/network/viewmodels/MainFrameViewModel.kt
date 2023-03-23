package com.example.shiftcheckbin.network.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.shiftcheckbin.MainFragment
import com.example.shiftcheckbin.network.models.ApiResponse
import com.example.shiftcheckbin.network.models.BinInfoResponse
import com.example.shiftcheckbin.network.repositories.BinRepository
import com.example.shiftcheckbin.network.repositories.IBinRepository

class MainFrameViewModel : BaseViewModel() {
    private val binRepository: IBinRepository = BinRepository()
    private val _cardInfo = MutableLiveData<ApiResponse<BinInfoResponse>>()

    var history = ArrayList<String>()

    private val errorHandler = object : CoroutinesErrorHandler {
        override fun onError(message: String) {
            Log.d("!", message)
        }
    }

    val cardInfo = _cardInfo

    fun getInfo(binNumber: String) {
        baseRequest(_cardInfo, errorHandler, binRepository.getInfoByBin(binNumber))
    }

    fun saveNumber(newNumber: String, recyclerViewAdapter: MainFragment.HistoryAdapter) {
        if (history.size != 0) {
            if (history.last() != newNumber) {
                history.add(newNumber)
                recyclerViewAdapter.notifyItemChanged(history.size - 1)
            }
        } else {
            history.add(newNumber)
            recyclerViewAdapter.notifyItemChanged(history.size - 1)
        }

    }
}
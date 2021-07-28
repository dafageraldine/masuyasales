package com.yusuffahrudin.masuyamobileapp.data.history_penjualan

import com.google.gson.annotations.SerializedName

data class HistoryPenjualanResponse (
        @SerializedName("result")
        val result: ArrayList<HistoryPenjualan>? = null
)
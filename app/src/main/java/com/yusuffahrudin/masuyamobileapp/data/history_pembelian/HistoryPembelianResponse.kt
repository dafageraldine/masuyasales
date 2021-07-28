package com.yusuffahrudin.masuyamobileapp.data.history_pembelian

import com.google.gson.annotations.SerializedName

data class HistoryPembelianResponse (
        @SerializedName("result")
        val result: ArrayList<HistoryPembelian>? = null
)
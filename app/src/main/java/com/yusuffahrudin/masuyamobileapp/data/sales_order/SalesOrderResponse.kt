package com.yusuffahrudin.masuyamobileapp.data.sales_order

import com.google.gson.annotations.SerializedName

data class SalesOrderResponse (
        @SerializedName("result")
        val result: ArrayList<SalesOrder>? = null
)
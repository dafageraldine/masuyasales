package com.yusuffahrudin.masuyamobileapp.data

import com.google.gson.annotations.SerializedName

data class Result (
        @field:SerializedName("success")
        val success: Int? = null,

        @field:SerializedName("message")
        val message: String? = null
)
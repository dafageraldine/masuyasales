package com.yusuffahrudin.masuyamobileapp.api

import com.yusuffahrudin.masuyamobileapp.data.Result
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface UploadFileService {
    @Multipart
    @POST("YOUR_URL/image_uploader.php")
    fun uploadImages(@Part images: List<MultipartBody.Part?>?): Call<Result?>?
}
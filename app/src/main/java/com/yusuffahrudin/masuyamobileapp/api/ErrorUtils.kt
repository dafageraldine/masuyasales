package com.yusuffahrudin.masuyamobileapp.api

import android.app.Activity
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException


object ErrorUtils {
    fun parseError(response: Response<*>): APIError? {
        val converter: Converter<ResponseBody?, APIError> =
                API
                .instance()
                .responseBodyConverter(APIError::class.java, arrayOfNulls<Annotation>(0))
        val error: APIError?
        error = try {
            converter.convert(response.errorBody())
        } catch (e: IOException) {
            return APIError()
        }
        return error
    }
}
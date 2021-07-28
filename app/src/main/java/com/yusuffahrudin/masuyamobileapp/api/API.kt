package com.yusuffahrudin.masuyamobileapp.api

import com.yusuffahrudin.masuyamobileapp.BuildConfig
import com.yusuffahrudin.masuyamobileapp.controller.AppController.appContext
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object API {
    fun instance(): Retrofit {
        val sessionManager = SessionManager(appContext)
        val cache = sessionManager.userDetails
        val url = if (cache[SessionManager.kdkota].equals("TES", ignoreCase = true)) {
            "http://91d70b4fe043.sn.mynetname.net:81/cobaApp2/masuyates/"
        } else if (cache[SessionManager.kdkota].equals("JKT", ignoreCase = true)) {
            "http://103.119.228.145:83/cobaApp2/masuyamobile" + cache[SessionManager.kdkota] + "/"
        } else {
            "http://91d70b4fe043.sn.mynetname.net:81/cobaApp2/masuyamobile" + cache[SessionManager.kdkota] + "/"
        }

        val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ApiWorker.gsonConverter)
                .client(ApiWorker.client)
                .build()
        return retrofit
    }
}
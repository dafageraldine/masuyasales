package com.yusuffahrudin.masuyamobileapp.historypenjualan

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.history_penjualan.HistoryPenjualan
import com.yusuffahrudin.masuyamobileapp.data.history_penjualan.HistoryPenjualanResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

class HistoryPenjualanViewModel: ViewModel() {
    private var historyList: MutableLiveData<ArrayList<HistoryPenjualan>>? = null

    fun getHistoryPenjualan(activity: Activity, nmbrg: String, customer: String, sales: String, fromTgl: String, toTgl: String, kdkota: String): LiveData<ArrayList<HistoryPenjualan>> {
        historyList = MutableLiveData()
        loadHistoryPenjualan(activity, nmbrg, customer, sales, fromTgl, toTgl, kdkota)
        return historyList as MutableLiveData<ArrayList<HistoryPenjualan>>
    }

    @SuppressLint("CheckResult")
    private fun loadHistoryPenjualan(activity: Activity, nmbrg: String, customer: String, sales: String, fromTgl: String, toTgl: String, kdkota: String) {
        API.instance().create(ApiService::class.java)
                .getHistoryPenj(nmbrg, customer, sales, fromTgl, toTgl, kdkota)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(getObserverHistoryPenjualan(activity))
    }

    private fun getObserverHistoryPenjualan(activity: Activity): DisposableObserver<HistoryPenjualanResponse> {
        return object : DisposableObserver<HistoryPenjualanResponse>() {
            override fun onNext(response: HistoryPenjualanResponse) {
                println("================= result    ${response.result}")
                historyList?.value = response.result
            }

            override fun onError(e: Throwable) {
                Log.d("HistoryPenjViewModel", "Error $e")
                FirebaseCrashlytics.getInstance().recordException(e)
                DialogAlert("${activity.getString(R.string.error_pengambilan_data)} ${e.message}", "error", Objects.requireNonNull(activity))
            }

            override fun onComplete() {
                Log.d("HistoryPenjViewModel", "Fetch data success")
            }
        }
    }
}
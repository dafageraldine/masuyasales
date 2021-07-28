package com.yusuffahrudin.masuyamobileapp.historypembelian

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
import com.yusuffahrudin.masuyamobileapp.data.history_pembelian.HistoryPembelian
import com.yusuffahrudin.masuyamobileapp.data.history_pembelian.HistoryPembelianResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

class HistoryPembelianViewModel: ViewModel() {
    private var historyList: MutableLiveData<ArrayList<HistoryPembelian>>? = null

    fun getHistoryPemb(activity: Activity, nmbrg: String, supplier: String, fromTgl: String, toTgl: String, kdkota: String): LiveData<ArrayList<HistoryPembelian>> {
        historyList = MutableLiveData()
        loadHistoryPemb(activity, nmbrg, supplier, fromTgl, toTgl, kdkota)
        return historyList as MutableLiveData<ArrayList<HistoryPembelian>>
    }

    @SuppressLint("CheckResult")
    private fun loadHistoryPemb(activity: Activity, nmbrg: String, supplier: String, fromTgl: String, toTgl: String, kdkota: String) {
        API.instance().create(ApiService::class.java)
                .getHistoryPemb(nmbrg, supplier, fromTgl, toTgl, kdkota)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(getObserverHistoryPemb(activity))
    }

    private fun getObserverHistoryPemb(activity: Activity): DisposableObserver<HistoryPembelianResponse> {
        return object : DisposableObserver<HistoryPembelianResponse>() {
            override fun onNext(response: HistoryPembelianResponse) {
                println("================= result    ${response.result}")
                historyList?.value = response.result
            }

            override fun onError(e: Throwable) {
                Log.d("HistoryPembViewModel", "Error $e")
                FirebaseCrashlytics.getInstance().recordException(e)
                DialogAlert("${activity.getString(R.string.error_pengambilan_data)} ${e.message}", "error", Objects.requireNonNull(activity))
            }

            override fun onComplete() {
                Log.d("HistoryPembViewModel", "Fetch data success")
            }
        }
    }
}
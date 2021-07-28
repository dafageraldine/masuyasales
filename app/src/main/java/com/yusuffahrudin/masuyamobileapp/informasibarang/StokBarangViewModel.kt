package com.yusuffahrudin.masuyamobileapp.informasibarang

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Stok
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.StokResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

class StokBarangViewModel: ViewModel() {
    private var stokList: MutableLiveData<ArrayList<Stok>>? = null

    fun getStok(swipeRefreshLayout: SwipeRefreshLayout, activity: Activity, kdbrg: String, kota: String, user: String, rusak: Boolean): LiveData<ArrayList<Stok>> {
        stokList = MutableLiveData()
        loadStok(swipeRefreshLayout, activity, kdbrg, kota, user, rusak)
        return stokList as MutableLiveData<ArrayList<Stok>>
    }

    @SuppressLint("CheckResult")
    private fun loadStok(swipeRefreshLayout: SwipeRefreshLayout, activity: Activity, kdbrg: String, kota: String, user: String, rusak: Boolean) {
        if (rusak){
            API.instance().create(ApiService::class.java)
                    .getStokBarangRusak(kdbrg, kota, user)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribeWith(getObserverStok(activity, swipeRefreshLayout))
        } else {
            API.instance().create(ApiService::class.java)
                    .getStokBarang(kdbrg, kota, user)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribeWith(getObserverStok(activity, swipeRefreshLayout))
        }
    }

    private fun getObserverStok(activity: Activity, swipeRefreshLayout: SwipeRefreshLayout): DisposableObserver<StokResponse> {
        return object : DisposableObserver<StokResponse>() {
            override fun onNext(response: StokResponse) {
                println("================= result    ${response.result}")
                stokList?.value = response.result
            }

            override fun onError(e: Throwable) {
                Log.d("StokBarangViewModel", "Error $e")
                FirebaseCrashlytics.getInstance().recordException(e)
                DialogAlert("${activity.getString(R.string.error_pengambilan_data)} ${e.message}", "error", Objects.requireNonNull(activity))
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onComplete() {
                Log.d("StokBarangViewModel", "Fetch data success")
            }
        }
    }
}
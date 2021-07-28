package com.yusuffahrudin.masuyamobileapp.updatepricelist

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Product
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.ProductResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class ListBarangViewModel: ViewModel() {
    private var brgList: MutableLiveData<ArrayList<Product>>? = null

    fun getBarang(activity: Activity, tipe: String, kdcust: String, itemCount: Int): LiveData<ArrayList<Product>> {
        brgList = MutableLiveData()
        loadBarang(activity, tipe, kdcust, itemCount)
        return brgList as MutableLiveData<ArrayList<Product>>
    }

    @SuppressLint("CheckResult")
    private fun loadBarang(activity: Activity, tipe: String, kdcust: String, itemCount: Int) {
        println("---------------------- "+activity.localClassName)
        if (activity.localClassName.equals("updatepricelist.ListBarangSpecialPriceActivity", ignoreCase = true)){
            API.instance().create(ApiService::class.java)
                    .getBarangSpecialPrice(tipe, kdcust, itemCount)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribeWith(getObserverBarang(activity))
        }
        if (activity.localClassName.equals("updatepricelist.UpdatePriceCustActivity", ignoreCase = true)){
            API.instance().create(ApiService::class.java)
                    .getBarangPriceCust(kdcust, itemCount)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribeWith(getObserverBarang(activity))
        }
    }

    private fun getObserverBarang(activity: Activity): DisposableObserver<ProductResponse> {
        return object : DisposableObserver<ProductResponse>() {
            override fun onNext(response: ProductResponse) {
                println("================= result    ${response.result}")
                brgList?.value = response.result
            }

            override fun onError(e: Throwable) {
                DialogAlert(e.message, "error", activity)
                Log.d("ListBarangViewModel", "Error $e")
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            override fun onComplete() {
                Log.d("ListBarangViewModel", "Fetch data success")
            }
        }
    }
}
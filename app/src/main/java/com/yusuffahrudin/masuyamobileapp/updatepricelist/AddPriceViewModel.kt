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

class AddPriceViewModel: ViewModel() {
    private var brgList: MutableLiveData<ArrayList<Product>>? = null

    fun getBarang(activity: Activity, kdbrg: String): LiveData<ArrayList<Product>> {
        brgList = MutableLiveData()
        loadBarang(activity, kdbrg)
        return brgList as MutableLiveData<ArrayList<Product>>
    }

    @SuppressLint("CheckResult")
    private fun loadBarang(activity: Activity, kdbrg: String) {
        println("---------------------- "+activity.localClassName)
        API.instance().create(ApiService::class.java)
                .getBarangHrgMin(kdbrg)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(getObserverBarang(activity))
    }

    fun getBarangPricelist(activity: Activity, kdcust: String, kdbrg: String): LiveData<ArrayList<Product>> {
        brgList = MutableLiveData()
        loadBarangPricelist(activity, kdcust, kdbrg)
        return brgList as MutableLiveData<ArrayList<Product>>
    }

    @SuppressLint("CheckResult")
    private fun loadBarangPricelist(activity: Activity, kdcust: String, kdbrg: String) {
        println("---------------------- "+activity.localClassName)
        API.instance().create(ApiService::class.java)
                .getBarangPricelist(kdcust, kdbrg)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(getObserverBarang(activity))
    }

    private fun getObserverBarang(activity: Activity): DisposableObserver<ProductResponse> {
        return object : DisposableObserver<ProductResponse>() {
            override fun onNext(response: ProductResponse) {
                println("================= result    ${response.result}")
                brgList?.value = response.result
                if (response.result!!.isEmpty()){
                    if (activity.localClassName.equals("updatepricelist.AddSpecialPriceActivity", ignoreCase = true)){
                        DialogAlert("Belum ada pricelist khusus untuk barang tsb.!", "attention", activity)
                    }
                }
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
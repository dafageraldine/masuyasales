package com.yusuffahrudin.masuyamobileapp.salesorder

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.sales_order.ItemJual
import com.yusuffahrudin.masuyamobileapp.data.sales_order.ItemJualResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class CreateSOViewModel: ViewModel() {
    private var itemList: MutableLiveData<ArrayList<ItemJual>>? = null

    fun getItem(activity: Activity, kdcust: String, kdgd: String, statusPajak: String, itemCount: Int): LiveData<ArrayList<ItemJual>> {
        itemList = MutableLiveData()
        loadItem(activity, kdcust, kdgd, statusPajak, itemCount)
        return itemList as MutableLiveData<ArrayList<ItemJual>>
    }

    @SuppressLint("CheckResult")
    private fun loadItem(activity: Activity, kdcust: String, kdgd: String, statusPajak: String, itemCount: Int) {
        API.instance().create(ApiService::class.java)
                .getItemSOforSale(kdcust, kdgd, statusPajak, itemCount)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(getObserverItem(activity))
    }

    fun findItem(activity: Activity, kdcust: String, kdgd: String, statusPajak: String, barang: String): LiveData<ArrayList<ItemJual>> {
        itemList = MutableLiveData()
        loadfindItem(activity, kdcust, kdgd, statusPajak, barang)
        return itemList as MutableLiveData<ArrayList<ItemJual>>
    }

    @SuppressLint("CheckResult")
    private fun loadfindItem(activity: Activity, kdcust: String, kdgd: String, statusPajak: String, barang: String) {
        API.instance().create(ApiService::class.java)
                .findItemSOforSale(kdcust, kdgd, statusPajak, barang)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(getObserverItem(activity))
    }

    private fun getObserverItem(activity: Activity): DisposableObserver<ItemJualResponse> {
        return object : DisposableObserver<ItemJualResponse>() {
            override fun onNext(response: ItemJualResponse) {
                println("================= result    ${response.result}")
                itemList?.value = response.result as ArrayList<ItemJual>?
            }

            override fun onError(e: Throwable) {
                DialogAlert(e.message, "error", activity)
                Log.d("SalesOrderViewModel", "Error $e")
                e.printStackTrace()
            }

            override fun onComplete() {
                Log.d("SalesOrderViewModel", "Fetch data success")
            }
        }
    }
}
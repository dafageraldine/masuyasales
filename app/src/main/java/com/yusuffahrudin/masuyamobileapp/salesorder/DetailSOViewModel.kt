package com.yusuffahrudin.masuyamobileapp.salesorder

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
import com.yusuffahrudin.masuyamobileapp.data.sales_order.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class DetailSOViewModel: ViewModel() {
    private var itemList: MutableLiveData<ArrayList<ItemJual>>? = null
    private var headerList: MutableLiveData<ArrayList<SalesOrder>>? = null
    private var detailList: MutableLiveData<ArrayList<ItemOrder>>? = null

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

    fun getHeader(activity: Activity, nobukti: String): LiveData<ArrayList<SalesOrder>> {
        headerList = MutableLiveData()
        loadHeader(activity, nobukti)
        return headerList as MutableLiveData<ArrayList<SalesOrder>>
    }

    @SuppressLint("CheckResult")
    private fun loadHeader(activity: Activity, nobukti: String) {
        API.instance().create(ApiService::class.java)
                .getHeaderSO(nobukti)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(getObserverHeader(activity))
    }

    fun getDetail(activity: Activity, nobukti: String): LiveData<ArrayList<ItemOrder>> {
        detailList = MutableLiveData()
        loadDetail(activity, nobukti)
        return detailList as MutableLiveData<ArrayList<ItemOrder>>
    }

    @SuppressLint("CheckResult")
    private fun loadDetail(activity: Activity, nobukti: String) {
        API.instance().create(ApiService::class.java)
                .getDetailSO(nobukti)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(getObserverDetail(activity))
    }

    private fun getObserverHeader(activity: Activity): DisposableObserver<SalesOrderResponse> {
        return object : DisposableObserver<SalesOrderResponse>() {
            override fun onNext(response: SalesOrderResponse) {
                println("================= result    ${response.result}")
                headerList?.value = response.result
            }

            override fun onError(e: Throwable) {
                DialogAlert(e.message, "error", activity)
                Log.d("DetailSOViewModel", "Error $e")
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            override fun onComplete() {
                Log.d("DetailSOViewModel", "Fetch data success")
            }
        }
    }

    private fun getObserverDetail(activity: Activity): DisposableObserver<ItemOrderResponse> {
        return object : DisposableObserver<ItemOrderResponse>() {
            override fun onNext(response: ItemOrderResponse) {
                println("================= result    ${response.result}")
                detailList?.value = response.result as ArrayList<ItemOrder>?
            }

            override fun onError(e: Throwable) {
                DialogAlert(e.message, "error", activity)
                Log.d("DetailSOViewModel", "Error $e")
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            override fun onComplete() {
                Log.d("DetailSOViewModel", "Fetch data success")
            }
        }
    }

    private fun getObserverItem(activity: Activity): DisposableObserver<ItemJualResponse> {
        return object : DisposableObserver<ItemJualResponse>() {
            override fun onNext(response: ItemJualResponse) {
                println("================= result item   ${response.result}")
                itemList?.value = response.result as ArrayList<ItemJual>
            }

            override fun onError(e: Throwable) {
                DialogAlert(e.message, "error", activity)
                Log.d("DetailSOViewModel", "Error $e")
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            override fun onComplete() {
                Log.d("DetailSOViewModel", "Fetch data success")
            }
        }
    }
}
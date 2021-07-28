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
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrderResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class SalesOrderViewModel: ViewModel() {
    private var soList: MutableLiveData<ArrayList<SalesOrder>>? = null

    fun getSO(activity: Activity, user: String, itemCount: Int, tipeSO: String, nobukti: String, cust: String, autho: String, status: String, item: String): LiveData<ArrayList<SalesOrder>> {
        soList = MutableLiveData()
        loadSO(activity, user, itemCount, tipeSO, nobukti, cust, autho, status, item)
        return soList as MutableLiveData<ArrayList<SalesOrder>>
    }

    @SuppressLint("CheckResult")
    private fun loadSO(activity: Activity, user: String, itemCount: Int, tipeSO: String, nobukti: String, cust: String, autho: String, status: String, item: String) {
        println("=======SalesOVM=============")
        println(tipeSO)
        when {
            tipeSO.equals("All", ignoreCase = true) -> {
                API.instance().create(ApiService::class.java)
                        .getSOAll(user, itemCount, nobukti, cust, autho, status, item)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeWith(getObserverSO(activity))
            }
            tipeSO.equals("Open", ignoreCase = true) -> {
                API.instance().create(ApiService::class.java)
                        .getSOOpen(user, itemCount, nobukti, cust, autho, status, item)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeWith(getObserverSO(activity))
            }
            tipeSO.equals("Pending", ignoreCase = true) -> {
                API.instance().create(ApiService::class.java)
                        .getSOPending(user, itemCount, nobukti, cust, autho, status, item)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeWith(getObserverSO(activity))
            }
            tipeSO.equals("Close", ignoreCase = true) -> {
                API.instance().create(ApiService::class.java)
                        .getSOClose(user, itemCount, nobukti, cust, autho, status, item)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeWith(getObserverSO(activity))
            }
            tipeSO.equals("Otorisasi", ignoreCase = true) -> {
                API.instance().create(ApiService::class.java)
                        .getSOOtorisasi(user, nobukti, cust, autho, status, item)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeWith(getObserverSO(activity))
            }
        }
    }

    private fun getObserverSO(activity: Activity): DisposableObserver<SalesOrderResponse> {
        return object : DisposableObserver<SalesOrderResponse>() {
            override fun onNext(response: SalesOrderResponse) {
                println("================= result    ${response.result}")
                soList?.value = response.result
            }

            override fun onError(e: Throwable) {
                DialogAlert(e.message, "error", activity)
                Log.d("SalesOrderViewModel", "Error $e")
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            override fun onComplete() {
                Log.d("SalesOrderViewModel", "Fetch data success")
            }
        }
    }
}
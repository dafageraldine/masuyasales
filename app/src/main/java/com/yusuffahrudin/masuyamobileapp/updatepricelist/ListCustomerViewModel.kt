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
import com.yusuffahrudin.masuyamobileapp.data.customer.Customer
import com.yusuffahrudin.masuyamobileapp.data.customer.CustomerResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class ListCustomerViewModel: ViewModel() {
    private var custList: MutableLiveData<ArrayList<Customer>>? = null

    fun getCustomer(activity: Activity, tipe: String, cust: String, itemCount: Int): LiveData<ArrayList<Customer>> {
        custList = MutableLiveData()
        loadCustomer(activity, tipe, cust, itemCount)
        return custList as MutableLiveData<ArrayList<Customer>>
    }

    @SuppressLint("CheckResult")
    private fun loadCustomer(activity: Activity, tipe: String, cust: String, itemCount: Int) {
        println("---------------------- "+activity.localClassName)
        if (activity.localClassName.equals("updatepricelist.ListCustomerSpecialPriceActivity", ignoreCase = true)){
            if (cust == ""){
                API.instance().create(ApiService::class.java)
                        .getCustomerSpecialPrice(tipe, itemCount)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeWith(getObserverCustomer(activity))
            } else {
                API.instance().create(ApiService::class.java)
                        .searchCustomerSpecialPrice(tipe, cust)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeWith(getObserverCustomer(activity))
            }
        } else {
            if (cust == ""){
                API.instance().create(ApiService::class.java)
                        .getAllCustomer(tipe)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeWith(getObserverCustomer(activity))
            } else {
                println("--------------- cust tidak kosong")
                API.instance().create(ApiService::class.java)
                        .searchCustomer(tipe, cust)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeWith(getObserverCustomer(activity))
            }

        }
        /*if (activity.localClassName.equals("updatepricelist.AddSpecialPriceCustActivity", ignoreCase = true)){
            API.instance().create(ApiService::class.java)
                    .getAllCustomer(tipe)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribeWith(getObserverCustomer(activity))
        }*/
    }

    private fun getObserverCustomer(activity: Activity): DisposableObserver<CustomerResponse> {
        return object : DisposableObserver<CustomerResponse>() {
            override fun onNext(response: CustomerResponse) {
                println("================= result    ${response.result}")
                custList?.value = response.result
            }

            override fun onError(e: Throwable) {
                DialogAlert(e.message, "error", activity)
                Log.d("ListCustomerViewModel", "Error $e")
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            override fun onComplete() {
                Log.d("ListCustomerViewModel", "Fetch data success")
            }
        }
    }
}
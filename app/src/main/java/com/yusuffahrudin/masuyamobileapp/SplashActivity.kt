package com.yusuffahrudin.masuyamobileapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.CekVersiResponse
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrderResponse
import com.yusuffahrudin.masuyamobileapp.databinding.ActivitySplashBinding
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class SplashActivity : AppCompatActivity() {
    internal lateinit var sessionManager: SessionManager
    private var level: String? = null
    private var kdkota: String? = null
    private lateinit var binding: ActivitySplashBinding
    public var nmkota: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //img_logo = findViewById(R.id.img_logo)
        //tv_version = findViewById(R.id.tv_version)
        println("================== splash")
        binding.tvVersion.text = "Masuya Mobile App v" + applicationVersionName()
        val anim = AnimationUtils.loadAnimation(this, R.anim.splash_transition)
        binding.imgLogo.startAnimation(anim)
        binding.tvVersion.startAnimation(anim)

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        level = user[SessionManager.level]
        kdkota = user[SessionManager.kdkota]

        val timer = object : Thread() {
            override fun run() {
                try {
                    sleep(5000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    var msg = sessionManager.checkloginforapk()
                    if(msg == "sudah login"){
                    cekversiandroid()
                    }
                    else {
                        sessionManager.checkLogin()
                        finish()
                    }
                }
            }
        }
        timer.start()
    }

    //Programmatically get the current version Name
    private fun applicationVersionName(): String{
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return packageInfo.versionName
    }

    private fun cekversiandroid() {
        val params = HashMap<String?, String?>()
        params["versi"] = applicationVersionName()
//        params["versi"] = "2.56.88"
            API.instance().create(ApiService::class.java)
            .versioncheck(params)?.enqueue(object : Callback<CekVersiResponse?> {
                override fun onResponse(
                    call: Call<CekVersiResponse?>,
                    response: Response<CekVersiResponse?>
                ) {
                    println("======cek versi dulu==========")
                    println(response.body()?.result?.get(0)?.pesan.toString())
                    val pesan = response.body()?.result?.get(0)?.pesan.toString()
                    println("======selesai cek versi==========")
                    if (pesan == "latest"){
                        println("sudah update")
                        sessionManager.checkLogin()
                        finish()

                    } else if (pesan == "silahkan update aplikasi anda !"){
                        DialogAlert(pesan, "error", this@SplashActivity)
                        println("belum update")
//                        finish()
                    }

                }

                override fun onFailure(call: Call<CekVersiResponse?>, t: Throwable) {
                    DialogAlert(t.message, "error", this@SplashActivity)
                    print(t.message)
                    cekversiandroid()
                }

            })
    }
}

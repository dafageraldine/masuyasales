package com.yusuffahrudin.masuyamobileapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R.id.*
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityHomeBinding
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import kotlin.properties.Delegates


class HomeActivity: AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var level: String
    private lateinit var kdkota: String
    private lateinit var pass: String
    private lateinit var name: String
    private var broadcastReceiver: BroadcastReceiver? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val idFirebase = "1002"
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val i = intent
        pass = i.extras?.getString("pass").toString()
        sessionManager = SessionManager(this)
        val sessionManager = sessionManager.userDetails
        level = sessionManager[SessionManager.level].toString()
        kdkota = sessionManager[SessionManager.kdkota].toString()
        name = sessionManager[SessionManager.kunci_email].toString()
        val builder = StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build())

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val firstLaunch = prefs.getBoolean("firstLaunch", true)

        if (firstLaunch) {
            DialogAlert("Log version ${packageManager.getPackageInfo(packageName, 0).versionName} :\n" +
                    "- Fix bug harga modul add special price\n" +
                    "- Fix bug special price", "attention", this)
            val editor = prefs.edit()
            editor.putBoolean("firstLaunch", false)
            editor.apply()
        }

        cekPermission()
        cekDefaultPassword()

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                bottom_navigation_sales -> {
                    loadSalesFragment(savedInstanceState)
                }
                bottom_navigation_warehouse -> {
                    loadWarehouseFragment(savedInstanceState)
                }
                bottom_navigation_admin -> {
                    loadAdminFragment(savedInstanceState)
                }
                bottom_navigation_profil -> {
                    loadProfilFragment(savedInstanceState)
                }
            }
            true
        }
        if  (savedInstanceState == null){
            binding.bottomNavigation.selectedItemId = bottom_navigation_sales
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setMessage("Yakin ingin keluar?")
                .setCancelable(false)
                .setPositiveButton("Ya") { _, _ ->
                    exit()
                }
                .setNegativeButton("Tidak", null)
                .show()
    }

    fun exit(){
        if (broadcastReceiver != null) {
            this.unregisterReceiver(broadcastReceiver)
        }
        this.finish()
    }

    private fun loadSalesFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, SalesFragment())
                    .commit()

            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, idFirebase)
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Press button navigation marketing")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        }
    }

    private fun loadWarehouseFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, WarehouseFragment())
                    .commit()

            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, idFirebase)
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Press button navigation warehouse")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        }
    }

    private fun loadAdminFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, AdminFragment())
                    .commit()

            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, idFirebase)
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Press button navigation admin")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        }
    }

    private fun loadProfilFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(main_container, ProfilFragment())
                    .commit()

            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, idFirebase)
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Press button navigation profil")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        }
    }

    private fun cekPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v(::HomeActivity.name, "Permission is granted")
        } else {
            Log.v(::HomeActivity.name, "Permission is not granted")
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    private fun cekDefaultPassword(){
        if(pass.equals("masuya123", false)){
            DialogAlert(getString(R.string.peringatan_password), "attention", this)
        }
    }
}
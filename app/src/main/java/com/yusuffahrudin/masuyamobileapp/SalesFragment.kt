package com.yusuffahrudin.masuyamobileapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import com.yusuffahrudin.masuyamobileapp.adapter.AdapterMain
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.databinding.FragmentHomeBinding
import com.yusuffahrudin.masuyamobileapp.util.DataHelper
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList

class SalesFragment: Fragment() {

    private lateinit var carouselView: CarouselView
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var adapterMain: AdapterMain
    private lateinit var sessionManager: SessionManager
    private lateinit var progressBar: ProgressBar
    private lateinit var level: String
    private lateinit var kdkota: String
    private lateinit var db: DataHelper
    private var listAks: ArrayList<UserAkses> = ArrayTampung.getListAkses()
    private lateinit var a: Server
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root
        db = DataHelper(requireContext())
        progressBar = binding.progressbar
        gridLayoutManager = GridLayoutManager(context, 3)
        binding.rvMain.layoutManager = gridLayoutManager
        carouselView = binding.carousel

        progressBar.visibility = View.VISIBLE
        if (listAks.isEmpty()){
            doAsync {
                listAks.addAll(db.getAllData())
                println(db.getAllData())
                uiThread {
                    progressBar.visibility = View.GONE
                    adapterMain = AdapterMain(requireActivity(), resources.getStringArray(R.array.gv_sales_text), resources.obtainTypedArray(R.array.gv_sales_img), listAks)
                    binding.rvMain.adapter = adapterMain
                }
            }
        } else {
            adapterMain = AdapterMain(requireActivity(), resources.getStringArray(R.array.gv_sales_text), resources.obtainTypedArray(R.array.gv_sales_img), listAks)
            binding.rvMain.adapter = adapterMain
            progressBar.visibility = View.GONE
        }

        binding.tvVersion.text = "Masuya v"+applicationVersionName()

        sessionManager = SessionManager(context)
        val user = sessionManager.userDetails
        level = user[SessionManager.level].toString()
        kdkota = user[SessionManager.kdkota].toString()
        a = Server(kdkota)

        binding.tvUser.text = user[SessionManager.kunci_email]?.toUpperCase(Locale.getDefault())
        binding.tvKota.text = user[SessionManager.kdkota]?.toUpperCase(Locale.getDefault())
        carouselView.pageCount = banner().size
        carouselView.setImageListener(imageListener)

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //Programmatically get the current version Name
    private fun applicationVersionName(): String{
        val packageInfo = activity?.packageManager?.getPackageInfo(activity?.packageName.toString(), 0)
        return packageInfo?.versionName.toString()
    }

    private fun banner(): List<String>{
        val list = mutableListOf("${a.URL_IMAGE()}assets/banner1.jpg",
                "${a.URL_IMAGE()}assets/banner2.jpg",
                "${a.URL_IMAGE()}assets/banner3.jpg",
                "${a.URL_IMAGE()}assets/banner4.jpg",
                "${a.URL_IMAGE()}assets/banner5.jpg",
                "${a.URL_IMAGE()}assets/banner6.jpg",
                "${a.URL_IMAGE()}assets/banner7.jpg",
                "${a.URL_IMAGE()}assets/banner8.jpg",
                "${a.URL_IMAGE()}assets/banner9.jpg",
                "${a.URL_IMAGE()}assets/banner10.jpg",
                "${a.URL_IMAGE()}assets/banner11.jpg",
                "${a.URL_IMAGE()}assets/banner12.jpg",
                "${a.URL_IMAGE()}assets/banner13.jpg",
                "${a.URL_IMAGE()}assets/banner14.jpg",
                "${a.URL_IMAGE()}assets/banner15.jpg")

        return  list
    }

    private var imageListener: ImageListener = ImageListener { position, imageView ->
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        Glide.with(requireContext())
                .load(banner()[position])
                .apply(RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.drawable.img_not_found))
                .into(imageView)
    }
}
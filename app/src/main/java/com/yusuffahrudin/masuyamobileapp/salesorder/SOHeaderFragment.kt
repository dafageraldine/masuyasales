package com.yusuffahrudin.masuyamobileapp.salesorder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.BuildConfig
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.GudangJualResponse
import com.yusuffahrudin.masuyamobileapp.data.Result
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.data.sales_order.NoteOtorisasi
import com.yusuffahrudin.masuyamobileapp.data.sales_order.NoteOtorisasiResponse
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder
import com.yusuffahrudin.masuyamobileapp.databinding.*
import com.yusuffahrudin.masuyamobileapp.firebase.SendNotification
import com.yusuffahrudin.masuyamobileapp.util.DataHelper
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SOHeaderFragment: Fragment() {

    private lateinit var nobukti: String
    private lateinit var nmsales: String
    private lateinit var userKota: String
    private lateinit var kdkota: String
    private lateinit var level: String
    private lateinit var kdlevelManager: String
    private lateinit var kdlevelSpv: String
    private lateinit var user: String
    private val kdlevelSpvAR = "10101020"
    private val kdlevelStaffAR = "1010102010"
    private var underCost = false
    private var underBottomSP = false
    private var underBottomSO = false
    private lateinit var listHeader: ArrayList<SalesOrder>
    private lateinit var sessionManager: SessionManager
    private lateinit var listGd: MutableList<String>
    private lateinit var adapterKdGd: ArrayAdapter<String>
    private lateinit var alasanARArray: MutableList<String>
    private lateinit var adapterAlasanAR: ArrayAdapter<String>
    private lateinit var alasanSLArray: MutableList<String>
    private lateinit var adapterAlasanSL: ArrayAdapter<String>
    private var listAlasan: ArrayList<NoteOtorisasi> = ArrayList()
    private lateinit var bindingDialog: DialogOtorisasiSoBinding
    private val REQUESTCAMERA = 2
    private val REQUESTGALLERY = 3
    private val REQUESTMULTIPLEPERMISSIONS = 4
    private val REQUESTPDF = 5
    private var encodePDF = ""
    private lateinit var imagePath: String
    private var photoFile: File? = null
    private lateinit var db: DataHelper
    private var listAkses : java.util.ArrayList<UserAkses> = ArrayTampung.getListAkses()
    private var _binding: FragmentSoHeaderBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("RestrictedApi")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSoHeaderBinding.inflate(inflater, container, false)
        val rootView = binding.root
        db = DataHelper(requireContext())

        sessionManager = SessionManager(this.context)
        val session = sessionManager.userDetails
        userKota = session[SessionManager.kota].toString()
        kdkota = session[SessionManager.kdkota].toString()
        level = session[SessionManager.level].toString()
        user = session[SessionManager.kunci_email].toString()
        if (session[SessionManager.underCost].toString() == "1") underCost = true
        if (session[SessionManager.underBottomSP].toString() == "1") underBottomSP = true
        if (session[SessionManager.underBottomSO].toString() == "1") underBottomSO = true
        listHeader = ArrayTampung.getListHeaderSO()

        val i = this.activity?.intent
        nobukti = i?.extras!!.getString("nobukti").toString()
        nmsales = i.extras!!.getString("nmsales").toString()

        if (listAkses.isEmpty()){
            doAsync {
                binding.progressbar.visibility = View.VISIBLE
                listAkses.addAll(db.getAllData())
                uiThread {
                    binding.progressbar.visibility = View.GONE
                }
            }
        }

        prepareLayout()

        binding.edtTglorder.setOnClickListener { view -> onClick(view) }
        binding.edtTglkirim.setOnClickListener { view -> onClick(view) }
        binding.edtTglPo.setOnClickListener { view -> onClick(view) }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val listCart = ArrayTampung.getListChart()
            val rb = rootView.findViewById<RadioButton>(checkedId)
            if (listHeader[0].jnsjualtax != rb.text && listCart.size > 0) {
                DialogAlert("Status pajak tidak dapat diubah jika item belum kosong", "attention", this.requireActivity())
                setRadio()
            }
        }

        binding.imgClose.setOnClickListener {
            deleteImagePO(listHeader[0].fileName)
        }

        binding.btnImageFilePo.setOnClickListener {
            dialogFilePO(binding.btnImageFilePo.text.toString())
        }

        binding.imgAdd.setOnClickListener {
            if (checkAndRequestPermissions()){
                uploadPODialog()
            }
        }

        binding.btnAccept.setOnClickListener { bottomOto(binding.btnAccept.text.toString()) }
        binding.btnReject.setOnClickListener { bottomOto(binding.btnReject.text.toString()) }

        binding.cbLunas.setOnClickListener {
            dialogInputPelunasan(binding.cbLunas.isChecked)
        }

        /*binding.cbAuthoUc.setOnClickListener {
            if(listHeader[0].status!!.contains("harga pokok")){
                updateAutho(binding.cbAuthoUc.isChecked, "UC","", "", listHeader[0].nmcust.toString())
            }
        }

        binding.cbAuthoUb.setOnClickListener {
            if(listHeader[0].status!!.contains("!") && listHeader[0].ARSL.equals("1")){
                if (binding.cbAuthoAr.isChecked) {
                    updateAutho(binding.cbAuthoUb.isChecked, "UB", binding.actNoteUb.text.toString(), "", listHeader[0].nmcust.toString())
                } else {
                    DialogAlert("Diperlukan otorisasi AR lebih dulu untuk pengecekan status customer!", "attention", this.requireActivity())
                }
            } else {
                updateAutho(binding.cbAuthoUb.isChecked, "UB", binding.actNoteUb.text.toString(), "", listHeader[0].nmcust.toString())
            }
        }

        binding.cbAuthoAr.setOnClickListener {
            if (binding.actNoteAr.text.toString().equals("") || binding.actNoteAr.text.toString().equals("null")){
                binding.cbAuthoAr.isChecked = false
                DialogAlert("Kasih alasan dulu baru otorisasi..!", "attention", this.requireActivity())
            } else {
                val notif = SendNotification()
                var lain = false
                for (index in 0 until listNote.size) {
                    if (binding.actNoteAr.text.toString().equals(listNote.get(index).note, true) && listNote.get(index).arsl == true) {
                        notif.pushNotif(requireContext(), kdkota,
                                "Ada SO yang perlu diotorisasi!",
                                "${listHeader[0].nobukti} - ${listHeader[0].nmcust}",
                                "SO",
                                "'$kdlevelManager','$kdlevelSpv'")
                    }
                    if (binding.actNoteAr.text.toString().equals(listNote.get(index).note, true)) {
                        lain = true
                    }
                }
                if (!lain) {
                    notif.pushNotif(requireContext(), kdkota,
                            "Ada SO yang perlu diotorisasi!",
                            "${listHeader[0].nobukti} - ${listHeader[0].nmcust}",
                            "SO",
                            "'$kdlevelManager','$kdlevelSpv'")
                }
                updateAutho(binding.cbAuthoAr.isChecked, "AR", binding.actNoteAr.text.toString(), binding.edtTglNoteAr.text.toString(), listHeader[0].nmcust.toString())
            }
        }

        binding.edtAlasanOdar.setOnItemClickListener { parent, view, position, id ->
            Toast.makeText(requireContext(), binding.actNoteAr.adapter.getItem(position).toString(), Toast.LENGTH_LONG).show()
            if(binding.actNoteAr.adapter.getItem(position).toString().equals("Janji bayar", false)){
                binding.edtTglNoteAr.visibility = View.VISIBLE
                binding.edtTglNoteAr.setText("")
            } else {
                binding.edtTglNoteAr.visibility = View.GONE
            }
        }*/

        return rootView
    }

    private fun prepareLayout(){
        binding.imgClose.visibility = View.GONE
        binding.edtNobukti.setText(nobukti)
        getGudang(requireActivity())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUESTCAMERA && resultCode == RESULT_OK) {
            binding.btnImageFilePo.text = listHeader[0].fileName
            uploadImagePO(jpgToBase64(photoFile!!.absolutePath))
        }
        if (requestCode == REQUESTGALLERY && resultCode == RESULT_OK) {
            val selectedImage = data?.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = activity!!.contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            cursor?.moveToFirst()
            val columnIndex = cursor!!.getColumnIndex(filePathColumn[0])
            val imgDecodableString = cursor.getString(columnIndex)
            cursor.close()
            photoFile = File(selectedImage.path.toString())
            binding.btnImageFilePo.text = listHeader[0].fileName
            uploadImagePO(jpgToBase64(imgDecodableString))
        }
        if (requestCode == REQUESTPDF && resultCode == RESULT_OK && data != null) {
            val path = data.data as Uri
            val inputStream = context!!.contentResolver.openInputStream(path)
            encodePDF = Base64.encodeToString(inputStream!!.readBytes(), Base64.DEFAULT)
            println("--------------------------------------------- encodePDF $encodePDF")
            binding.btnImageFilePo.text = listHeader[0].fileName
            uploadImagePO(encodePDF)
        }
    }

    private fun setRadio(){
        when(listHeader[0].jnsjualtax){
            "PPN" -> binding.radioPpn.isChecked = true
            "PNBKP" -> binding.radioPnbkp.isChecked = true
            "PBBS" -> binding.radioPbbs.isChecked = true
        }
    }

    fun getHeader(){
        var statuspajak = ""
        var jnsJualTaxAngka = "00"
        when {
            binding.radioPpn.isChecked -> {
                statuspajak = binding.radioPpn.text.toString()
                jnsJualTaxAngka = "01"
            }
            binding.radioPnbkp.isChecked -> {
                statuspajak = binding.radioPnbkp.text.toString()
                jnsJualTaxAngka = "02"
            }
            binding.radioPbbs.isChecked -> {
                statuspajak = binding.radioPbbs.text.toString()
                jnsJualTaxAngka = "03"
            }
        }
        listHeader[0].jnsjualtax = statuspajak
        listHeader[0].kodeTax = jnsJualTaxAngka
        listHeader[0].kdgd = binding.spinKdgd.selectedItem.toString()
        listHeader[0].tglcreate = binding.edtTglorder.text.toString()
        listHeader[0].tglkirim = binding.edtTglkirim.text.toString()
        listHeader[0].tglPO = binding.edtTglPo.text.toString()
        listHeader[0].noPO = binding.edtNomorpo.text.toString()
        listHeader[0].orderby = binding.edtOrderby.text.toString()
        listHeader[0].ket1 = binding.edtCetaknote.text.toString()
        listHeader[0].ket2 = binding.edtKet.text.toString()
        println("")
        println("---------------------------------------")
        println("photofile $photoFile")
        println("encodePDF $encodePDF")
        println("---------------------------------------")
        println("")
        if (photoFile != null || encodePDF != ""){
            listHeader[0].imgPO = binding.btnImageFilePo.text.toString()
        } else {
            listHeader[0].imgPO = ""
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClick(v: View) {
        // Get Current Date
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)+1
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        c.add(Calendar.DAY_OF_MONTH, 7)
        if (v === binding.edtTglorder) {
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                binding.edtTglorder.setText("$year-${if(monthOfYear+1 < 10) "0${monthOfYear+1}" else monthOfYear+1}-${if(dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth}")
            }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
        if (v === binding.edtTglkirim) {
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                binding.edtTglkirim.setText("$year-${if(monthOfYear+1 < 10) "0${monthOfYear+1}" else monthOfYear+1}-${if(dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth}")
            }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
        if (v === binding.edtTglPo) {
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                binding.edtTglPo.setText("$year-${if(monthOfYear+1 < 10) "0${monthOfYear+1}" else monthOfYear+1}-${if(dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth}")
            }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
    }

    //fungsi untuk select data dari database
    @SuppressLint("RestrictedApi", "SetTextI18n")
    fun selectSalesOrder() {
        binding.progressbar.visibility = View.VISIBLE
        val model = ViewModelProviders.of(this).get(DetailSOViewModel::class.java)
        model.getHeader(requireActivity(), nobukti).observe(this, { headerList: ArrayList<SalesOrder> ->
            run {
                listHeader.addAll(headerList)
                for (i in headerList.indices) {
                    when (headerList[i].kdLevel.toString().length) {
                        10 -> {
                            kdlevelManager = headerList[i].kdLevel.toString().substring(0, headerList[i].kdLevel.toString().length - 4)
                            kdlevelSpv = headerList[i].kdLevel.toString().substring(0, headerList[i].kdLevel.toString().length - 2)
                        }
                        8 -> {
                            kdlevelManager = headerList[i].kdLevel.toString().substring(0, headerList[i].kdLevel.toString().length - 2)
                            kdlevelSpv = headerList[i].kdLevel.toString()
                        }
                        6 -> {
                            kdlevelManager = headerList[i].kdLevel.toString()
                            kdlevelSpv = "${headerList[i].kdLevel.toString()}10"
                        }
                        else -> {
                            kdlevelManager = "101020"
                            kdlevelSpv = "10102010"
                        }
                    }
                    println("--------------------- kdlevel pembuat ${headerList[i].kdLevel}")
                    println("--------------------- kdlevelManager $kdlevelManager")
                    println("--------------------- kdlevelSpv $kdlevelSpv")

                    var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    var dateCreate: Date? = null
                    var dateKirim: Date? = null
                    var datePO: Date? = null
                    try {
                        dateCreate = sdf.parse(headerList[i].tglcreate.toString())
                        dateKirim = sdf.parse(headerList[i].tglkirim.toString())
                        if (headerList[i].tglPO != null) datePO = sdf.parse(headerList[i].tglPO.toString())
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }

                    sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val tglCreate = sdf.format(dateCreate!!)
                    val tglKirim = sdf.format(dateKirim!!)
                    val tglPo = if (headerList[i].tglPO != null) sdf.format(datePO!!) else ""
                    when {
                        headerList[i].jnsjualtax.equals("PPN", ignoreCase = true) -> binding.radioPpn.isChecked = true
                        headerList[i].jnsjualtax.equals("PNBKP", ignoreCase = true) -> binding.radioPnbkp.isChecked = true
                        headerList[i].jnsjualtax.equals("PBBS", ignoreCase = true) -> binding.radioPbbs.isChecked = true
                    }
                    binding.spinKdgd.setSelection(adapterKdGd.getPosition(headerList[i].kdgd))

                    binding.tvStatus.text = headerList[i].statusorder
                    binding.edtCust.setText(headerList[i].nmcust)
                    binding.edtTglorder.setText(tglCreate)
                    binding.edtTglkirim.setText(tglKirim)
                    binding.edtNomorpo.setText(headerList[i].noPO)
                    binding.edtTglPo.setText(tglPo)
                    binding.edtCetaknote.setText(headerList[i].ket1)
                    binding.edtKet.setText(headerList[i].ket2)
                    binding.edtOrderby.setText(headerList[i].orderby)
                    binding.edtCreateby.setText(headerList[i].createby)
                    binding.btnImageFilePo.text = headerList[i].filePO
                    if (headerList[i].status.toString().equals("Verified", true) || headerList[i].status.toString() == "") {
                        binding.cbOtoOdar.visibility = View.GONE
                        binding.tvAlasanOdar.visibility = View.GONE
                        binding.tvKetOdar.visibility = View.GONE
                        binding.tvTglOdar.visibility = View.GONE
                        binding.edtAlasanOdar.visibility = View.GONE
                        binding.edtKetOdar.visibility = View.GONE
                        binding.edtTglOdar.visibility = View.GONE

                        binding.cbOtoOdsl.visibility = View.GONE
                        binding.tvAlasanOdsl.visibility = View.GONE
                        binding.tvKetOdsl.visibility = View.GONE
                        binding.edtAlasanOdsl.visibility = View.GONE
                        binding.edtKetOdsl.visibility = View.GONE

                        binding.btnAccept.visibility = View.GONE
                        binding.btnReject.visibility = View.GONE
                    }
                    println("---- ODAR ${headerList[i].otoODAR}")
                    println("---- ODSL ${headerList[i].otoODSL}")
                    binding.cbOtoOdar.isChecked = headerList[i].otoODAR.toString() == "1"
                    binding.cbOtoOdsl.isChecked = headerList[i].otoODSL.toString() == "1"
                    binding.cbLunas.isChecked = headerList[i].lunas
                    if (level.equals(kdlevelSpvAR) || level.equals(kdlevelStaffAR)) {
                        if  (listHeader[0].lamaKredit == "-1" && !listHeader[0].lunas) {
                            binding.cbLunas.isEnabled = true
                        }
                    } else
                        binding.cbLunas.isEnabled = false
                    if (headerList[i].ketODAR.toString() == "null") binding.edtKetOdar.setText("")
                    else binding.edtKetOdar.setText(headerList[i].ketODAR.toString())
                    if (headerList[i].ketODSL.toString() == "null") binding.edtKetOdsl.setText("")
                    else binding.edtKetOdsl.setText(headerList[i].ketODSL.toString())

                    if (headerList[i].alasanODAR.toString() == "null") binding.edtAlasanOdar.setText("")
                    else binding.edtAlasanOdar.setText(headerList[i].alasanODAR.toString())
                    if (headerList[i].alasanODSL.toString() == "null") binding.edtAlasanOdsl.setText("")
                    else binding.edtAlasanOdsl.setText(headerList[i].alasanODSL.toString())

                    if (headerList[i].alasanODAR.toString().equals("Schedule pembayaran mundur", true)) {
                        binding.edtTglOdar.visibility = View.VISIBLE
                        binding.edtTglOdar.setText(headerList[i].tglAlasanODAR)
                    } else {
                        binding.tvTglOdar.visibility = View.GONE
                        binding.edtTglOdar.visibility = View.GONE
                    }
                    binding.tvAlasanOdar.text = "${resources.getString(R.string.oto_by)} ${headerList[i].otoODARby.toString().toUpperCase(Locale.getDefault())}"
                    binding.tvAlasanOdsl.text = "${resources.getString(R.string.oto_by)} ${headerList[i].otoODSLby.toString().toUpperCase(Locale.getDefault())}"
                }

                when {
                    binding.tvStatus.text == "OPEN" -> {
                        binding.edtNobukti.isFocusable = false
                        binding.edtNobukti.isEnabled = false
                        binding.tvStatuskirim.text = getString(R.string.not_yet)
                        binding.imgStatus.setImageResource(R.drawable.data_completed)
                        binding.edtTglorder.isFocusableInTouchMode = false
                        binding.edtTglorder.isEnabled = true
                        binding.edtTglkirim.isFocusableInTouchMode = false
                        binding.edtTglkirim.isEnabled = true
                        binding.edtNomorpo.isFocusableInTouchMode = true
                        binding.edtTglPo.isFocusableInTouchMode = false
                        binding.edtTglPo.isEnabled = true
                        binding.edtOrderby.isFocusableInTouchMode = true
                        binding.edtCetaknote.isFocusableInTouchMode = true
                        binding.edtKet.isFocusableInTouchMode = true
                        binding.edtCreateby.isEnabled = false
                        binding.radioPpn.isEnabled = true
                        binding.radioPnbkp.isEnabled = true
                        binding.radioPbbs.isEnabled = true
                        binding.spinKdgd.isEnabled = true
                        if (binding.btnImageFilePo.text == "" || binding.btnImageFilePo.text == "null") {
                            binding.imgClose.visibility = View.GONE
                            binding.imgAdd.visibility = View.VISIBLE
                        } else {
                            binding.imgClose.visibility = View.VISIBLE
                            binding.imgAdd.visibility = View.GONE
                        }
                        println("------------------------------ level user $level")
                        println("------------------------------ kdlevelSpvAR $kdlevelSpvAR")
                        println("------------------------------ kdlevelStaffAR $kdlevelStaffAR")

                        //---------------- default set false ----------------------------
                        binding.cbOtoOdsl.isEnabled = false
                        binding.edtAlasanOdsl.isEnabled = false
                        binding.edtKetOdsl.isEnabled = false

                        binding.cbOtoOdar.isEnabled = false
                        binding.edtAlasanOdar.isEnabled = false
                        binding.edtKetOdar.isEnabled = false
                        binding.edtTglOdar.isEnabled = false

                        binding.btnAccept.isEnabled = false
                        binding.btnReject.isEnabled = false
                        //----------------------------------------------------------------

                        if (listHeader[0].status!!.contains("!")) {
                            if (listHeader[0].otoODAR == "1" && listHeader[0].ARSL.equals("1")) {
                                binding.btnAccept.visibility = View.GONE
                                binding.btnReject.visibility = View.GONE
                            } else if (listHeader[0].otoODAR == "1" && listHeader[0].ARSL.equals("0")) {
                                binding.btnAccept.visibility = View.GONE
                                binding.btnReject.visibility = View.GONE
                            } else if (listHeader[0].otoODAR == "0") {
                                binding.btnAccept.visibility = View.GONE
                                binding.btnReject.visibility = View.GONE
                            } else {
                                if (level.equals(kdlevelSpv, true) || level.equals(kdlevelManager, true)){
                                    DialogAlert("Diperlukan otorisasi AR lebih dulu untuk pengecekan status customer!", "attention", this.requireActivity())
                                    binding.btnAccept.visibility = View.GONE
                                    binding.btnReject.visibility = View.GONE
                                } else {
                                    binding.btnAccept.visibility = View.GONE
                                    binding.btnReject.visibility = View.GONE
                                }
                            }
                        } else {
                            binding.btnAccept.visibility = View.GONE
                            binding.btnReject.visibility = View.GONE
                        }
                    }
                    binding.tvStatus.text == "PENDING" -> {
                        binding.edtNobukti.isFocusable = false
                        binding.edtNobukti.isEnabled = false
                        binding.tvStatuskirim.text = getString(R.string.partially_ship)
                        binding.imgStatus.setImageResource(R.drawable.data_pending)
                        binding.edtTglorder.isFocusable = false
                        binding.edtTglorder.isEnabled = false
                        binding.edtTglkirim.isFocusable = false
                        binding.edtTglkirim.isEnabled = false
                        binding.edtTglPo.isFocusable = false
                        binding.edtTglPo.isEnabled = false
                        binding.edtNomorpo.isFocusable = false
                        binding.edtOrderby.isFocusable = false
                        binding.edtCetaknote.isFocusable = false
                        binding.edtKet.isFocusable = false
                        binding.edtCreateby.isEnabled = false
                        binding.radioPpn.isEnabled = false
                        binding.radioPnbkp.isEnabled = false
                        binding.radioPbbs.isEnabled = false
                        binding.spinKdgd.isEnabled = false
                        binding.imgAdd.visibility = View.GONE
                        binding.imgClose.visibility = View.GONE

                        //---------------- default set false ----------------------------
                        binding.cbOtoOdsl.isEnabled = false
                        binding.edtAlasanOdsl.isEnabled = false
                        binding.edtKetOdsl.isEnabled = false

                        binding.cbOtoOdar.isEnabled = false
                        binding.edtAlasanOdar.isEnabled = false
                        binding.edtKetOdar.isEnabled = false
                        binding.edtTglOdar.isEnabled = false

                        binding.btnAccept.isEnabled = false
                        binding.btnReject.isEnabled = false
                        binding.cbLunas.isEnabled = false
                        //----------------------------------------------------------------

                        if (listHeader[0].status!!.contains("!")) {
                            if (listHeader[0].otoODAR == "1" && listHeader[0].ARSL.equals("1")) {
                                binding.btnAccept.visibility = View.GONE
                                binding.btnReject.visibility = View.GONE
                            } else if (listHeader[0].otoODAR == "1" && listHeader[0].ARSL.equals("0")) {
                                binding.btnAccept.visibility = View.GONE
                                binding.btnReject.visibility = View.GONE
                            } else if (listHeader[0].otoODAR == "0") {
                                binding.btnAccept.visibility = View.GONE
                                binding.btnReject.visibility = View.GONE
                            } else {
                                if (level.equals(kdlevelSpv, true) || level.equals(kdlevelManager, true)){
                                    DialogAlert("Diperlukan otorisasi AR lebih dulu untuk pengecekan status customer!", "attention", this.requireActivity())
                                    binding.btnAccept.visibility = View.GONE
                                    binding.btnReject.visibility = View.GONE
                                } else {
                                    binding.btnAccept.visibility = View.GONE
                                    binding.btnReject.visibility = View.GONE
                                }
                            }
                        } else {
                            binding.btnAccept.visibility = View.GONE
                            binding.btnReject.visibility = View.GONE
                        }
                    }
                    listHeader[0].ket.toString() != "null" -> {
                        binding.edtNobukti.isFocusable = false
                        binding.edtNobukti.isEnabled = false
                        binding.tvStatuskirim.text = "* ${listHeader[0].ket.toString()}"
                        binding.imgStatus.setImageResource(R.drawable.data_closed)
                        binding.edtTglorder.isFocusable = false
                        binding.edtTglorder.isEnabled = false
                        binding.edtTglkirim.isFocusable = false
                        binding.edtTglkirim.isEnabled = false
                        binding.edtTglPo.isFocusable = false
                        binding.edtTglPo.isEnabled = false
                        binding.edtNomorpo.isFocusable = false
                        binding.edtOrderby.isFocusable = false
                        binding.edtCetaknote.isFocusable = false
                        binding.edtKet.isFocusable = false
                        binding.edtCreateby.isEnabled = false
                        binding.radioPpn.isEnabled = false
                        binding.radioPnbkp.isEnabled = false
                        binding.radioPbbs.isEnabled = false
                        binding.spinKdgd.isEnabled = false
                        binding.imgAdd.visibility = View.GONE
                        binding.imgClose.visibility = View.GONE

                        binding.cbOtoOdsl.isEnabled = false
                        binding.edtAlasanOdsl.isEnabled = false
                        binding.edtKetOdsl.isEnabled = false

                        binding.cbOtoOdar.isEnabled = false
                        binding.edtAlasanOdar.isEnabled = false
                        binding.edtKetOdar.isEnabled = false
                        binding.edtTglOdar.isEnabled = false
                        binding.cbLunas.isEnabled = false

                        binding.btnAccept.visibility = View.GONE
                        binding.btnReject.visibility = View.GONE
                    }
                    else -> {
                        binding.edtNobukti.isFocusable = false
                        binding.edtNobukti.isEnabled = false
                        binding.tvStatuskirim.text = getString(R.string.fully_ship)
                        binding.imgStatus.setImageResource(R.drawable.data_closed)
                        binding.edtTglorder.isFocusable = false
                        binding.edtTglorder.isEnabled = false
                        binding.edtTglkirim.isFocusable = false
                        binding.edtTglkirim.isEnabled = false
                        binding.edtTglPo.isFocusable = false
                        binding.edtTglPo.isEnabled = false
                        binding.edtNomorpo.isFocusable = false
                        binding.edtOrderby.isFocusable = false
                        binding.edtCetaknote.isFocusable = false
                        binding.edtKet.isFocusable = false
                        binding.edtCreateby.isEnabled = false
                        binding.radioPpn.isEnabled = false
                        binding.radioPnbkp.isEnabled = false
                        binding.radioPbbs.isEnabled = false
                        binding.spinKdgd.isEnabled = false
                        binding.imgAdd.visibility = View.GONE
                        binding.imgClose.visibility = View.GONE

                        binding.cbOtoOdsl.isEnabled = false
                        binding.edtAlasanOdsl.isEnabled = false
                        binding.edtKetOdsl.isEnabled = false

                        binding.cbOtoOdar.isEnabled = false
                        binding.edtAlasanOdar.isEnabled = false
                        binding.edtKetOdar.isEnabled = false
                        binding.edtTglOdar.isEnabled = false
                        binding.cbLunas.isEnabled = false

                        binding.btnAccept.visibility = View.GONE
                        binding.btnReject.visibility = View.GONE
                    }
                }
                (activity as DetailSOActivity).setView()
                binding.progressbar.visibility = View.GONE
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun bottomOto(button: String){
        val dialogOto = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        bindingDialog = DialogOtorisasiSoBinding.inflate(layoutInflater)
        val bottomLayout = bindingDialog.root
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.show_from_bottom)
        dialogOto.setContentView(bottomLayout)

        if (button.equals(binding.btnAccept.text.toString(), true)) {
            if (level == "10101020" || level == "1010102010") getAlasanARAccept(requireActivity())
            else if (level == "101020" || level == "10102010" || level == "101030" || level == "10103010") getAlasanSLAccept(requireActivity())
        }
        else if (button.equals(binding.btnReject.text.toString(), true)) getAlasanARReject(requireActivity())
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)+1
        val day = c.get(Calendar.DAY_OF_MONTH)
        bindingDialog.edtTglAlasan.setText("$year-${if (month < 10) "0$month" else month}-${if (day < 10) "0$day" else day}")
        bindingDialog.edtTglAlasan.visibility = View.GONE
        bindingDialog.selectAlasanSo.setOnClickListener { bindingDialog.actAlasanOto.showDropDown() }
        bindingDialog.actAlasanOto.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(requireContext(), bindingDialog.actAlasanOto.adapter.getItem(position).toString(), Toast.LENGTH_LONG).show()
            if(bindingDialog.actAlasanOto.adapter.getItem(position).toString().equals("Schedule pembayaran mundur", false)){
                bindingDialog.edtTglAlasan.visibility = View.VISIBLE
                //bindingDialog.edtTglAlasan.setText("")
            } else {
                bindingDialog.edtTglAlasan.visibility = View.GONE
            }
        }

        bindingDialog.edtTglAlasan.setOnClickListener {
            val cal = Calendar.getInstance()
            val mYear = cal.get(Calendar.YEAR)
            val mMonth = cal.get(Calendar.MONTH)+1
            val mDay = cal.get(Calendar.DAY_OF_MONTH)
            cal.add(Calendar.DAY_OF_MONTH, 30)
            println("------------- tes")
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                bindingDialog.edtTglAlasan.setText("$year-${if (monthOfYear + 1 < 10) "0${monthOfYear + 1}" else monthOfYear + 1}-${if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth}")
            }, mYear, mMonth, mDay)
            println("------------- tes2")
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.datePicker.maxDate = cal.timeInMillis
            println("------------- tes3")
            datePickerDialog.show()
        }

        bindingDialog.btnCancel.setOnClickListener {
            dialogOto.dismiss()
        }

        bindingDialog.btnOk.setOnClickListener {
            if (bindingDialog.actAlasanOto.text.toString() == "" || bindingDialog.actAlasanOto.text.toString() == "null"){
                DialogAlert("Alasan tidak boleh kosong..!", "attention", requireActivity())
            } else {
                if (button.equals(resources.getString(R.string.accept), true)) {
                    println("----------------------- button Ok diclick")
                    updateAutho(
                        "1",
                        user,
                        bindingDialog.actAlasanOto.text.toString(),
                        bindingDialog.edtKet.text.toString(),
                        bindingDialog.edtTglAlasan.text.toString(),
                        nobukti,
                        binding.edtCust.text.toString()
                    )
                    dialogOto.dismiss()
                }
                else if (button.equals(resources.getString(R.string.reject), true)) {
                    updateAutho(
                        "0",
                        user,
                        bindingDialog.actAlasanOto.text.toString(),
                        bindingDialog.edtKet.text.toString(),
                        bindingDialog.edtTglAlasan.text.toString(),
                        nobukti,
                        binding.edtCust.text.toString()
                    )
                    dialogOto.dismiss()
                }
            }
        }

        bottomLayout.startAnimation(slideUp)
        dialogOto.show()
    }

    @SuppressLint("InflateParams")
    private fun dialogFilePO(fileName: String){
        if (fileName.contains(".jpg")){
            val dialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
            val bindingDialogShowImage = DialogShowImageBinding.inflate(layoutInflater)
            val dialogView = bindingDialogShowImage.root
            dialog.setContentView(dialogView)
            dialog.setTitle(fileName)

            val a = Server(kdkota)
            Glide.with(requireContext())
                    .load(a.URL() + "salesorder/File_PO/$fileName")
                    .apply(RequestOptions()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.drawable.img_not_found)
                            .fitCenter())
                    .into(bindingDialogShowImage.imageView)
            bindingDialogShowImage.progressbar.visibility = View.GONE

            bindingDialogShowImage.btnOk.setOnClickListener { dialog.dismiss() }

            dialog.show()
        } else if (fileName.contains(".pdf")) {
            val a = Server(kdkota)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(a.URL() + "salesorder/File_PO/$fileName"), "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        }
    }

    private fun getGudang(activity: Activity){
        binding.progressbar.visibility = View.VISIBLE
        val kotaUser = when {
            userKota.equals("ALL", ignoreCase = true) -> "'SBY','MLG','MKS','BPN','SMG','YGY'"
            userKota.equals("MLG", ignoreCase = true) -> "'SBY','MLG'"
            else -> "'$userKota'"
        }
        listGd = mutableListOf()
        API.instance().create(ApiService::class.java)
                .getGdgJual(kotaUser, user, level)
                ?.enqueue(object : Callback<GudangJualResponse?> {
                    override fun onFailure(call: Call<GudangJualResponse?>, e: Throwable) {
                        Log.d("Get Gudang Jual", "Error $e")
                        FirebaseCrashlytics.getInstance().recordException(e)
                        DialogAlert(e.message, "error", activity)
                    }

                    override fun onResponse(call: Call<GudangJualResponse?>, response: retrofit2.Response<GudangJualResponse?>) {
                        if (response.code() == 200) {
                            for (i in 0 until response.body()?.result!!.size) {
                                listGd.add(response.body()?.result!![i].kdgd.toString())
                            }
                            adapterKdGd = ArrayAdapter(activity, R.layout.spinner_black, listGd)
                            binding.spinKdgd.adapter = adapterKdGd
                            binding.progressbar.visibility = View.GONE
                            selectSalesOrder()
                        }
                    }
                })
    }

    private fun getAlasanARAccept(activity: Activity){
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            listAlasan.clear()
            alasanARArray = mutableListOf()
            API.instance().create(ApiService::class.java)
                    .noteARAccept
                    ?.enqueue(object : Callback<NoteOtorisasiResponse?> {
                        override fun onFailure(call: Call<NoteOtorisasiResponse?>, e: Throwable) {
                            DialogAlert(e.message, "error", activity)
                            Log.d("Get Note AR", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        override fun onResponse(call: Call<NoteOtorisasiResponse?>, response: retrofit2.Response<NoteOtorisasiResponse?>) {
                            if (response.code() == 200) {
                                listAlasan.addAll(response.body()?.result!!)
                                for (i in 0 until response.body()?.result!!.size) {
                                    alasanARArray.add(response.body()?.result!![i].note.toString())
                                }
                                adapterAlasanAR = ArrayAdapter(activity, R.layout.spinner_black, alasanARArray)
                                bindingDialog.actAlasanOto.setAdapter(adapterAlasanAR)
                                //getNoteSM()
                            }
                        }
                    })
            uiThread { binding.progressbar.visibility = View.GONE }
        }
    }

    private fun getAlasanARReject(activity: Activity){
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            listAlasan.clear()
            alasanARArray = mutableListOf()
            API.instance().create(ApiService::class.java)
                    .noteARReject
                    ?.enqueue(object : Callback<NoteOtorisasiResponse?> {
                        override fun onFailure(call: Call<NoteOtorisasiResponse?>, e: Throwable) {
                            DialogAlert(e.message, "error", activity)
                            Log.d("Get Note AR", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        override fun onResponse(call: Call<NoteOtorisasiResponse?>, response: retrofit2.Response<NoteOtorisasiResponse?>) {
                            if (response.code() == 200) {
                                listAlasan.addAll(response.body()?.result!!)
                                for (i in 0 until response.body()?.result!!.size) {
                                    alasanARArray.add(response.body()?.result!![i].note.toString())
                                }
                                adapterAlasanAR = ArrayAdapter(activity, R.layout.spinner_black, alasanARArray)
                                bindingDialog.actAlasanOto.setAdapter(adapterAlasanAR)
                                //getNoteSM()
                            }
                        }
                    })
            uiThread { binding.progressbar.visibility = View.GONE }
        }
    }

    private fun getAlasanSLAccept(activity: Activity){
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            alasanSLArray = mutableListOf()
            API.instance().create(ApiService::class.java)
                    .noteSLAccept
                    ?.enqueue(object : Callback<NoteOtorisasiResponse?> {
                        override fun onFailure(call: Call<NoteOtorisasiResponse?>, e: Throwable) {
                            DialogAlert(e.message, "error", activity)
                            Log.d("Get Note SL", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        override fun onResponse(call: Call<NoteOtorisasiResponse?>, response: retrofit2.Response<NoteOtorisasiResponse?>) {
                            if (response.code() == 200) {
                                for (i in 0 until response.body()?.result!!.size) {
                                    alasanSLArray.add(response.body()?.result!![i].note.toString())
                                }
                                adapterAlasanSL = ArrayAdapter(activity, R.layout.spinner_black, alasanSLArray)
                                bindingDialog.actAlasanOto.setAdapter(adapterAlasanSL)
                            }
                        }
                    })
            uiThread { binding.progressbar.visibility = View.GONE }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val camerapermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val readpermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        val writepermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val listPermissionsNeeded = java.util.ArrayList<String>()

        if (camerapermission != PackageManager.PERMISSION_GRANTED) { listPermissionsNeeded.add(Manifest.permission.CAMERA) }
        if (readpermission != PackageManager.PERMISSION_GRANTED) { listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE) }
        if (writepermission != PackageManager.PERMISSION_GRANTED) { listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE) }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), listPermissionsNeeded.toTypedArray(), REQUESTMULTIPLEPERMISSIONS)
            return false
        }
        return true
    }

    private fun uploadPODialog() {
        val pictureDialog = AlertDialog.Builder(requireContext())
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf(getString(R.string.select_pdf_from_storage), getString(R.string.select_from_gallery), getString(R.string.select_from_camera))
        pictureDialog.setItems(pictureDialogItems) { _, which ->
            println("================== $which")
            when (which) {
                0 -> openPDFIntent()
                1 -> openGalleryIntent()
                2 -> openCameraIntent()
            }
        }
        pictureDialog.show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCameraIntent() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(pictureIntent.resolveActivity(activity!!.packageManager) != null) {
            val timeStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            listHeader[0].fileName = "IMG-$timeStamp.jpg"
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            var currentPhotoPath: String
            photoFile = File.createTempFile(listHeader[0].fileName.toString(), ".jpg", storageDir).apply { currentPhotoPath = absolutePath }
            photoFile!!.also {
                val photoURI = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", it)
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(pictureIntent, REQUESTCAMERA)
            }
            imagePath = "file:$currentPhotoPath"
            println("-------------------------- image Path $imagePath")
        }
        /*if(pictureIntent.resolveActivity(activity!!.packageManager) != null){
            val timeStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            listHeader[0].fileName = "IMG-$timeStamp"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = context!!.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, listHeader[0].fileName.toString())
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                val image = File(uri!!.path.toString())
                imagePath = "file:" + image.absolutePath
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                startActivityForResult(pictureIntent, REQUESTCAMERA)
            } else {
                val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val photoFile = File.createTempFile(listHeader[0].fileName.toString(), ".jpg", storageDir)
                val photoURI = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", photoFile)
                val image = File(photoURI!!.path.toString())
                imagePath = "file:" + image.absolutePath
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(pictureIntent, REQUESTCAMERA)
            }
        }*/
    }

    private fun openPDFIntent(){
        var pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent = Intent.createChooser(pdfIntent, "Choose a file")
        val timeStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        listHeader[0].fileName = "PDF-$timeStamp.pdf"
        startActivityForResult(pdfIntent, REQUESTPDF)
    }

    private fun openGalleryIntent() {
        //Create an Intent with action as ACTION_PICK
        val galleryIntent= Intent(Intent.ACTION_PICK)
        // Sets the type as image/*. This ensures only components of type image are selected
        galleryIntent.type = "image/*"
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        val timeStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        listHeader[0].fileName = "IMG-$timeStamp.jpg"

        // Launching the Intent
        startActivityForResult(galleryIntent, REQUESTGALLERY)
    }

    private fun jpgToBase64(path: String): String{
        val bm = BitmapFactory.decodeFile(path)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun uploadImagePO(base64: String?) {
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            var imagePO = ""
            if (base64 != null){
                imagePO = base64
            }
            API.instance().create(ApiService::class.java)
                    .uploadImagePO(imagePO, listHeader[0].fileName.toString())
                    ?.enqueue(object : Callback<Result?> {
                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            DialogAlert(e.message, "error", requireActivity())
                            Log.d("Upload Image PO", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        @SuppressLint("RestrictedApi")
                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            if (response.isSuccessful) {
                                if (response.body()?.success == 1) {
                                    binding.imgClose.visibility = View.VISIBLE
                                    binding.imgAdd.visibility = View.GONE
                                } else {
                                    DialogAlert(response.body()?.message.toString(), "error", requireActivity())
                                    binding.imgClose.visibility = View.GONE
                                    binding.imgAdd.visibility = View.VISIBLE
                                }
                            }
                        }
                    })
            uiThread { binding.progressbar.visibility = View.GONE }
        }
    }

    private fun deleteImagePO(filename: String?) {
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            API.instance().create(ApiService::class.java)
                    .deleteImagePO(filename.toString())
                    ?.enqueue(object : Callback<Result?> {
                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            DialogAlert(e.message, "error", requireActivity())
                            Log.d("Delete Image PO", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        @SuppressLint("RestrictedApi")
                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            if (response.isSuccessful) {
                                if (response.body()?.success == 1) {
                                    photoFile!!.delete()
                                    listHeader[0].fileName = ""
                                    binding.btnImageFilePo.text = requireActivity().getString(R.string.foto_po_tidak_ditemukan)
                                    binding.imgClose.visibility = View.GONE
                                    binding.imgAdd.visibility = View.VISIBLE
                                    DialogAlert("Image PO berhasil dihapus", "attention", requireActivity())
                                } else {
                                    DialogAlert(response.body()?.message.toString(), "error", requireActivity())
                                    binding.btnImageFilePo.text = requireActivity().getString(R.string.foto_po_tidak_ditemukan)
                                    binding.imgClose.visibility = View.VISIBLE
                                    binding.imgAdd.visibility = View.GONE
                                }
                            }
                        }
                    })
            uiThread { binding.progressbar.visibility = View.GONE }
        }
    }

    private fun updateAutho(oto: String, who: String, alasan: String, ket: String, tglAlasan: String, nobukti: String, nmcust: String) {
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            val params = HashMap<String?, String?>()
            //JSONArray List Item Order
            params["oto"] = oto
            params["level"] = level
            params["who"] = who
            params["alasan"] = alasan
            params["ket"] = ket
            params["tglAlasan"] = tglAlasan
            params["nobukti"] = nobukti
            API.instance().create(ApiService::class.java)
                    .updateOtoODSO(params)
                    ?.enqueue(object : Callback<Result?> {
                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            binding.progressbar.visibility = View.GONE
                            DialogAlert(e.message, "error", requireActivity())
                        }

                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            if (response.isSuccessful) {
                                val message = response.body()?.message.toString()
                                if (response.body()?.success == 1) {
                                    val notif = SendNotification()
                                    if (oto == "1") {
                                        notif.pushNotifSales(requireContext(), kdkota, "$nobukti - SO Accepted by $who",
                                                nmcust,
                                                "SO",
                                                nmsales)
                                    } else {
                                        notif.pushNotifSales(requireContext(), kdkota, "$nobukti - SO Rejected by $who",
                                                nmcust,
                                                "SO",
                                                nmsales)
                                    }
                                    binding.progressbar.visibility = View.GONE
                                    selectSalesOrder()

                                    DialogAlert(message, "success", requireActivity())
                                } else {
                                    binding.progressbar.visibility = View.GONE
                                    DialogAlert(message, "error", requireActivity())
                                }
                            }
                        }
                    })
        }
    }

    private fun dialogInputPelunasan(isCheck: Boolean) {
        val dialog = BottomSheetDialog(requireContext(), R.style.SheetDialogNotif)
        val bindingDialogPelunasan = DialogYesOrNoBinding.inflate(layoutInflater)
        val bottomLayout = bindingDialogPelunasan.root
        dialog.setContentView(bottomLayout)
        if (isCheck)
            bindingDialogPelunasan.tvMessage.text = "Yakin untuk input pelunasan SO ini?"
        else
            bindingDialogPelunasan.tvMessage.text = "Yakin untuk membatalkan pelunasan SO ini?"

        bindingDialogPelunasan.btnYa.setOnClickListener {
            inputPelunasan(isCheck)
            dialog.dismiss()
        }
        bindingDialogPelunasan.btnTidak.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun inputPelunasan(isCheck: Boolean){
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            var lunas = "0"
            if (isCheck)
                lunas = "1"
            API.instance().create(ApiService::class.java)
                .insertPelunasan(nobukti, lunas, user)
                ?.enqueue(object : Callback<Result?> {
                    override fun onFailure(call: Call<Result?>, e: Throwable) {
                        binding.progressbar.visibility = View.GONE
                        DialogAlert(e.message, "error", requireActivity())
                    }

                    override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                        if (response.isSuccessful) {
                            val message = response.body()?.message.toString()
                            if (response.body()?.success == 1) {
                                val notif = SendNotification()
                                notif.pushNotifSales(requireContext(), kdkota, "$nobukti - Paid off",
                                    listHeader[0].nmcust.toString(),
                                    "SO",
                                    nmsales)
                                binding.progressbar.visibility = View.GONE
                                selectSalesOrder()

                                DialogAlert(message, "success", requireActivity())
                            } else {
                                binding.progressbar.visibility = View.GONE
                                DialogAlert(message, "error", requireActivity())
                            }
                        }
                    }
                })
        }
    }
}
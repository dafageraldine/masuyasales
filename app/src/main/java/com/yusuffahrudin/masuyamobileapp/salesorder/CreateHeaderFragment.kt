package com.yusuffahrudin.masuyamobileapp.salesorder

import android.Manifest
import android.annotation.SuppressLint
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
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.RadioButton
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.BuildConfig
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.customer.CustomerDetailFragment.Companion.EXTRA_CUSTOMER
import com.yusuffahrudin.masuyamobileapp.data.Result
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.GudangJualResponse
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.data.customer.Customer
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder
import com.yusuffahrudin.masuyamobileapp.databinding.*
import com.yusuffahrudin.masuyamobileapp.updatepricelist.ListCustomerViewModel
import com.yusuffahrudin.masuyamobileapp.util.DataHelper
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreateHeaderFragment: Fragment() {
    private lateinit var listGd: MutableList<String>
    private lateinit var listCust: ArrayList<Customer>
    private lateinit var adapterKdGd: ArrayAdapter<String>
    private lateinit var adapterCust: AdapterCustomer
    private lateinit var sessionManager: SessionManager
    private lateinit var kdkota: String
    private lateinit var userKota: String
    private lateinit var user: String
    private lateinit var level: String
    private val listHeader: ArrayList<SalesOrder> = ArrayTampung.getListHeaderSO()
    private val REQUESTCAMERA = 2
    private val REQUESTGALLERY = 3
    private val REQUESTPDF = 5
    private val REQUESTMULTIPLEPERMISSIONS = 4
    private lateinit var imagePath: String
    private var encodePDF = ""
    private var photoFile: File? = null
    private lateinit var db: DataHelper
    private var listAkses : ArrayList<UserAkses> = ArrayTampung.getListAkses()
    private var _binding: FragmentSoHeaderBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("RestrictedApi")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSoHeaderBinding.inflate(inflater, container, false)
        val rootView = binding.root
        db = DataHelper(requireContext())

        sessionManager = SessionManager(this.context)
        val session = sessionManager.userDetails
        kdkota = session[SessionManager.kdkota].toString()
        userKota = session[SessionManager.kota].toString()
        level = session[SessionManager.level].toString()
        user = session[SessionManager.kunci_email].toString()

        if (listAkses.isEmpty()){
            binding.progressbar.visibility = View.VISIBLE
            doAsync {
                listAkses.addAll(db.getAllData())
                uiThread {
                    binding.progressbar.visibility = View.GONE
                }
            }
        }

        prepareLayout()
        getGudang()

        binding.edtCreateby.setText(session[SessionManager.kunci_email].toString())
        val item = SalesOrder()
        listHeader.add(item)
        if(requireActivity().intent.getParcelableExtra<Customer>(EXTRA_CUSTOMER) != null){
            val customer = requireActivity().intent.getParcelableExtra<Customer>(EXTRA_CUSTOMER)!!
            listHeader[0].kdcust = customer.kdcust
            listHeader[0].nmcust = customer.nmcust
            listHeader[0].kdkel = customer.kdkel
            listHeader[0].alm1 = customer.alm1
            listHeader[0].alm2 = customer.alm2
            listHeader[0].alm3 = customer.alm3
            listHeader[0].kdsales = customer.kdsales
            listHeader[0].nmsales = customer.sales
            binding.edtCust.setText(listHeader[0].kdcust)
        }

        binding.edtCust.setOnClickListener { view -> onClick(view) }
        binding.edtTglorder.setOnClickListener { view -> onClick(view) }
        binding.edtTglkirim.setOnClickListener { view -> onClick(view) }
        binding.edtTglPo.setOnClickListener { view -> onClick(view) }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val listCart = ArrayTampung.getListChart()
            val rb = rootView.findViewById<RadioButton>(checkedId)
            if(listHeader[0].jnsjualtax != rb.text && listCart.size > 0){
                DialogAlert("Status pajak tidak dapat diubah jika item belum kosong", "attention", this.requireActivity())
                setRadio()
            }
            when(rb.text){
                "PPN" -> CreateItemFragment.editPpnPersen.setText("10")
                "PNBKP" -> CreateItemFragment.editPpnPersen.setText("0")
                "PBBS" -> CreateItemFragment.editPpnPersen.setText("0")
            }
        }

        binding.btnImageFilePo.setOnClickListener {
            dialogFilePO(binding.btnImageFilePo.text.toString())
        }

        binding.imgAdd.setOnClickListener {
            if (checkAndRequestPermissions()){
                uploadPODialog()
            }
        }

        binding.imgClose.setOnClickListener {
            deleteImagePO(listHeader[0].fileName)
        }

        return rootView
    }

    @SuppressLint("SetTextI18n")
    private fun prepareLayout() {
        binding.imgClose.visibility = View.GONE
        binding.edtNobukti.isFocusable = false
        binding.edtNobukti.isEnabled = false
        binding.edtCreateby.isEnabled = false
        binding.edtNobukti.setText("AUTO")
        binding.cbOtoOdar.visibility = View.GONE
        binding.tvAlasanOdar.visibility = View.GONE
        binding.edtAlasanOdar.visibility = View.GONE
        binding.tvKetOdar.visibility = View.GONE
        binding.edtKetOdar.visibility = View.GONE
        binding.tvTglOdar.visibility = View.GONE
        binding.edtTglOdar.visibility = View.GONE

        binding.cbOtoOdsl.visibility = View.GONE
        binding.tvAlasanOdsl.visibility = View.GONE
        binding.edtAlasanOdsl.visibility = View.GONE
        binding.tvKetOdsl.visibility = View.GONE
        binding.edtKetOdsl.visibility = View.GONE
        binding.btnAccept.visibility = View.GONE
        binding.btnReject.visibility = View.GONE

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)+1
        val day = c.get(Calendar.DAY_OF_MONTH)

        binding.edtTglorder.setText("$year-${if (month < 10) "0$month" else month}-${if (day < 10) "0$day" else day}")
        binding.edtTglPo.setText("$year-${if (month < 10) "0$month" else month}-${if (day < 10) "0$day" else day}")
        c.add(Calendar.DATE, 1)
        binding.edtTglkirim.setText("$year-${if (month < 10) "0$month" else month}-${if (day < 10) "0$day" else day}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUESTPDF && resultCode == RESULT_OK && data != null) {
            val path = data.data as Uri
            val inputStream = context!!.contentResolver.openInputStream(path)
            encodePDF = Base64.encodeToString(inputStream!!.readBytes(), Base64.DEFAULT)
            println("--------------------------------------------- encodePDF $encodePDF")
            binding.btnImageFilePo.text = listHeader[0].fileName
            uploadImagePO(encodePDF)
        }
        if (requestCode == REQUESTCAMERA && resultCode == RESULT_OK) {
            println("--------------------------------------------- photoFile ${photoFile!!.path}")
            binding.btnImageFilePo.text = listHeader[0].fileName
            uploadImagePO(jpgToBase64(photoFile!!.absolutePath))
        }
        if (requestCode == REQUESTGALLERY && resultCode == RESULT_OK) {
            println("========================================== GALERI")
            println("--------------------------------- uri ${data?.data}")

            val selectedImage = data?.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context?.contentResolver?.query(selectedImage!!, filePathColumn, null, null, null)
            cursor?.moveToFirst()
            val columnIndex = cursor!!.getColumnIndex(filePathColumn[0])
            val imgDecodableString = cursor.getString(columnIndex)
            println(imgDecodableString)
            cursor.close()
            photoFile = File(selectedImage?.path.toString())
            binding.btnImageFilePo.text = listHeader[0].fileName
            uploadImagePO(jpgToBase64(imgDecodableString))
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
        listHeader[0].nobukti = binding.edtNobukti.text.toString()
        listHeader[0].jnsjualtax = statuspajak
        listHeader[0].kodeTax = jnsJualTaxAngka
        listHeader[0].kdgd = binding.spinKdgd.selectedItem.toString()
        listHeader[0].kdcust = binding.edtCust.text.toString()
        listHeader[0].tglcreate = binding.edtTglorder.text.toString()
        listHeader[0].tglkirim = binding.edtTglkirim.text.toString()
        listHeader[0].tglPO = binding.edtTglPo.text.toString()
        listHeader[0].noPO = binding.edtNomorpo.text.toString()
        listHeader[0].orderby = binding.edtOrderby.text.toString()
        listHeader[0].ket1 = binding.edtCetaknote.text.toString()
        listHeader[0].ket2 = binding.edtKet.text.toString()
        listHeader[0].statusorder = "OPEN"
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
        if (v === binding.edtCust) {
            dialogPilihCustomer()
        }
        if (v === binding.edtTglorder) {
            // Get Current Date
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)+1
            val mDay = c.get(Calendar.DAY_OF_MONTH)
            c.add(Calendar.DAY_OF_MONTH, 7)
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                binding.edtTglorder.setText("$year-${if (monthOfYear + 1 < 10) "0${monthOfYear + 1}" else monthOfYear + 1}-${if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth}")
            }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
        if (v === binding.edtTglkirim) {
            // Get Current Date
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)+1
            val mDay = c.get(Calendar.DAY_OF_MONTH)
            c.add(Calendar.DAY_OF_MONTH, 7)
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                binding.edtTglkirim.setText("$year-${if (monthOfYear + 1 < 10) "0${monthOfYear + 1}" else monthOfYear + 1}-${if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth}")
            }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
        if (v === binding.edtTglPo) {
            // Get Current Date
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)+1
            val mDay = c.get(Calendar.DAY_OF_MONTH)
            c.add(Calendar.DAY_OF_MONTH, 7)
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                binding.edtTglPo.setText("$year-${if (monthOfYear + 1 < 10) "0${monthOfYear + 1}" else monthOfYear + 1}-${if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth}")
            }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
    }

    @SuppressLint("InflateParams")
    private fun dialogFilePO(fileName: String){
        if (fileName.contains("jpg")){
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
        } else if (fileName.contains("pdf")) {
            val a = Server(kdkota)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(a.URL() + "salesorder/File_PO/$fileName"), "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        }
    }

    private fun getGudang(){
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
                        DialogAlert(e.message, "error", requireActivity())
                        Log.d("Get Gudang Jual", "Error $e")
                        e.printStackTrace()
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }

                    override fun onResponse(call: Call<GudangJualResponse?>, response: retrofit2.Response<GudangJualResponse?>) {
                        if (response.code() == 200) {
                            for (i in 0 until response.body()?.result!!.size) {
                                listGd.add(response.body()?.result!![i].kdgd.toString())
                            }
                            adapterKdGd = ArrayAdapter(requireContext(), R.layout.spinner_black, listGd)
                            binding.spinKdgd.adapter = adapterKdGd
                        }
                    }
                })
        binding.progressbar.visibility = View.GONE
    }

    private fun uploadPODialog(){
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
       if(pictureIntent.resolveActivity(activity!!.packageManager) != null){
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
           /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
               val resolver = context!!.contentResolver
               val contentValues = ContentValues()
               contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, listHeader[0].fileName.toString())
               contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
               contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
               val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
               photoFile = File(uri!!.path.toString())
               imagePath = "file:" + photoFile!!.absolutePath
               println("-------------------------- image Path $imagePath")
               pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
               startActivityForResult(pictureIntent, REQUESTCAMERA)
           } else {
               //val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
               val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
               photoFile = File.createTempFile(listHeader[0].fileName.toString(), ".jpg", storageDir)
               val photoURI = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", photoFile!!)
               val image = File(photoURI!!.path.toString())
               imagePath = "file:" + image.absolutePath
               println("-------------------------- image Path $imagePath")
               pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
               startActivityForResult(pictureIntent, REQUESTCAMERA)
           }*/
       }
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
        val galleryIntent= Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        //val mimeTypes = arrayOf("image/jpeg", "image/png")
        //galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        //galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)
        val timeStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        listHeader[0].fileName = "IMG-$timeStamp.jpg"

        // Launching the Intent
        startActivityForResult(galleryIntent, REQUESTGALLERY)
    }

    private fun jpgToBase64(path: String): String{
        //var byteArray = ByteArray(8192)
        println("========================================== $path")
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(path.toUri(), "r")
            val bm = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor!!.fileDescriptor)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } else {
            val bm = BitmapFactory.decodeFile(path)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }*/
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
            // Parsing any Media type file
            // Parsing any Media type file
            //val requestBody: RequestBody = RequestBody.create(MediaType.parse("*/*"), base64)
            //val fileToUpload = MultipartBody.Part.createFormData("file", imagePO, requestBody)
            //val filename: RequestBody = RequestBody.create(MediaType.parse("text/plain"), imagePO)
            API.instance().create(ApiService::class.java)
                    .uploadImagePO(imagePO, listHeader[0].fileName)
                    ?.enqueue(object : Callback<Result?> {
                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            DialogAlert(e.message, "error", requireActivity())
                            Log.d("Upload Image PO", "Error $e")
                            e.printStackTrace()
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
                            e.printStackTrace()
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

    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        val file = File(fileUri.toString())
        val requestFile = RequestBody.create(MediaType.parse(activity?.contentResolver?.getType(fileUri).toString()), file)
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    private fun checkAndRequestPermissions(): Boolean {
        val camerapermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val readpermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        val writepermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val listPermissionsNeeded = ArrayList<String>()

        if (camerapermission != PackageManager.PERMISSION_GRANTED) { listPermissionsNeeded.add(Manifest.permission.CAMERA) }
        if (readpermission != PackageManager.PERMISSION_GRANTED) { listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE) }
        if (writepermission != PackageManager.PERMISSION_GRANTED) { listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE) }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), listPermissionsNeeded.toTypedArray(), REQUESTMULTIPLEPERMISSIONS)
            return false
        }
        return true
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun dialogPilihCustomer(){
        val dialogPilihCust = Dialog(requireActivity())
        val bindingDialogPilihCust = DialogPilihCustomerBinding.inflate(layoutInflater)
        val bottomSheetLayout = bindingDialogPilihCust.root
        dialogPilihCust.setContentView(bottomSheetLayout)

        listCust = ArrayList()

        bindingDialogPilihCust.progressbar.visibility = View.GONE
        bindingDialogPilihCust.tvTitle.text = "Input Customer"
        bindingDialogPilihCust.searchview.isActivated = true
        bindingDialogPilihCust.searchview.queryHint = resources.getString(R.string.kode_nama_customer)
        bindingDialogPilihCust.searchview.onActionViewExpanded()
        bindingDialogPilihCust.searchview.requestFocus()

        bindingDialogPilihCust.rvMain.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        bindingDialogPilihCust.rvMain.layoutManager = layoutManager
        adapterCust = AdapterCustomer(dialogPilihCust)
        bindingDialogPilihCust.rvMain.adapter = adapterCust

        bindingDialogPilihCust.searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchCustomer(bindingDialogPilihCust.progressbar, query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        bindingDialogPilihCust.btnCancel.setOnClickListener { dialogPilihCust.dismiss() }

        dialogPilihCust.show()
    }

    private inner class AdapterCustomer(private var dialog: Dialog) : RecyclerView.Adapter<AdapterCustomer.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = RvCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.tvKdcust.text = listCust[position].kdcust.toString()
            holder.binding.tvNmcust.text = listCust[position].nmcust.toString()

            holder.binding.cvMain.setOnClickListener {
                listHeader[0].kdcust = listCust[position].kdcust
                listHeader[0].nmcust = listCust[position].nmcust
                listHeader[0].kdkel = listCust[position].kdkel
                listHeader[0].alm1 = listCust[position].alm1
                listHeader[0].alm2 = listCust[position].alm2
                listHeader[0].alm3 = listCust[position].alm3
                listHeader[0].kdsales = listCust[position].kdsales
                listHeader[0].nmsales = listCust[position].sales
                binding.edtCust.setText(listHeader[0].kdcust)
                dialog.dismiss()
            }
        }

        override fun getItemCount(): Int {
            return listCust.size
        }

        inner class ViewHolder(val binding: RvCustomerBinding) : RecyclerView.ViewHolder(binding.root)
    }

    private fun searchCustomer(progressBar: ProgressBar, cust: String){
        progressBar.visibility = View.VISIBLE
        listCust.clear()
        var sales = ""
        if  (level == "1010201010" || level == "1010301010") sales = user
        val model = ViewModelProviders.of(this).get(ListCustomerViewModel::class.java)
        model.getCustomer(requireActivity(), sales, cust, 0).observe(this, { custList: ArrayList<Customer> ->
            run {
                listCust.addAll(custList)
                progressBar.visibility = View.GONE
                adapterCust.notifyDataSetChanged()
            }
        })
    }
/*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(activity?.javaClass?.name, "Permission callback called-------")
        when (requestCode) {
            REQUEST_MULTIPLE_PERMISSIONS -> {
                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.size > 0) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(activity?.javaClass?.name, "read / write services permission granted")
                    } else {
                        Log.d(activity?.javaClass?.name, "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)
                                || ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                            showDialogOK("Service Permissions are required for this app",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                            DialogInterface.BUTTON_NEGATIVE ->
                                                // proceed with logic by disabling the related features or quit the app.
                                                activity?.finish()
                                        }
                                    })
                        } else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }
    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show()
    }

    private fun explain(msg: String) {
        val dialog = android.support.v7.app.AlertDialog.Builder(requireContext())
        dialog.setMessage(msg)
                .setPositiveButton("Yes") { paramDialogInterface, paramInt ->
                    //  permissionsclass.requestPermission(type,code);
                    startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.yusuffahrudin.masuyamobileapp")))
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> activity?.finish() }
        dialog.show()
    }*/

}
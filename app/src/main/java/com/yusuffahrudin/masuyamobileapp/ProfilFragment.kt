package com.yusuffahrudin.masuyamobileapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.databinding.FragmentProfileBinding
import com.yusuffahrudin.masuyamobileapp.profile.ChangePasswordActivity
import com.yusuffahrudin.masuyamobileapp.util.AESUtil
import com.yusuffahrudin.masuyamobileapp.util.DataHelper
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class ProfilFragment: Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var kdkota: String
    private lateinit var password: String
    private lateinit var photoFile: File
    private lateinit var fileName: String
    private var success: Int = 0
    private lateinit var message: String
    private lateinit var db: DataHelper
    private var listAks: ArrayList<UserAkses> = ArrayTampung.getListAkses()
    private val REQUEST_CAMERA = 1
    private val REQUEST_GALLERY = 2
    private val REQUEST_MULTIPLE_PERMISSIONS = 3
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding        = FragmentProfileBinding.inflate(inflater, container, false)
        val rootView    = binding.root
        db              = DataHelper(requireContext())

        sessionManager  = SessionManager(this.context)
        val user        = sessionManager.userDetails
        kdkota          = user[SessionManager.kdkota].toString()

        binding.progressbar.visibility  = View.INVISIBLE
        binding.tvNama.text             = user[SessionManager.kunci_email]
        binding.tvLevel.text            = user[SessionManager.level]
        selectData(requireActivity())

        binding.imageView.setOnClickListener { _ ->
            if (checkAndRequestPermissions()){
                uploadFoto()
            }
        }

        binding.btnChangePassword.setOnClickListener { _ ->
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            intent.putExtra("user", binding.tvNama.text.toString())
            intent.putExtra("pass", password)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener { _ ->
            listAks.clear()
            db.deleteUser()
            sessionManager.logout()
            (activity as HomeActivity).exit()
            activity?.finish()
        }
        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            val bm = BitmapFactory.decodeFile(photoFile.absolutePath)
            binding.imageView.setImageBitmap(bm)
            binding.bgImage.setImageBitmap(bm)
            uploadImage(jpgToBase64(photoFile.absolutePath))
        }
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            val selectedImage = data!!.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = activity!!.contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            cursor?.moveToFirst()
            val columnIndex = cursor!!.getColumnIndex(filePathColumn[0])
            val imgDecodableString = cursor.getString(columnIndex)
            cursor.close()
            println(imgDecodableString)
            fileName = "IMG-" + binding.tvNama.text
            val bm = BitmapFactory.decodeFile(imgDecodableString)
            binding.imageView.setImageBitmap(bm)
            binding.bgImage.setImageBitmap(bm)
            uploadImage(jpgToBase64(imgDecodableString))
        }
    }

    private fun selectData(act: Activity) {
        binding.progressbar.visibility = View.VISIBLE
        act.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        GlobalScope.launch(Dispatchers.Main) {
            val a = Server(kdkota)
            val url = a.URL() + "tools/select_user.php"

            val strReq = @SuppressLint("SetTextI18n")
            object : StringRequest(Method.POST, url, Response.Listener { response ->
                Log.d(tag, "Response : $response")
                try {
                    val jObj = JSONObject(response)
                    success = jObj.getInt("success")
                    message = jObj.getString("message")

                    //cek error node pada JSON
                    if (success == 1) {
                        val result = jObj.getJSONArray("result")
                        for (i in 0 until result.length()) {
                            try {
                                val obj = result.getJSONObject(i)
                                binding.tvMerk.text = "${obj.getString("DeviceManufacture")} - ${obj.getString("DeviceModel")}"
                                binding.tvAndroidVersi.text = obj.getString("AndroidVersion")
                                binding.tvAndroidSdk.text = obj.getString("AndroidSDK")
                                binding.tvNetwork.text = obj.getString("Operator")
                                if (obj.getString("Password").equals("null", true) || obj.getString("Password") == null) {
                                    password = ""
                                } else {
                                    password = AESUtil.decrypt(obj.getString("Password"))
                                }
                                val a = Server(kdkota)
                                Glide.with(act)
                                        .load(a.URL_IMAGE_PROFILE() + "IMG-" + binding.tvNama.text + ".jpg")
                                        .apply(RequestOptions()
                                                .placeholder(R.mipmap.ic_launcher)
                                                .error(R.drawable.img_not_found)
                                                .override(300, 400)
                                                .fitCenter())
                                        .into(binding.bgImage)
                                Glide.with(act)
                                        .load(a.URL_IMAGE_PROFILE() + "IMG-" + binding.tvNama.text + ".jpg")
                                        .apply(RequestOptions()
                                                .placeholder(R.mipmap.ic_launcher)
                                                .error(R.drawable.img_not_found)
                                                .override(300, 300)
                                                .circleCrop())
                                        .into(binding.imageView)
                                binding.tvLevel.text = obj.getString("NmLevel")
                            } catch (e: JSONException) {
                                binding.progressbar.visibility = View.GONE
                                act.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                e.printStackTrace()
                                DialogAlert(e.message, "error", requireActivity())
                            }
                        }
                        binding.progressbar.visibility = View.GONE
                        act.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    } else {
                        binding.progressbar.visibility = View.GONE
                        act.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        DialogAlert(message, "error", requireActivity())
                    }
                } catch (e: JSONException) {
                    binding.progressbar.visibility = View.GONE
                    act.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    e.printStackTrace()
                    DialogAlert(e.message.toString(), "error", requireActivity())
                }
            }, Response.ErrorListener { error ->
                binding.progressbar.visibility = View.GONE
                act.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                error.printStackTrace()
                DialogAlert(error.message.toString(), "error", requireActivity())
            }) {
                override fun getParams(): Map<String, String> {
                    //Posting parameter ke post url
                    val params = HashMap<String, String>()
                    params["user"] = binding.tvNama.text.toString()
                    return params
                }
            }
            strReq.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val camerapermission    = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val readpermission      = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        val writepermission     = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val listPermissionsNeeded = java.util.ArrayList<String>()

        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (readpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), listPermissionsNeeded.toTypedArray(), REQUEST_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    private fun uploadFoto(){
        val pictureDialog = AlertDialog.Builder(requireContext())
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf(getString(R.string.select_from_gallery),getString(R.string.select_from_camera))
        pictureDialog.setItems(pictureDialogItems
        ) { _ , which ->
            when {
                which == 0 -> openGalleryIntent()
                which == 1 -> openCameraIntent()
            }
        }
        pictureDialog.show()
    }

    @SuppressLint("RestrictedApi")
    private fun openCameraIntent() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(pictureIntent.resolveActivity(activity!!.packageManager) != null){
            //Create a file to store the image
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                DialogAlert("Failed createImageFile", "error", requireActivity())
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID+".provider", photoFile)
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(pictureIntent,REQUEST_CAMERA)
            } else {
                DialogAlert("photoFile not found", "error", requireActivity())
            }
        }
    }

    private fun openGalleryIntent() {
        //Create an Intent with action as ACTION_PICK
        val galleryIntent= Intent(Intent.ACTION_PICK)
        // Sets the type as image/*. This ensures only components of type image are selected
        galleryIntent.type = "image/*"
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes)

        // Launching the Intent
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    private fun createImageFile(): File {
        fileName = "IMG-" + binding.tvNama.text
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(fileName, ".jpg", storageDir)
        //imagePath = "file:" + image.absolutePath
        return image
    }

    private fun jpgToBase64(path: String): String{
        //var byteArray = ByteArray(8192)
        val bm = BitmapFactory.decodeFile(path)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun uploadImage(base64: String?) {
        val a = Server(kdkota)
        val url_insert = a.URL() + "tools/upload_image_profile.php"

        @SuppressLint("RestrictedApi")
        val strReq = object : StringRequest(Method.POST, url_insert, Response.Listener { response ->
            Log.v(this.tag, "Response : $response")
            try {
                val jObj = JSONObject(response)
                val success = jObj.getInt("success")
                val message = jObj.getString("message")
                //cek error node pada JSON
                if (success == 1) {
                    DialogAlert(message, "success", requireActivity())
                } else {
                    DialogAlert(message, "error", requireActivity())
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            DialogAlert(error.message.toString(), "error", requireActivity())
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()
                println(fileName)
                println(base64)

                if (base64 == null) {
                    params["image_po"] = ""
                } else {
                    params["image_po"] = base64
                }
                params["filename"] = fileName

                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }
}
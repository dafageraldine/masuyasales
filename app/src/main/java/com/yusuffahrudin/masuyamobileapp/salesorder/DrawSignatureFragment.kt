package com.yusuffahrudin.masuyamobileapp.salesorder

import android.gesture.GestureOverlayView
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yusuffahrudin.masuyamobileapp.R
import java.io.ByteArrayOutputStream

class DrawSignatureFragment: Fragment() {
    private lateinit var signaturePad: GestureOverlayView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_draw_signature, container, false)

        signaturePad = rootView.findViewById(R.id.gov_signature)
        signaturePad.isDrawingCacheEnabled = true

        return rootView
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun drawSignature(): String{
        val bm = Bitmap.createBitmap(signaturePad.width, signaturePad.height, Bitmap.Config.ARGB_8888)
        val base64 = bitmapToBase64(bm)
        signaturePad.isDrawingCacheEnabled = false

        return base64
    }
}
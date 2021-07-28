package com.yusuffahrudin.masuyamobileapp.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yusuffahrudin.masuyamobileapp.R
import kotlin.system.exitProcess

class DialogAlert(private val message: String?, private val type: String, private val activity: Activity) {
    private lateinit var dialog: BottomSheetDialog

    init {
        proses()
    }

    private fun proses() {
        when {
            type.equals("success", ignoreCase = true) -> dialogSuccess(message)
            type.equals("success-reply", ignoreCase = true) -> dialogSuccessReply(message)
            type.equals("attention", ignoreCase = true) -> dialogAttention(message)
            else -> dialogError(message)
        }
    }

    private fun dialogSuccess(pesan: String?) {
        dialog = BottomSheetDialog(activity, R.style.SheetDialogNotif)
        dialog.setContentView(R.layout.dialog_success)
        val dialogView = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(280, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnOk = dialogView?.findViewById<Button>(R.id.btn_ok)
        val tvMessage = dialogView?.findViewById<TextView>(R.id.tv_message)
        tvMessage?.text = pesan

        dialog.show()

        btnOk?.setOnClickListener {

                dialog.dismiss()

        }
    }

    @SuppressLint("InflateParams")
    private fun dialogSuccessReply(pesan: String?) {
        dialog = BottomSheetDialog(activity, R.style.SheetDialogNotif)
        dialog.setContentView(R.layout.dialog_success)
        val dialogView = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(280, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnOk = dialogView?.findViewById<Button>(R.id.btn_ok)
        val tvMessage = dialogView?.findViewById<TextView>(R.id.tv_message)
        tvMessage?.text = pesan

        dialog.show()

        btnOk?.setOnClickListener {
            dialog.dismiss()
            activity.finish()
        }
    }

    @SuppressLint("InflateParams")
    private fun dialogError(pesan: String?) {
        dialog = BottomSheetDialog(activity, R.style.SheetDialogNotif)
        dialog.setContentView(R.layout.dialog_error)
        val dialogView = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        dialog.window?.setLayout(280, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnOk = dialogView?.findViewById<Button>(R.id.btn_ok)
        val tvMessage = dialogView?.findViewById<TextView>(R.id.tv_message)
        tvMessage?.text = pesan

        dialog.show()

        btnOk?.setOnClickListener { if(pesan == "silahkan update aplikasi anda !"){
            exitProcess(-1)
        }
        else{
            dialog.dismiss() }}
    }

    @SuppressLint("InflateParams")
    private fun dialogAttention(pesan: String?) {
        dialog = BottomSheetDialog(activity, R.style.SheetDialogNotif)
        dialog.setContentView(R.layout.dialog_attention)
        val dialogView = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        dialog.window?.setLayout(280, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnOk = dialogView?.findViewById<Button>(R.id.btn_ok)
        val tvMessage = dialogView?.findViewById<TextView>(R.id.tv_message)
        tvMessage?.text = pesan

        dialog.show()

        btnOk?.setOnClickListener { dialog.dismiss() }
    }
}

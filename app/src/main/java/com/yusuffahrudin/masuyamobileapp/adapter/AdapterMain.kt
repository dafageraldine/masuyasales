package com.yusuffahrudin.masuyamobileapp.adapter

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.TypedArray
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.customer.CustomerActivity
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.historypenjualan.HistoryActivity
import com.yusuffahrudin.masuyamobileapp.informasibarang.ListBarangActivity
import com.yusuffahrudin.masuyamobileapp.laporan.LaporanActivity
import com.yusuffahrudin.masuyamobileapp.listtimbangan.ListTimbanganActivity
import com.yusuffahrudin.masuyamobileapp.salesorder.OtorisasiSOActivity
import com.yusuffahrudin.masuyamobileapp.salesorder.SalesOrderActivity
import com.yusuffahrudin.masuyamobileapp.stockopname.StockOpnameActivity
import com.yusuffahrudin.masuyamobileapp.updatepricelist.UpdatePricelistActivity
import com.yusuffahrudin.masuyamobileapp.usermanage.UserManageActivity


/**
 * Created by yusuf fahrudin on 14-07-2017.
 */

class AdapterMain(val activity: Activity, val gridViewString: Array<String>, val gridViewImageId: TypedArray, val listAks: ArrayList<UserAkses>) : RecyclerView.Adapter<AdapterMain.ViewHolder>() {
    internal lateinit var intent: Intent
    private var defValue = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_activity_main, parent, false)
        val height = parent.measuredHeight / 2
        view.minimumHeight = height
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //mengambil elemen dari arraylist pada posisi yang ditentukan
        //dan memasukkannya sebagai isi dari view recyclerview
        holder.imgMain.setImageResource(gridViewImageId.getResourceId(position, defValue))
        holder.tvMain.text = gridViewString[position]
        holder.cvMain.animation = AnimationUtils.loadAnimation(activity, R.anim.show_from_left)

        // Set onclicklistener pada view cvMain (CardView)
        holder.cvMain.setOnClickListener {
            var cek = false
            for (j in listAks.indices) {
                if (listAks[j].modul == gridViewString[position] && listAks[j].akses == 1) {
                    when (gridViewString[position]) {
                        "Informasi Barang" -> {
                            intent = Intent(activity, ListBarangActivity::class.java)
                            intent.removeExtra("kelas")
                            activity.startActivity(intent)
                        }
                        "My Customer" -> {
                            intent = Intent(activity, CustomerActivity::class.java)
                            activity.startActivity(intent)
                        }
                        "History" -> {
                            intent = Intent(activity, HistoryActivity::class.java)
                            activity.startActivity(intent)
                        }
                        "Laporan" -> {
                            intent = Intent(activity, LaporanActivity::class.java)
                            activity.startActivity(intent)
                        }
                        "Sales Order" -> {
                            val dialog = android.app.AlertDialog.Builder(activity)
                            dialog.setCancelable(true)
                            dialog.setItems(R.array.salesorder_array) { _: DialogInterface?, which: Int ->
                                when (which) {
                                    0 -> {
                                        intent = Intent(activity, SalesOrderActivity::class.java)
                                        activity.startActivity(intent)
                                    }
                                    1 -> {
                                        intent = Intent(activity, OtorisasiSOActivity::class.java)
                                        activity.startActivity(intent)
                                    }
                                }
                            }.show()
                        }
                        "Update Pricelist" -> {
                            intent = Intent(activity, UpdatePricelistActivity::class.java)
                            activity.startActivity(intent)
                        }
                        "Stock Opname" -> {
                            val listData = ArrayTampung.getListOpname()
                            listData.clear()
                            intent = Intent(activity, StockOpnameActivity::class.java)
                            activity.startActivity(intent)
                        }
                        "User Management" -> {
                            intent = Intent(activity, UserManageActivity::class.java)
                            activity.startActivity(intent)
                        }
                        "List Timbangan" -> {
                            intent = Intent(activity, ListTimbanganActivity::class.java)
                            activity.startActivity(intent)
                        }
                    }
                    cek = true
                }
            }

            if (!cek) {
                Toast.makeText(activity, activity.getString(R.string.tidak_mempunyai_hak_akses), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        //mengembalikan jumlah data yang ada pada list recyclerview
        return gridViewString.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgMain: ImageView = itemView.findViewById(R.id.imageView)
        var tvMain: TextView = itemView.findViewById(R.id.textview)
        var cvMain: FrameLayout = itemView.findViewById(R.id.cv_main)
    }
}

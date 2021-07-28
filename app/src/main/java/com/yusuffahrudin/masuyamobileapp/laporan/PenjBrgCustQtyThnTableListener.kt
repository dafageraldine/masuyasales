package com.yusuffahrudin.masuyamobileapp.laporan

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.listener.ITableViewListener
import com.yusuffahrudin.masuyamobileapp.data.ColumnHeaderModel
import com.yusuffahrudin.masuyamobileapp.data.RowHeaderModel


class PenjBrgCustQtyThnTableListener(val activity: Activity,
                                     val colValues: MutableList<ColumnHeaderModel>,
                                     val rowValues: MutableList<RowHeaderModel>,
                                     val temp: MutableList<String>) : ITableViewListener {

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, columnPosition: Int, rowPosition: Int) {
        println(temp.isNotEmpty())
        if (temp.isNotEmpty()){
            DetailPenjBrgCustQtyBln(activity, columnPosition.toString(), rowValues[rowPosition].mData, temp[0], temp[1], temp[2])
        } else {
            println("================================== ")
        }
    }

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        // Do What you want
    }

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, columnPosition: Int) {
        // Do what you want.
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, columnPosition: Int) {
        // Do what you want.
    }

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, rowPosition: Int) {
        if (temp.isNotEmpty()){
            DetailPenjBrgCustQtyThn(activity, rowValues[rowPosition].mData, temp[0], temp[1], temp[2])
        }
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, rowPosition: Int) {
        // Do what you want.
    }
}
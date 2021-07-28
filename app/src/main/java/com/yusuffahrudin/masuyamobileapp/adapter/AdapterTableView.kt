package com.yusuffahrudin.masuyamobileapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.data.CellModel
import com.yusuffahrudin.masuyamobileapp.data.ColumnHeaderModel
import com.yusuffahrudin.masuyamobileapp.data.RowHeaderModel

class AdapterTableView(val context: Context) : AbstractTableAdapter<ColumnHeaderModel, RowHeaderModel, CellModel>(context){
    //private lateinit var adapterkota: ArrayAdapter<String>

    // Cell関連
    class CellViewHolder(itemView: View) : AbstractViewHolder(itemView){
        val cell = itemView.findViewById<TextView>(R.id.cell_data)
    }
    override fun onCreateCellViewHolder(parent: ViewGroup?, viewType: Int): AbstractViewHolder {
        val layout = LayoutInflater.from(context).inflate(R.layout.tableview_cell_layout, parent, false )
        return CellViewHolder(layout)
    }
    override fun onBindCellViewHolder(
            holder: AbstractViewHolder?,
            cellItemModel: Any?,
            columnPosition: Int,
            rowPosition: Int
    ) {
        val cell = cellItemModel as CellModel
        val viewHolder = holder as CellViewHolder
        viewHolder.cell.text = cell.getData()
        viewHolder.itemView.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        viewHolder.cell.requestLayout()
    }

    // ColumnHeader関連
    class ColumnHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView){
        // チュートリアルと異なる
        val columnHeader = itemView.findViewById<TextView>(R.id.column_header_textView)
    }
    override fun onCreateColumnHeaderViewHolder(parent: ViewGroup?, viewType: Int): AbstractViewHolder {
        val layout = LayoutInflater.from(context).inflate(R.layout.tableview_column_header_layout,parent,false)
        return ColumnHeaderViewHolder(layout)
    }
    override fun onBindColumnHeaderViewHolder(
            holder: AbstractViewHolder?,
            columnHeaderItemModel: Any?,
            columnPosition: Int
    ) {
        val colHeader = columnHeaderItemModel as ColumnHeaderModel
        val colViewHolder = holder as ColumnHeaderViewHolder
        colViewHolder.columnHeader.text = colHeader.mData
        colViewHolder.itemView.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        colViewHolder.itemView.requestLayout()
    }

    // RowHeader関連
    class RowHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView){
        val rowHeader = itemView.findViewById<TextView>(R.id.row_header_textview1)
        //val rowHeader2 = itemView.findViewById<TextView>(R.id.row_header_textview2)
    }
    override fun onCreateRowHeaderViewHolder(parent: ViewGroup?, viewType: Int): AbstractViewHolder {
        val layout = LayoutInflater.from(context).inflate(R.layout.tableview_row_header_layout,parent,false)
        return RowHeaderViewHolder(layout)
    }
    override fun onBindRowHeaderViewHolder(holder: AbstractViewHolder?, rowHeaderItemModel: Any?, rowPosition: Int) {
        val rowHeader = rowHeaderItemModel as RowHeaderModel
        val rowViewHolder = holder as RowHeaderViewHolder
        rowViewHolder.rowHeader.text = rowHeader.mData
        //rowViewHolder.rowHeader2.text = rowHeader.mData2
    }

    override fun onCreateCornerView(): View {
        val layout = LayoutInflater.from(context).inflate(R.layout.tableview_corner_layout, null)
        //val tv_corner = layout.findViewById<TextView>(R.id.tv_corner)

        return layout
    }

    override fun getColumnHeaderItemViewType(position: Int): Int {
        return 0
    }
    override fun getRowHeaderItemViewType(position: Int): Int {
        return 0
    }
    override fun getCellItemViewType(position: Int): Int {
        return 0
    }
}
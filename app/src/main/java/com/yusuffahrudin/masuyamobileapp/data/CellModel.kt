package com.yusuffahrudin.masuyamobileapp.data

import android.os.Parcel
import android.os.Parcelable
import com.evrencoskun.tableview.sort.ISortableModel

class CellModel(pId: String, private val mData: String): ISortableModel, Parcelable{
    private val mId: String = pId

    constructor(parcel: Parcel) : this(
            TODO("pId"),
            parcel.readString().toString())

    fun getData(): String{
        return mData
    }
    override fun getContent(): Any {
        return mData
    }
    override fun getId(): String {
        return mId
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mData)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CellModel> {
        override fun createFromParcel(parcel: Parcel): CellModel {
            return CellModel(parcel)
        }

        override fun newArray(size: Int): Array<CellModel?> {
            return arrayOfNulls(size)
        }
    }
}
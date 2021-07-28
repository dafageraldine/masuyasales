package com.yusuffahrudin.masuyamobileapp.data

import android.os.Parcel
import android.os.Parcelable

data class ColumnHeaderModel(val mData: String): Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString().toString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mData)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ColumnHeaderModel> {
        override fun createFromParcel(parcel: Parcel): ColumnHeaderModel {
            return ColumnHeaderModel(parcel)
        }

        override fun newArray(size: Int): Array<ColumnHeaderModel?> {
            return arrayOfNulls(size)
        }
    }
}
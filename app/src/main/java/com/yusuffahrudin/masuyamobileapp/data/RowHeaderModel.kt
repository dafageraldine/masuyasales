package com.yusuffahrudin.masuyamobileapp.data

import android.os.Parcel
import android.os.Parcelable

data class RowHeaderModel(val mData: String): Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString().toString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mData)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RowHeaderModel> {
        override fun createFromParcel(parcel: Parcel): RowHeaderModel {
            return RowHeaderModel(parcel)
        }

        override fun newArray(size: Int): Array<RowHeaderModel?> {
            return arrayOfNulls(size)
        }
    }
}
package com.sdy.jitangapplication.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

/**
 *    author : ZFM
 *    date   : 2021/1/1810:04
 *    desc   :
 *    version: 1.0
 */


data class LocationBean(
    val placeName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val id: String? = ""
) :Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?:"",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(placeName)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationBean> {
        override fun createFromParcel(parcel: Parcel): LocationBean {
            return LocationBean(parcel)
        }

        override fun newArray(size: Int): Array<LocationBean?> {
            return arrayOfNulls(size)
        }
    }
}
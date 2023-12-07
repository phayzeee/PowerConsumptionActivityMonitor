package com.example.powerconsumptionactivitymonitor.model

import android.os.Parcel

import android.os.Parcelable


class Electronic : Parcelable {
    var idElectronic: Int
        private set
    var electronicName: String

    constructor(idElectronic: Int, electronicName: String) {
        this.idElectronic = idElectronic
        this.electronicName = electronicName
    }

    constructor() {
        idElectronic = 0
        electronicName = ""
    }

    protected constructor(`in`: Parcel) {
        idElectronic = `in`.readInt()
        electronicName = `in`.readString()!!
    }

    override fun toString(): String {
        return electronicName
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(idElectronic)
        dest.writeString(electronicName)
    }


    companion object CREATOR : Parcelable.Creator<Electronic> {
        override fun createFromParcel(parcel: Parcel): Electronic {
            return Electronic(parcel)
        }

        override fun newArray(size: Int): Array<Electronic?> {
            return arrayOfNulls(size)
        }
    }
}
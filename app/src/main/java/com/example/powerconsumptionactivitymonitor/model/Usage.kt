package com.example.powerconsumptionactivitymonitor.model

import android.os.Parcel
import android.os.Parcelable


open class Usage : Parcelable {
    var idUsage = 0
    var electronic: Electronic?
    var numberOfElectronic: Int
    var totalUsageHoursPerDay = 0.0
    var totalWattagePerDay = 0

    constructor(electronic: Electronic?, numberOfElectronic: Int) {
        this.electronic = electronic
        this.numberOfElectronic = numberOfElectronic
    }

    constructor(
        idUsage: Int,
        electronic: Electronic?,
        numberOfElectronic: Int,
        totalWattagePerDay: Int,
        totalUsageHoursPerDay: Double
    ) {
        this.idUsage = idUsage
        this.electronic = electronic
        this.numberOfElectronic = numberOfElectronic
        this.totalUsageHoursPerDay = totalUsageHoursPerDay
        this.totalWattagePerDay = totalWattagePerDay
    }

    protected constructor(`in`: Parcel) {
        idUsage = `in`.readInt()
        electronic = `in`.readValue(Electronic::class.java.classLoader) as Electronic?
        numberOfElectronic = `in`.readInt()
        totalUsageHoursPerDay = `in`.readDouble()
        totalWattagePerDay = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(idUsage)
        dest.writeValue(electronic)
        dest.writeInt(numberOfElectronic)
        dest.writeDouble(totalUsageHoursPerDay)
        dest.writeInt(totalWattagePerDay)
    }


    companion object CREATOR : Parcelable.Creator<Usage> {
        override fun createFromParcel(parcel: Parcel): Usage {
            return Usage(parcel)
        }

        override fun newArray(size: Int): Array<Usage?> {
            return arrayOfNulls(size)
        }
    }
}
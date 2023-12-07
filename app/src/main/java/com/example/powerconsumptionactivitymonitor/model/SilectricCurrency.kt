package com.example.powerconsumptionactivitymonitor.model

import java.util.Currency


class SilectricCurrency : Comparable<SilectricCurrency?> {
    private var countryName: String? = null
    private var currency: Currency?
    fun getCurrency(): Currency? {
        return currency
    }

    fun setCurrency(currency: Currency?) {
        this.currency = currency
    }

    constructor(countryName: String?, currency: Currency?) {
        this.countryName = countryName
        this.currency = currency
    }

    constructor(currency: Currency?) {
        this.currency = currency
    }

    override fun toString(): String {
        return countryName + " (" + currency?.symbol + ")"
    }



    override fun compareTo(other: SilectricCurrency?): Int {
        val that = other as SilectricCurrency
        return countryName!!.compareTo(that.countryName.toString())
    }



    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as SilectricCurrency
        return currency?.equals(that.currency) ?: (that.currency == null)
    }

    override fun hashCode(): Int {
        return if (currency != null) currency.hashCode() else 0
    }
}
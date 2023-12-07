package com.example.powerconsumptionactivitymonitor.model

class ElectronicTimeUsageTemplate {
    private var idElectronicTimeUsageTemplate = 0
    var hours: Int
    var minutes: Int
    var idElectronic = 0
    var wattage: Int
    var idUsageMode: Int
    var usageMode: UsageMode
    var isNewElectronicTimeUsageTemplate: Boolean
        private set
    var isNewElectronic = false
        private set

    constructor(
        idElectronicTimeUsageTemplate: Int,
        idElectronic: Int,
        idUsageMode: Int,
        wattage: Int,
        hours: Int,
        minutes: Int,
        usageMode: UsageMode
    ) {
        this.idElectronicTimeUsageTemplate = idElectronicTimeUsageTemplate
        this.hours = hours
        this.minutes = minutes
        this.idElectronic = idElectronic
        this.wattage = wattage
        this.idUsageMode = idUsageMode
        this.usageMode = usageMode
        isNewElectronicTimeUsageTemplate = false
    }

    constructor(
        isNewElectronic: Boolean,
        idUsageMode: Int,
        wattage: Int,
        hours: Int,
        minutes: Int,
        usageMode: UsageMode
    ) {
        this.hours = hours
        this.minutes = minutes
        this.wattage = wattage
        this.idUsageMode = idUsageMode
        this.isNewElectronic = isNewElectronic
        isNewElectronicTimeUsageTemplate = true
        this.usageMode = usageMode
    }

    fun setIsNewTimeUsage(isNewTimeUsage: Boolean) {
        isNewElectronicTimeUsageTemplate = isNewTimeUsage
    }

    fun setIsNewUsage(isNewUsage: Boolean) {
        isNewElectronic = isNewUsage
    }
}
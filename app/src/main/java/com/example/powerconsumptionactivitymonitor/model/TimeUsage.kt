package com.example.powerconsumptionactivitymonitor.model


class TimeUsage {
    var idTimeUsage = 0
        private set
    var hours: Int
    var minutes: Int
    var idUsage = 0
    var wattage: Int
    var idUsageMode: Int
    var usageMode: UsageMode
    var isNewTimeUsage: Boolean
    var isNewUsage = false

    constructor(
        idTimeUsage: Int,
        idUsage: Int,
        idUsageMode: Int,
        wattage: Int,
        hours: Int,
        minutes: Int,
        usageMode: UsageMode
    ) {
        this.idTimeUsage = idTimeUsage
        this.hours = hours
        this.minutes = minutes
        this.idUsage = idUsage
        this.wattage = wattage
        this.idUsageMode = idUsageMode
        this.usageMode = usageMode
        isNewTimeUsage = false
    }

    constructor(
        isNewUsage: Boolean,
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
        this.isNewUsage = isNewUsage
        isNewTimeUsage = true
        this.usageMode = usageMode
    }
}
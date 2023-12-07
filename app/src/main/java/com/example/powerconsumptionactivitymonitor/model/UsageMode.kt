package com.example.powerconsumptionactivitymonitor.model


class UsageMode(var idUsageMode: Int, var usageModeName: String) {

    override fun toString(): String {
        return usageModeName
    }
}
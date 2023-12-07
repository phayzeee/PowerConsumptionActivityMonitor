package com.example.powerconsumptionactivitymonitor

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.powerconsumptionactivitymonitor.model.SilectricCurrency
import java.util.Collections
import java.util.Currency
import java.util.Locale



class OptionsActivity : AppCompatActivity() {
    private var sharedPreferences: SharedPreferences? = null
    private var usageFeePerKwh = 0.0
    var basicChargeFee = 0.0
    private var otherFee = 0.0
    var currencyCode = ""
    var selectedSilectricCurrency: SilectricCurrency? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
        val toolbar: Toolbar = findViewById<View>(R.id.electricStaticFeeOptionsToolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences("silectricPreferences", MODE_PRIVATE)
      //  usageFeePerKwh = sharedPreferences?.getFloat("usage_fee_per_kwh", 0f)?.toDouble() ?: 0.0
//        basicChargeFee = sharedPreferences.getFloat("basic_fee", 0f).toDouble()
//        otherFee = sharedPreferences.getFloat("others_fee", 0f).toDouble()
//        currencyCode = sharedPreferences.getString("currency_code", "USD")

        sharedPreferences?.let { prefs ->
            usageFeePerKwh = prefs.getFloat("usage_fee_per_kwh", 0f).toDouble()
            basicChargeFee = prefs.getFloat("basic_fee", 0f).toDouble()
            otherFee = prefs.getFloat("others_fee", 0f).toDouble()
            currencyCode = prefs.getString("currency_code", "USD").toString()

        } ?: run {
            usageFeePerKwh = 0.0
            basicChargeFee = 0.0
            otherFee = 0.0
            currencyCode = ""
        }
        val currency: Currency = Currency.getInstance(currencyCode)
        selectedSilectricCurrency = SilectricCurrency(currency)
        initFormOptions()
        initSaveElectricFeesFloatingButton()
    }

    private fun initSaveElectricFeesFloatingButton() {
        val saveElectricFeeFloatingButton =
            findViewById<View>(R.id.saveElectricFeeButton) as ImageButton
        saveElectricFeeFloatingButton.setOnClickListener {
            val editorSharedPref = sharedPreferences!!.edit()
            val usageFeePerKwhEditText =
                findViewById<View>(R.id.usage_fee_per_kwh_edit_text) as EditText
            val basicChargeFeeEditText =
                findViewById<View>(R.id.basic_charge_fee_edit_text) as EditText
            val othersFeeEditText =
                findViewById<View>(R.id.other_fee_edit_text) as EditText
            val currencyOptionsSpinner =
                findViewById<View>(R.id.currencyOptionsSpinner) as SearchableSpinner
            editorSharedPref.putBoolean("has_initiated", true)
            editorSharedPref.putFloat(
                "usage_fee_per_kwh",
                java.lang.Float.valueOf(usageFeePerKwhEditText.text.toString())
            )
            editorSharedPref.putFloat(
                "basic_fee",
                java.lang.Float.valueOf(basicChargeFeeEditText.text.toString())
            )
            editorSharedPref.putFloat(
                "others_fee",
                java.lang.Float.valueOf(othersFeeEditText.text.toString())
            )
            val silectricCurrency: SilectricCurrency =
                currencyOptionsSpinner.selectedItem as SilectricCurrency
            editorSharedPref.putString(
                "currency_code",
                silectricCurrency.getCurrency()?.currencyCode
            )
            editorSharedPref.apply()
            setResult(RESULT_OK, Intent())
            finish()
        }
    }

    private fun initFormOptions() {
        val usageFeePerKwhEditText =
            findViewById<View>(R.id.usage_fee_per_kwh_edit_text) as EditText
        val basicChargeFeeEditText = findViewById<View>(R.id.basic_charge_fee_edit_text) as EditText
        val othersFeeEditText = findViewById<View>(R.id.other_fee_edit_text) as EditText
        val currencyOptionsSpinner =
            findViewById<View>(R.id.currencyOptionsSpinner) as SearchableSpinner
        val silectricCurrencies: ArrayList<SilectricCurrency> =
            OptionsActivity.Companion.allCurrencies
        val dataAdapter: ArrayAdapter<SilectricCurrency> =
            ArrayAdapter<SilectricCurrency>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, silectricCurrencies)
        currencyOptionsSpinner.adapter = dataAdapter
        for (i in silectricCurrencies.indices) {
            val currencyCode1: String = silectricCurrencies[i].getCurrency()!!.currencyCode
            val currencyCode2: String = selectedSilectricCurrency?.getCurrency()!!.currencyCode
            if (currencyCode1 === currencyCode2) currencyOptionsSpinner.setSelection(i)
        }
        usageFeePerKwhEditText.setText(usageFeePerKwh.toString())
        basicChargeFeeEditText.setText(basicChargeFee.toString())
        othersFeeEditText.setText(otherFee.toString())
    }

    companion object {
        private val allCurrencies: ArrayList<SilectricCurrency>
            private get() {
                val silectricCurrencyLinkedHashSet: LinkedHashSet<SilectricCurrency> =
                    LinkedHashSet<SilectricCurrency>()
                val locales = Locale.getAvailableLocales()
                for (loc in locales) {
                    try {
                        val currency: Currency = Currency.getInstance(loc)
                        silectricCurrencyLinkedHashSet.add(
                            SilectricCurrency(
                                loc.displayCountry,
                                currency
                            )
                        )
                    } catch (exc: Exception) {
                        // Locale not found
                    }
                }
                val silectricCurrencies: ArrayList<SilectricCurrency> =
                    ArrayList<SilectricCurrency>()
                silectricCurrencies.addAll(silectricCurrencyLinkedHashSet)
                Collections.sort(silectricCurrencies)
                return silectricCurrencies
            }
    }
}
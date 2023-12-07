package com.example.powerconsumptionactivitymonitor

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.powerconsumptionactivitymonitor.model.DbConnection
import com.example.powerconsumptionactivitymonitor.model.Usage
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency


class MainActivity : AppCompatActivity() {
    private val USAGE_ACTIVITY_REQ = 1
    private val ELECTRIC_FEE_ACTIVITY_REQ = 3
    private var silectricPreferences: SharedPreferences? = null
    private var dbConnection: DbConnection? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById<View>(R.id.mainToolbar) as Toolbar
        setSupportActionBar(toolbar)
        silectricPreferences = getSharedPreferences("silectricPreferences", MODE_PRIVATE)
        dbConnection = DbConnection(this, resources.openRawResource(R.raw.initial_data))
        checkPreferences()
        initInputDays()
        initAddUsageButton()
        initListUsage()
        calculateTotal()
    }

    private fun calculateTotal() {
        val daysEditText = findViewById<View>(R.id.daysEditText) as EditText
        val days = daysEditText.text.toString().toInt()
        val totalWattDaily: Double = dbConnection!!.totalWattDaily
        val totalKwh = totalWattDaily * days / 1000 //divide 1000 for convert Watt to KWatt
        val kwhFormat: DecimalFormat = DecimalFormat.getNumberInstance() as DecimalFormat
        //        kwhFormat.applyPattern("###,###.##");
//        kwhFormat.setDecimalSeparatorAlwaysShown(false);
        val totalElectricUsageTextView =
            findViewById<View>(R.id.totalElectricUsageTextView) as TextView
        val totalKwHString: String = kwhFormat.format(totalKwh) + " KwH "
        totalElectricUsageTextView.text = totalKwHString
        val feeUsagePerKwh = silectricPreferences!!.getFloat("usage_fee_per_kwh", 1f).toDouble()
        val feeBase = silectricPreferences!!.getFloat("basic_fee", 0f).toDouble()
        val feeOthers = silectricPreferences!!.getFloat("others_fee", 0f).toDouble()
        var totalMonthlyUsage = totalKwh * feeUsagePerKwh
        totalMonthlyUsage += feeBase + feeOthers
        val currencyCode = silectricPreferences!!.getString("currency_code", "USD")
        val currency: Currency = Currency.getInstance(currencyCode)
        val monthlyFeeFormat: DecimalFormat =
            DecimalFormat.getCurrencyInstance(resources.configuration.locale) as DecimalFormat
        val monthlyFeeFormatSymbols = DecimalFormatSymbols()
        monthlyFeeFormatSymbols.currencySymbol = currency.symbol
        //        monthlyFeeFormatSymbols.setMonetaryDecimalSeparator(',');
//        monthlyFeeFormatSymbols.setGroupingSeparator('.');
        monthlyFeeFormat.setDecimalFormatSymbols(monthlyFeeFormatSymbols)
        //        monthlyFeeFormat.setDecimalSeparatorAlwaysShown(false);
        val totalElectricFeeTextView = findViewById<View>(R.id.totalElectricFeeTextView) as TextView
        val totalMonthlyFeeString: String = monthlyFeeFormat.format(totalMonthlyUsage)
        totalElectricFeeTextView.text = totalMonthlyFeeString
    }

    private fun initInputDays() {
        val daysEditText = findViewById<View>(R.id.daysEditText) as EditText
        val daysInPreferences = silectricPreferences!!.getInt("number_of_days", 30)
        daysEditText.setText(daysInPreferences.toString())
        daysEditText.setOnClickListener { v ->
            val days: Int
            days = daysEditText.text.toString().toInt()
            val alert: AlertDialog.Builder = AlertDialog.Builder(v.context)
            alert.setTitle("Number of Days: ")
            val np = NumberPicker(v.context)
            np.minValue = 1
            np.maxValue = 10000
            np.wrapSelectorWheel = false
            np.value = days
            np.setOnValueChangedListener { picker, oldVal, newVal ->
                daysEditText.setText(newVal.toString())
                val editPref = silectricPreferences!!.edit()
                editPref.putInt("number_of_days", newVal)
                editPref.apply()
                calculateTotal()
            }
            alert.setPositiveButton("Ok",
                DialogInterface.OnClickListener { dialog, which ->
                    np.clearFocus()
                    val days = np.value
                    daysEditText.setText(days.toString())
                    val editPref = silectricPreferences!!.edit()
                    editPref.putInt("number_of_days", days)
                    editPref.apply()
                    calculateTotal()
                })
            val parent = FrameLayout(v.context)
            parent.addView(
                np, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
            alert.setView(parent)
            alert.show().window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    private fun initAddUsageButton() {
        val addUsageButton = findViewById<View>(R.id.addUsageButton) as ImageButton
        addUsageButton.setOnClickListener { view ->
            val usageActivity = Intent(view.context, UsageActivity::class.java)
            val usage: Usage? = null
            usageActivity.putExtra("usage", usage)
            startActivityForResult(usageActivity, USAGE_ACTIVITY_REQ)
        }
    }

    private fun initListUsage() {
        val usageList: ArrayList<Usage> = dbConnection!!.usageList
        val list = findViewById<View>(R.id.usageListView) as ListView
        val adapter: ListAdapter = UsageListAdapter(this, usageList)
        list.adapter = adapter
        list.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val usageActivity = Intent(view.context, UsageActivity::class.java)
                val usage: Usage = parent.getItemAtPosition(position) as Usage
                usageActivity.putExtra("usage", usage)
                startActivityForResult(usageActivity, USAGE_ACTIVITY_REQ)
            }
    }

    private fun checkPreferences() {
        val hasInitiated = silectricPreferences!!.getBoolean("has_initiated", false)
        if (!hasInitiated) {
            val editorSharedPref = silectricPreferences!!.edit()
            editorSharedPref.putBoolean("has_initiated", true)
            editorSharedPref.putFloat("usage_fee_per_kwh", 0.20f)
            editorSharedPref.putFloat("basic_fee", 0f)
            editorSharedPref.putFloat("others_fee", 0f)
            editorSharedPref.putInt("number_of_days", 30)
            editorSharedPref.putString(
                "currency_code",
                Currency.getInstance(resources.configuration.locale).getCurrencyCode()
            )
            editorSharedPref.apply()
            val intent = Intent(this, OptionsActivity::class.java)
            startActivityForResult(intent, ELECTRIC_FEE_ACTIVITY_REQ)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        initListUsage()
        calculateTotal()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_electric_price) {
            val intent = Intent(this, OptionsActivity::class.java)
            startActivityForResult(intent, ELECTRIC_FEE_ACTIVITY_REQ)
            return true
        } else if (id == R.id.action_electronic_type) {
            val intent = Intent(this, ElectronicListActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}


internal class UsageListAdapter(private val activity: Activity, usageList: ArrayList<Usage>) :
    BaseAdapter() {
    private val usageList: ArrayList<Usage>

    init {
        this.usageList = usageList
        inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return usageList.size
    }

    override fun getItem(position: Int): Any {
        return usageList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class ViewHolder(view: View) {
        lateinit var nameElectronicOnListRow : TextView
        lateinit var totalWattagePerDayOnListRow : TextView
        lateinit var totalUsageHoursPerDayOnListRow : TextView
        lateinit var numberOfElectronicOnListRow : TextView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val vh: ViewHolder
        if (convertView == null) {
            view = inflater!!.inflate(R.layout.list_usage, parent, false)
            vh = ViewHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }
        vh.nameElectronicOnListRow = view.findViewById(R.id.electronicNameOnListRow)
        vh.totalWattagePerDayOnListRow = view.findViewById(R.id.totalWattagePerDayOnListRow)
        vh.totalUsageHoursPerDayOnListRow = view.findViewById(R.id.totalUsageHoursPerDayOnListRow)
        vh.numberOfElectronicOnListRow = view.findViewById(R.id.numberOfElectronicOnListRow)

        val df = DecimalFormat("0.##")
        val formattedHours: String = df.format(usageList[position].totalUsageHoursPerDay) + " hours"
        val formattedWatt: String = df.format(usageList[position].totalWattagePerDay) + " watt"
        val formattedNumberOfElectronic: String =
            df.format(usageList[position].numberOfElectronic) + " electronics"
        vh.nameElectronicOnListRow.text = usageList[position].electronic?.electronicName
        vh.totalWattagePerDayOnListRow.text = formattedWatt
        vh.totalUsageHoursPerDayOnListRow.text = formattedHours
        vh.numberOfElectronicOnListRow.text = formattedNumberOfElectronic
        return view
    }

//
//    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
//        var view = convertView
//        if (convertView == null) view = inflater!!.inflate(R.layout.list_usage, null)
//        val nameElectronicOnListRow =
//            view.findViewById<View>(R.id.electronicNameOnListRow) as TextView
//        val totalWattagePerDayOnListRow =
//            view.findViewById<View>(R.id.totalWattagePerDayOnListRow) as TextView
//        val totalUsageHoursPerDayOnListRow =
//            view.findViewById<View>(R.id.totalUsageHoursPerDayOnListRow) as TextView
//        val numberOfElectronicOnListRow =
//            view.findViewById<View>(R.id.numberOfElectronicOnListRow) as TextView
//        val usage: Usage = usageList[position]
//        val df = DecimalFormat("0.##")
//        try {
//            val formattedHours: String = df.format(usage.totalUsageHoursPerDay) + " hours"
//            val formattedWatt: String = df.format(usage.totalWattagePerDay) + " watt"
//            val formattedNumberOfElectronic: String =
//                df.format(usage.numberOfElectronic) + " electronics"
//            nameElectronicOnListRow.text = usage.electronic?.electronicName
//            totalWattagePerDayOnListRow.text = formattedWatt
//            totalUsageHoursPerDayOnListRow.text = formattedHours
//            numberOfElectronicOnListRow.text = formattedNumberOfElectronic
//        } catch (e: Exception) {
//            Log.e("MAinActivity", e.message!!)
//        }
//        return view
//    }

    companion object {
        private var inflater: LayoutInflater? = null
    }
}
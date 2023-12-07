package com.example.powerconsumptionactivitymonitor

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.powerconsumptionactivitymonitor.model.DbConnection
import com.example.powerconsumptionactivitymonitor.model.Electronic
import com.example.powerconsumptionactivitymonitor.model.ElectronicTimeUsageTemplate
import com.example.powerconsumptionactivitymonitor.model.TimeUsage
import com.example.powerconsumptionactivitymonitor.model.Usage
import com.example.powerconsumptionactivitymonitor.model.UsageMode


class UsageActivity : AppCompatActivity() {
    private var usage: Usage? = null
    private var dbConnection: DbConnection? = null
    private var isNewUsage = false
    private var timeUsageArrayList: ArrayList<TimeUsage>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usage)
        val toolbar: Toolbar = findViewById<View>(R.id.usageToolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        dbConnection = DbConnection(this)
        val deleteUsageButton = findViewById<View>(R.id.deleteUsageButton) as ImageButton
        usage = intent.getParcelableExtra("usage")
        if (usage == null) {
            usage = Usage(dbConnection?.defaultElectronic, 1)
            isNewUsage = true
            deleteUsageButton.visibility = View.INVISIBLE
        } else deleteUsageButton.visibility = View.VISIBLE
        initUsageForm()
        initSaveButton()
        initDeleteFloatingButton()
    }

    private fun initUsageForm() {
        val electronicNameSpinner =
            findViewById<View>(R.id.electronicNameSpinner) as SearchableSpinner
        val addTimeUsageButton = findViewById<View>(R.id.addTimeUsageButton) as ImageButton
        val numberOfElectronicTextView =
            findViewById<View>(R.id.numberOfElectronicTextView) as EditText
        val electronicList: ArrayList<Electronic> = dbConnection!!.electronicList
        timeUsageArrayList = dbConnection!!.getTimeUsagesByIdUsage(usage!!.idUsage)
        initElectronicSpinner(electronicNameSpinner, electronicList)
        initNumberOfElectronicNumberPicker(numberOfElectronicTextView)
        initListTimeUsage()
        val numberOfElectronic: Int = usage!!.numberOfElectronic
        numberOfElectronicTextView.setText(numberOfElectronic.toString())
        addTimeUsageButton.setOnClickListener { v -> popUpTimeUsageDialog(v, true, null) }
    }

    private fun initNumberOfElectronicNumberPicker(numberOfElectronicTextView: EditText) {
        numberOfElectronicTextView.setOnClickListener { v ->
            val alert: AlertDialog.Builder = AlertDialog.Builder(v.context)
            val np = NumberPicker(v.context)
            np.minValue = 1
            np.maxValue = 10000
            np.wrapSelectorWheel = false
            np.value = usage!!.numberOfElectronic
            alert.setPositiveButton(R.string.save,
                DialogInterface.OnClickListener { dialog, whichButton ->
                    np.clearFocus()
                    numberOfElectronicTextView.setText(np.value.toString())
                    usage?.numberOfElectronic = np.value
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
            alert.show()
        }
    }

    private fun initElectronicSpinner(
        electronicNameSpinner: SearchableSpinner,
        electronicList: List<Electronic>
    ) {
        val dataAdapter: ArrayAdapter<Electronic> =
            ArrayAdapter<Electronic>(this, android.R.layout.simple_spinner_dropdown_item, electronicList)
        electronicNameSpinner.setAdapter(dataAdapter)
        electronicNameSpinner.setTitle("Select Electronic Type")
        electronicNameSpinner.setPositiveButton("Ok")
        electronicNameSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val electronic: Electronic = parent.getItemAtPosition(position) as Electronic
                if (isNewUsage) {
                    val timeUsageTemplateArrayList: ArrayList<ElectronicTimeUsageTemplate> =
                        dbConnection!!.getElectronicTimeUsageTemplateByIdElectronic(electronic.idElectronic)
                    timeUsageArrayList!!.clear()
                    for (e in timeUsageTemplateArrayList) {
                        timeUsageArrayList!!.add(
                            TimeUsage(
                                isNewUsage,
                                e.idUsageMode,
                                e.wattage,
                                e.hours,
                                e.minutes,
                                e.usageMode
                            )
                        )
                    }
                    initListTimeUsage()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val electronicInUsage: Electronic? = usage?.electronic
        for (i in electronicList.indices) {
            if (electronicInUsage?.idElectronic === electronicList[i].idElectronic) {
                electronicNameSpinner.setSelection(i)
            }
        }
    }

    private fun initSaveButton() {
        val saveUsageButton = findViewById<View>(R.id.saveUsageButton) as ImageButton
        saveUsageButton.setOnClickListener {
            val electronicNameSpinner =
                findViewById<View>(R.id.electronicNameSpinner) as Spinner
            val electronic: Electronic = electronicNameSpinner.selectedItem as Electronic
            usage?.electronic = electronic
            var totalUsageHoursPerDay = 0.0
            var totalWattagePerDay = 0
            for (timeUsage in timeUsageArrayList!!) {
                val minutes: Double = timeUsage.minutes.toDouble()
                val minute_in_hours = minutes / 60
                val hours: Double = timeUsage.hours + minute_in_hours
                totalUsageHoursPerDay += hours
                totalWattagePerDay =
                    (totalWattagePerDay + hours * timeUsage.wattage).toInt()
            }
            usage?.totalUsageHoursPerDay = totalUsageHoursPerDay
            usage?.totalWattagePerDay = totalWattagePerDay
            if (isNewUsage) {
                usage?.let { it1 -> dbConnection?.addUsage(it1, timeUsageArrayList!!) }
            } else {
                dbConnection?.editUsage(usage!!, timeUsageArrayList!!)
            }
            finish()
        }
    }

    private fun initDeleteFloatingButton() {
        val deleteUsageButton = findViewById<View>(R.id.deleteUsageButton) as ImageButton
        deleteUsageButton.setOnClickListener {
            dbConnection?.deleteUsage(usage!!)
            finish()
        }


//        }
    }

    private fun initListTimeUsage() {
        val listUsageListView = findViewById<View>(R.id.timeUsageListView) as ListView
        val timeUsageListAdapter: ListAdapter = TimeUsageListAdapter(this, timeUsageArrayList)
        listUsageListView.adapter = timeUsageListAdapter
        listUsageListView.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val timeUsage: TimeUsage = parent.getItemAtPosition(position) as TimeUsage
                popUpTimeUsageDialog(view, false, timeUsage)
            }
    }

    private fun popUpTimeUsageDialog(v: View, isNewTimeUsage: Boolean, timeUsage: TimeUsage?) {
        val usageModeSpinner = Spinner(v.context)
        val wattageNumberPicker = NumberPicker(v.context)
        val hoursInPopupTimeUsage = NumberPicker(v.context)
        val minutesInPopupTimeUsage = NumberPicker(v.context)
        val timeUsageLayoutDialog = initTimeUsageLayoutDialog(
            v,
            usageModeSpinner, wattageNumberPicker, hoursInPopupTimeUsage,
            minutesInPopupTimeUsage
        )
        val okButtonString: String
        if (isNewTimeUsage) {
            okButtonString = "Add"
        } else {
            okButtonString = "Save"
            wattageNumberPicker.value = timeUsage!!.wattage
            hoursInPopupTimeUsage.value = timeUsage.hours
            minutesInPopupTimeUsage.value = timeUsage.minutes
            val usageModeArrayList: ArrayList<UsageMode> = dbConnection!!.usageModeList
            var usageModeSpinnerPos = 0
            for (usageModeInList in usageModeArrayList) {
                if (timeUsage.idUsageMode === usageModeInList.idUsageMode) break
                usageModeSpinnerPos++
            }
            usageModeSpinner.setSelection(usageModeSpinnerPos)
        }
        val alert: AlertDialog.Builder = AlertDialog.Builder(v.context)
        alert.setPositiveButton(okButtonString,
            DialogInterface.OnClickListener { dialog, which ->
                wattageNumberPicker.clearFocus()
                hoursInPopupTimeUsage.clearFocus()
                minutesInPopupTimeUsage.clearFocus()
                val usageMode: UsageMode = usageModeSpinner.selectedItem as UsageMode
                val idUsageMode: Int = usageMode.idUsageMode
                val wattage = wattageNumberPicker.value
                val hours = hoursInPopupTimeUsage.value
                val minutes = minutesInPopupTimeUsage.value
                if (isNewTimeUsage) {
                    timeUsageArrayList!!.add(
                        TimeUsage(
                            isNewUsage,
                            idUsageMode,
                            wattage,
                            hours,
                            minutes,
                            usageMode
                        )
                    )
                    initListTimeUsage()
                } else {
                    timeUsage!!.idUsageMode = idUsageMode
                    timeUsage.wattage = wattage
                    timeUsage.hours = hours
                    timeUsage.minutes = minutes
                    initListTimeUsage()
                }
            })
        alert.setNegativeButton("Cancel", null)
        alert.setNeutralButton("Delete",
            DialogInterface.OnClickListener { dialog, which ->
                if (timeUsage != null) {
                    timeUsageArrayList!!.remove(timeUsage)
                    initListTimeUsage()
                }
            })
        val frameAlert = FrameLayout(v.context)
        frameAlert.addView(
            timeUsageLayoutDialog, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        alert.setView(frameAlert)
        alert.show()
    }

    private fun initTimeUsageLayoutDialog(
        v: View,
        usageModeSpinner: Spinner,
        wattageNumberPicker: NumberPicker,
        hoursInPopupTimeUsage: NumberPicker,
        minutesInPopupTimeUsage: NumberPicker
    ): LinearLayout {
        val usageModeList: List<UsageMode> = dbConnection!!.usageModeList
        val dataAdapter: ArrayAdapter<UsageMode> =
            ArrayAdapter<UsageMode>(this, android.R.layout.simple_spinner_dropdown_item, usageModeList)
        usageModeSpinner.adapter = dataAdapter
        usageModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val usageMode: UsageMode = parent.getItemAtPosition(position) as UsageMode
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        wattageNumberPicker.minValue = 0
        wattageNumberPicker.maxValue = 10000
        wattageNumberPicker.wrapSelectorWheel = false
        hoursInPopupTimeUsage.minValue = 0
        hoursInPopupTimeUsage.maxValue = 24
        hoursInPopupTimeUsage.setFormatter { i -> String.format("%02d", i) }
        minutesInPopupTimeUsage.minValue = 0
        minutesInPopupTimeUsage.maxValue = 60
        minutesInPopupTimeUsage.setFormatter { i -> String.format("%02d", i) }
        val usageModeTextView = TextView(v.context)
        val wattageTextView = TextView(v.context)
        val hoursTextView = TextView(v.context)
        val minutesTextView = TextView(v.context)
        usageModeTextView.text = "Mode : "
        wattageTextView.text = "Watt : "
        hoursTextView.text = "Hours : "
        minutesTextView.text = "Minutes : "

//        if (Build.VERSION.SDK_INT < 23) {
//            usageModeTextView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
//            wattageTextView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
//            hoursTextView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
//            minutesTextView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
//        }else{
//            usageModeTextView.setTextAppearance(android.R.style.TextAppearance_Medium);
//            wattageTextView.setTextAppearance(android.R.style.TextAppearance_Medium);
//            hoursTextView.setTextAppearance(android.R.style.TextAppearance_Medium);
//            minutesTextView.setTextAppearance(android.R.style.TextAppearance_Medium);
//        }
        val wrappingLayParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER.toFloat()
        )
        val mainLayoutTimeUsage = LinearLayout(v.context)
        mainLayoutTimeUsage.layoutParams = wrappingLayParam
        mainLayoutTimeUsage.orientation = LinearLayout.VERTICAL
        mainLayoutTimeUsage.setPadding(10, 10, 10, 10)
        val usageModeLayout = LinearLayout(v.context)
        usageModeLayout.layoutParams = wrappingLayParam
        usageModeLayout.orientation = LinearLayout.HORIZONTAL
        usageModeLayout.setPadding(30, 10, 30, 10)
        val wattageLayout = LinearLayout(v.context)
        wattageLayout.layoutParams = wrappingLayParam
        wattageLayout.orientation = LinearLayout.VERTICAL
        wattageLayout.setPadding(30, 10, 30, 10)
        val hoursLayout = LinearLayout(v.context)
        hoursLayout.layoutParams = wrappingLayParam
        hoursLayout.orientation = LinearLayout.VERTICAL
        hoursLayout.setPadding(70, 10, 10, 10)
        val minutesLayout = LinearLayout(v.context)
        minutesLayout.layoutParams = wrappingLayParam
        minutesLayout.orientation = LinearLayout.VERTICAL
        minutesLayout.setPadding(0, 10, 10, 10)
        val numberPickersLayout = LinearLayout(v.context)
        numberPickersLayout.layoutParams = wrappingLayParam
        numberPickersLayout.orientation = LinearLayout.HORIZONTAL
        numberPickersLayout.setPadding(10, 10, 10, 10)
        usageModeLayout.addView(usageModeTextView, wrappingLayParam)
        usageModeLayout.addView(
            usageModeSpinner, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER.toFloat()
            )
        )
        mainLayoutTimeUsage.addView(usageModeLayout, wrappingLayParam)
        wattageLayout.addView(wattageTextView, wrappingLayParam)
        wattageLayout.addView(wattageNumberPicker, wrappingLayParam)
        numberPickersLayout.addView(wattageLayout, wrappingLayParam)
        hoursLayout.addView(hoursTextView, wrappingLayParam)
        hoursLayout.addView(hoursInPopupTimeUsage, wrappingLayParam)
        numberPickersLayout.addView(hoursLayout, wrappingLayParam)
        minutesLayout.addView(minutesTextView, wrappingLayParam)
        minutesLayout.addView(minutesInPopupTimeUsage, wrappingLayParam)
        numberPickersLayout.addView(minutesLayout, wrappingLayParam)
        mainLayoutTimeUsage.addView(numberPickersLayout, wrappingLayParam)
        return mainLayoutTimeUsage
    }
}

internal class TimeUsageListAdapter(
    private val activity: Activity,
    timeUsageList: ArrayList<TimeUsage>?
) :
    BaseAdapter() {
    private val timeUsageList: ArrayList<TimeUsage>?

    init {
        this.timeUsageList = timeUsageList
        inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return timeUsageList!!.size
    }

    override fun getItem(position: Int): Any {
        return timeUsageList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    inner class ViewHolder(view: View) {
        lateinit var wattageInListTimeUsage : TextView
        lateinit var usageModeNameInListTimeUsage : TextView
        lateinit var timeInListTimeUsage : TextView
    }


    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val vh: TimeUsageListAdapter.ViewHolder
        if (convertView == null) {
            view = inflater!!.inflate(R.layout.list_time_usage, parent, false)
            vh = ViewHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }
        vh.wattageInListTimeUsage = view.findViewById(R.id.wattageInListTimeUsage)!!
        vh.usageModeNameInListTimeUsage = view.findViewById(R.id.usageModeNameInListTimeUsage)!!
        vh.timeInListTimeUsage = view.findViewById(R.id.timeInListTimeUsage)!!

        vh.usageModeNameInListTimeUsage.text =
            java.lang.String.format("%s", timeUsageList?.get(position)?.usageMode?.usageModeName)
        vh.wattageInListTimeUsage.text =
            String.format("%s Watt", java.lang.String.valueOf(timeUsageList?.get(position)?.wattage))
        vh.timeInListTimeUsage.text = java.lang.String.valueOf(timeUsageList?.get(position)?.hours) + " : " + java.lang.String.valueOf(
            timeUsageList?.get(position)?.minutes
        )

        return view
    }

    companion object {
        private var inflater: LayoutInflater? = null
    }
}
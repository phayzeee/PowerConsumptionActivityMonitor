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
import com.example.powerconsumptionactivitymonitor.model.UsageMode


class ElectronicActivity : AppCompatActivity() {
    private var electronic: Electronic? = null
    private var dbConnection: DbConnection? = null
    private var isNewElectronic = false
    private var timeUsageTemplateArrayList: ArrayList<ElectronicTimeUsageTemplate>? = null
    var deleteElectronicButton: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_electronic)
        val toolbar: Toolbar = findViewById<View>(R.id.mainToolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        dbConnection = DbConnection(this)
        electronic = intent.getParcelableExtra("electronic")
        deleteElectronicButton = findViewById<View>(R.id.deleteElectronicButton) as ImageButton
        if (electronic == null) {
            electronic = Electronic()
            isNewElectronic = true
            deleteElectronicButton!!.visibility = View.INVISIBLE
        } else deleteElectronicButton!!.visibility = View.VISIBLE
        initForm()
        initListTimeUsageTemplate()
        initDeleteElectronicButton()
        initSaveElectronicButton()
    }

    private fun initForm() {
        val electronicNameInElectronicContentForm =
            findViewById<View>(R.id.electronicNameInElectronicContent) as TextView
        val addTimeUsageButton =
            findViewById<View>(R.id.addTimeUsageInElectronicContentButton) as ImageButton
        timeUsageTemplateArrayList =
            dbConnection?.getElectronicTimeUsageTemplateByIdElectronic(electronic!!.idElectronic)
        if (electronic != null) {
            electronicNameInElectronicContentForm.text = java.lang.String.valueOf(electronic!!.electronicName)
        }
        addTimeUsageButton.setOnClickListener { v -> popUpTimeUsageDialog(v, true, null) }
    }

    private fun initDeleteElectronicButton() {
        if (electronic == null) {
            deleteElectronicButton!!.visibility = View.INVISIBLE
        } else {
            deleteElectronicButton!!.visibility = View.VISIBLE
            deleteElectronicButton!!.setOnClickListener {
                val idElectronic: Int = electronic!!.idElectronic
                dbConnection?.deleteElectronic(idElectronic)
                finish()
            }
        }
    }

    private fun initSaveElectronicButton() {
        val saveFloatingButton = findViewById<View>(R.id.saveElectronicButton) as ImageButton
        saveFloatingButton.setOnClickListener {
            val electronicNameInElectronicForm =
                findViewById<View>(R.id.electronicNameInElectronicContent) as TextView
            val electronicName = electronicNameInElectronicForm.text.toString()
            electronic?.electronicName= electronicName
            if (isNewElectronic) {
                electronic?.let { it1 -> timeUsageTemplateArrayList?.let { it2 ->
                    dbConnection?.addElectronic(it1,
                        it2
                    )
                } }
            } else {
                electronic?.let { it1 -> timeUsageTemplateArrayList?.let { it2 ->
                    dbConnection?.editElectronic(it1,
                        it2
                    )
                } }
            }
            finish()
        }
    }

    private fun initListTimeUsageTemplate() {
        val listUsageListView =
            findViewById<View>(R.id.timeUsageListViewInElectronicContent) as ListView
        val timeUsageTemplateListAdapter: ListAdapter =
            ElectronicTimeUsageTemplateListAdapter(this, timeUsageTemplateArrayList)
        listUsageListView.adapter = timeUsageTemplateListAdapter
        listUsageListView.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val timeUsageTemplate: ElectronicTimeUsageTemplate =
                    parent.getItemAtPosition(position) as ElectronicTimeUsageTemplate
                popUpTimeUsageDialog(view, false, timeUsageTemplate)
            }
    }

    private fun popUpTimeUsageDialog(
        v: View,
        isNewElectronicTimeUsageTemplate: Boolean,
        timeUsageTemplate: ElectronicTimeUsageTemplate?
    ) {
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
        if (isNewElectronicTimeUsageTemplate) {
            okButtonString = "Add"
        } else {
            okButtonString = "Save"
            wattageNumberPicker.value = timeUsageTemplate!!.wattage
            hoursInPopupTimeUsage.value = timeUsageTemplate.hours
            minutesInPopupTimeUsage.value = timeUsageTemplate.minutes
            val usageModeArrayList: ArrayList<UsageMode> = dbConnection!!.usageModeList
            var usageModeSpinnerPos = 0
            for (usageModeInList in usageModeArrayList) {
                if (timeUsageTemplate.idUsageMode === usageModeInList.idUsageMode) break
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
                if (isNewElectronicTimeUsageTemplate) {
                    timeUsageTemplateArrayList!!.add(
                        ElectronicTimeUsageTemplate(
                            true,
                            idUsageMode,
                            wattage,
                            hours,
                            minutes,
                            usageMode
                        )
                    )
                    initListTimeUsageTemplate()
                } else {
                    timeUsageTemplate?.idUsageMode = idUsageMode
                    timeUsageTemplate?.wattage = wattage
                    timeUsageTemplate?.hours = hours
                    timeUsageTemplate?.minutes= minutes
                    initListTimeUsageTemplate()
                }
            })
        alert.setNegativeButton("Cancel", null)
        alert.setNeutralButton("Delete",
            DialogInterface.OnClickListener { dialog, which ->
                if (timeUsageTemplate != null) {
                    timeUsageTemplateArrayList!!.remove(timeUsageTemplate)
                    initListTimeUsageTemplate()
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

    @SuppressLint("SetTextI18n")
    private fun initTimeUsageLayoutDialog(
        v: View,
        usageModeSpinner: Spinner,
        wattageNumberPicker: NumberPicker,
        hoursInPopupTimeUsage: NumberPicker,
        minutesInPopupTimeUsage: NumberPicker
    ): LinearLayout {
        val usageModeList: List<UsageMode> = dbConnection!!.usageModeList
        val dataAdapter: ArrayAdapter<UsageMode> =
            ArrayAdapter<UsageMode>(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, usageModeList)
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
        usageModeTextView.text = getString(R.string.mode) + getString(R.string.double_score)
        wattageTextView.text = getString(R.string.watt) + getString(R.string.double_score)
        hoursTextView.text = getString(R.string.hours) + getString(R.string.double_score)
        minutesTextView.text = getString(R.string.minutes) + getString(R.string.double_score)
        usageModeTextView.setTextAppearance(this, androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
        wattageTextView.setTextAppearance(this, androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
        hoursTextView.setTextAppearance(this, androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
        minutesTextView.setTextAppearance(this, androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
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

internal class ElectronicTimeUsageTemplateListAdapter(
    private val activity: Activity,
    electronicTimeUsageTemplateList: ArrayList<ElectronicTimeUsageTemplate>?
) :
    BaseAdapter() {
    private val timeUsageList: ArrayList<ElectronicTimeUsageTemplate>?

    init {
        timeUsageList = electronicTimeUsageTemplateList
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val vh: ViewHolder
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

//
//    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
//        var view = convertView
//        if (convertView == null) view = inflater!!.inflate(R.layout.list_time_usage, null)
//        val wattageInListTimeUsage =
//            view.findViewById<View>(R.id.wattageInListTimeUsage) as TextView
//        val usageModeNameInListTimeUsage =
//            view.findViewById<View>(R.id.usageModeNameInListTimeUsage) as TextView
//        val timeInListTimeUsage = view.findViewById<View>(R.id.timeInListTimeUsage) as TextView
//        val electronicTimeUsageTemplate: ElectronicTimeUsageTemplate = timeUsageList!![position]
//        try {
//            usageModeNameInListTimeUsage.text =
//                java.lang.String.format(
//                    "%s",
//                    electronicTimeUsageTemplate.usageMode.usageModeName
//                )
//            wattageInListTimeUsage.text = String.format(
//                "%s Watt",
//                java.lang.String.valueOf(electronicTimeUsageTemplate.wattage)
//            )
//            timeInListTimeUsage.text = java.lang.String.valueOf(electronicTimeUsageTemplate.hours) + " : " + java.lang.String.valueOf(
//                electronicTimeUsageTemplate.minutes
//            )
//        } catch (e: Exception) {
//            Log.e("ListElectronic", e.message!!)
//        }
//        return view
//    }

    companion object {
        private var inflater: LayoutInflater? = null
    }
}

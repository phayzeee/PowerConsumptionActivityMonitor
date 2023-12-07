package com.example.powerconsumptionactivitymonitor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.powerconsumptionactivitymonitor.model.DbConnection
import com.example.powerconsumptionactivitymonitor.model.Electronic

class ElectronicListActivity : AppCompatActivity() {
    private var dbConnection: DbConnection? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_electronic)
        val toolbar: Toolbar = findViewById<View>(R.id.listElectronicToolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        dbConnection = DbConnection(this)
        initListElectronic()
        initAddFloatingButton()
    }

    private fun initListElectronic() {
        val electronicList: ArrayList<Electronic> = dbConnection!!.electronicList
        val list = findViewById<View>(R.id.electronicListView) as ListView
        val adapter: ListAdapter = ElectronicListAdapter(this, electronicList)
        list.adapter = adapter
        list.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val electronicActivity = Intent(view.context, ElectronicActivity::class.java)
                val electronic: Electronic = parent.getItemAtPosition(position) as Electronic
                electronicActivity.putExtra("electronic", electronic)
                startActivityForResult(
                    electronicActivity,
                    ElectronicListActivity.Companion.ELECTRONIC_ACTIVITY_REQ
                )
            }
    }

    private fun initAddFloatingButton() {
        val addElectronicTypeFloating = findViewById<View>(R.id.addElectronicButton) as ImageButton
        addElectronicTypeFloating.setOnClickListener { view ->
            val electronicActivity = Intent(view.context, ElectronicActivity::class.java)
            val electronic: Electronic? = null
            electronicActivity.putExtra("electronic", electronic)
            startActivityForResult(
                electronicActivity,
                ElectronicListActivity.Companion.ELECTRONIC_ACTIVITY_REQ
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ElectronicListActivity.Companion.ELECTRONIC_ACTIVITY_REQ) {
            initListElectronic()
        }
    }

    companion object {
        private const val ELECTRONIC_ACTIVITY_REQ = 1
    }
}



internal class ElectronicListAdapter(
    private val activity: Context,
    electronicList: ArrayList<Electronic>
) :
    BaseAdapter() {
    private val electronicList: ArrayList<Electronic>

    init {
        this.electronicList = electronicList
        inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return electronicList.size
    }

    override fun getItem(position: Int): Any {
        return electronicList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class ViewHolder(view: View) {
        lateinit var textView : TextView
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val vh: ViewHolder
        if (convertView == null) {
            view = inflater!!.inflate(R.layout.list_electronic, parent, false)
            vh = ViewHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ElectronicListAdapter.ViewHolder
        }
        vh?.textView = view?.findViewById(R.id.electronicNameInListElectronic)!!
        vh?.textView?.text = electronicList[position].electronicName

        return view
    }

    companion object {
        private var inflater: LayoutInflater? = null
    }
}
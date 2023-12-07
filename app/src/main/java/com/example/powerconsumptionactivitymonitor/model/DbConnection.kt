package com.example.powerconsumptionactivitymonitor.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.sql.Time


class DbConnection : SQLiteOpenHelper {
    private var thisDB: SQLiteDatabase? = null
    private var initialDataStream: InputStream? = null

    constructor(context: Context?, initialDataStream: InputStream?) : super(
        context,
        DbConnection.Companion.DATABASE_NAME,
        null,
        DbConnection.Companion.DATABASE_VERSION
    ) {
        this.initialDataStream = initialDataStream
    }

    constructor(context: Context?) : super(
        context,
        DbConnection.Companion.DATABASE_NAME,
        null,
        DbConnection.Companion.DATABASE_VERSION
    )

    val defaultElectronic: Electronic
        get() {
            var Electronic = Electronic()
            val selectQuery = "SELECT * FROM Electronic limit 1"
            openReadableDatabase()
            val cursor = thisDB!!.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val idElectronic = cursor.getInt(0)
                    val electronicName = cursor.getString(1)
                    Electronic = Electronic(idElectronic, electronicName)
                } while (cursor.moveToNext())
            }
            cursor.close()
            closeDatabase()
            return Electronic
        }
    val electronicList: ArrayList<Electronic>
        get() {
            val ElectronicList = ArrayList<Electronic>()
            val selectQuery = "SELECT * FROM Electronic order by electronicName asc"
            openReadableDatabase()
            val cursor = thisDB!!.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val idElectronic = cursor.getInt(0)
                    val electronicName = cursor.getString(1)
                    ElectronicList.add(Electronic(idElectronic, electronicName))
                } while (cursor.moveToNext())
            }
            cursor.close()
            closeDatabase()
            return ElectronicList
        }

    fun getTimeUsagesByIdUsage(idUsage: Int): ArrayList<TimeUsage> {
        var idUsage = idUsage
        val timeUsageLists = ArrayList<TimeUsage>()
        val selectQuery =
            "SELECT 'TimeUsage'.'idTimeUsage' , 'TimeUsage'.'idUsage' , 'TimeUsage'.'idUsageMode' , 'TimeUsage'.'wattage', 'TimeUsage'.'hours' , 'TimeUsage'.'minutes', 'UsageMode'.'usageModeName'  FROM TimeUsage inner join 'UsageMode' on 'UsageMode'.idUsageMode = 'TimeUsage'.idUsageMode  where idUsage=$idUsage"
        openReadableDatabase()
        val cursor = thisDB!!.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val idTimeUsage = cursor.getInt(0)
                idUsage = cursor.getInt(1)
                val idUsageMode = cursor.getInt(2)
                val wattage = cursor.getInt(3)
                val hours = cursor.getInt(4)
                val minutes = cursor.getInt(5)
                val usageModeName = cursor.getString(6)
                timeUsageLists.add(
                    TimeUsage(
                        idTimeUsage,
                        idUsage,
                        idUsageMode,
                        wattage,
                        hours,
                        minutes,
                        UsageMode(idUsageMode, usageModeName)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        closeDatabase()
        return timeUsageLists
    }

    fun getElectronicTimeUsageTemplateByIdElectronic(idElectronic: Int): ArrayList<ElectronicTimeUsageTemplate> {
        var idElectronic = idElectronic
        val timeUsageTemplateArrayList = ArrayList<ElectronicTimeUsageTemplate>()
        val selectQuery =
            "SELECT 'ElectronicTimeUsageTemplate'.'idElectronicTimeUsageTemplate' , 'ElectronicTimeUsageTemplate'.'idElectronic' , 'ElectronicTimeUsageTemplate'.'idUsageMode' , 'ElectronicTimeUsageTemplate'.'wattage', 'ElectronicTimeUsageTemplate'.'hours' , 'ElectronicTimeUsageTemplate'.'minutes', 'UsageMode'.'usageModeName'  FROM ElectronicTimeUsageTemplate inner join 'UsageMode' on 'UsageMode'.idUsageMode = 'ElectronicTimeUsageTemplate'.idUsageMode  where idElectronic=$idElectronic"
        //        String selectQuery = "SELECT * from ElectronicTimeUsageTemplate";
        openReadableDatabase()
        val cursor = thisDB!!.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val idTimeUsage = cursor.getInt(0)
                idElectronic = cursor.getInt(1)
                val idUsageMode = cursor.getInt(2)
                val wattage = cursor.getInt(3)
                val hours = cursor.getInt(4)
                val minutes = cursor.getInt(5)
                val usageModeName = cursor.getString(6)
                timeUsageTemplateArrayList.add(
                    ElectronicTimeUsageTemplate(
                        idTimeUsage,
                        idElectronic,
                        idUsageMode,
                        wattage,
                        hours,
                        minutes,
                        UsageMode(idUsageMode, usageModeName)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        closeDatabase()
        return timeUsageTemplateArrayList
    }

    val usageList: ArrayList<Usage>
        get() {
            val usageList = ArrayList<Usage>()
            val selectQuery = "select Usage.idUsage, Usage.idElectronic, " +
                    "Usage.numberOfElectronic, electronicName, " +
                    "Usage.totalWattagePerDay, Usage.totalUsageHoursPerDay " +
                    "from Usage inner join Electronic on " +
                    "Usage.idElectronic=Electronic.idElectronic order by idUsage desc;"
            openReadableDatabase()
            val cursor = thisDB!!.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val idUsage = cursor.getInt(0)
                    val idElectronic = cursor.getInt(1)
                    val numberOfItems = cursor.getInt(2)
                    val electronicName = cursor.getString(3)
                    val totalWattagePerDay = cursor.getInt(4)
                    val totalUsageHoursPerDay = cursor.getDouble(5)
                    val Electronic = Electronic(idElectronic, electronicName)
                    val usage = Usage(
                        idUsage,
                        Electronic,
                        numberOfItems,
                        totalWattagePerDay,
                        totalUsageHoursPerDay
                    )
                    usageList.add(usage)
                } while (cursor.moveToNext())
            }
            cursor.close()
            closeDatabase()
            return usageList
        }
    val totalWattDaily: Double
        get() {
            val selectQuery = "select sum(totalWattagePerDay * numberOfElectronic) as " +
                    "total_watt_daily from Usage inner join TimeUsage on " +
                    "Usage.idUsage=TimeUsage.idUsage;"
            var totalWattDaily = 0.0
            openReadableDatabase()
            val cursor = thisDB!!.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                totalWattDaily = cursor.getDouble(0)
            }
            cursor.close()
            closeDatabase()
            return totalWattDaily
        }

    fun addUsage(usage: Usage, timeUsageList: ArrayList<TimeUsage>) {
        openWritableDatabase()
        val values = ContentValues()
        values.put("numberOfElectronic", usage.numberOfElectronic)
        values.put("idElectronic", usage.electronic!!.idElectronic)
        values.put("totalWattagePerDay", usage.totalWattagePerDay)
        values.put("totalUsageHoursPerDay", usage.totalUsageHoursPerDay)
        val lastInsertedId = thisDB!!.insert("Usage", null, values)
        closeDatabase()
        updateTimeUsages(lastInsertedId, timeUsageList)
    }

    private fun updateTimeUsages(idUsage: Long, timeUsageList: ArrayList<TimeUsage>) {
        openWritableDatabase()
        thisDB!!.delete("TimeUsage", "idUsage=?", arrayOf(idUsage.toString()))
        for (timeUsage in timeUsageList) {
            val values = ContentValues()
            values.put("idUsage", idUsage)
            values.put("idUsageMode", timeUsage.idUsageMode)
            values.put("wattage", timeUsage.wattage)
            values.put("hours", timeUsage.hours)
            values.put("minutes", timeUsage.minutes)
            thisDB!!.insert("TimeUsage", null, values)
        }
        closeDatabase()
    }

    fun editUsage(usage: Usage, timeUsageList: ArrayList<TimeUsage>) {
        openWritableDatabase()
        val values = ContentValues()
        values.put("idElectronic", usage.electronic!!.idElectronic)
        values.put("numberOfElectronic", usage.numberOfElectronic)
        values.put("totalWattagePerDay", usage.totalWattagePerDay)
        values.put("totalUsageHoursPerDay", usage.totalUsageHoursPerDay)
        val whereArgs = arrayOf(usage.idUsage.toString())
        val result = thisDB!!.update("Usage", values, "idUsage=?", whereArgs).toLong()
        closeDatabase()
        updateTimeUsages(usage.idUsage.toLong(), timeUsageList)
    }

    fun addElectronic(
        electronic: Electronic,
        electronicTimeUsageTemplatesArrayList: ArrayList<ElectronicTimeUsageTemplate>
    ) {
        openWritableDatabase()
        val values = ContentValues()
        values.put("electronicName", electronic.electronicName)
        val result = thisDB!!.insert("Electronic", null, values)
        closeDatabase()
        updateElectronicTimeUsageTemplates(
            electronic.idElectronic.toLong(),
            electronicTimeUsageTemplatesArrayList
        )
    }

    private fun updateElectronicTimeUsageTemplates(
        idElectronic: Long,
        electronicTimeUsageTemplatesArrayList: ArrayList<ElectronicTimeUsageTemplate>
    ) {
        openWritableDatabase()
        thisDB!!.delete(
            "ElectronicTimeUsageTemplate",
            "idElectronic=?",
            arrayOf(idElectronic.toString())
        )
        for (e in electronicTimeUsageTemplatesArrayList) {
            val values = ContentValues()
            values.put("idElectronic", idElectronic)
            values.put("idUsageMode", e.idUsageMode)
            values.put("wattage", e.wattage)
            values.put("hours", e.hours)
            values.put("minutes", e.minutes)
            thisDB!!.insert("ElectronicTimeUsageTemplate", null, values)
        }
        closeDatabase()
    }

    fun editElectronic(
        electronic: Electronic,
        electronicTimeUsageTemplatesArrayList: ArrayList<ElectronicTimeUsageTemplate>
    ) {
        openWritableDatabase()
        val values = ContentValues()
        values.put("electronicName", electronic.electronicName)
        val whereArgs = arrayOf(electronic.idElectronic.toString())
        val result = thisDB!!.update("Electronic", values, "idElectronic=?", whereArgs).toLong()
        closeDatabase()
        updateElectronicTimeUsageTemplates(
            electronic.idElectronic.toLong(),
            electronicTimeUsageTemplatesArrayList
        )
    }

    fun deleteUsage(usage: Usage) {
        openWritableDatabase()
        thisDB!!.delete("Usage", "idUsage=?", arrayOf(usage.idUsage.toString()))
        thisDB!!.delete("TimeUsage", "idUsage=?", arrayOf(usage.idUsage.toString()))
        closeDatabase()
    }

    fun deleteElectronic(idElectronic: Int) {
        openWritableDatabase()
        thisDB!!.delete("Electronic", "idElectronic=?", arrayOf(idElectronic.toString()))
        thisDB!!.delete(
            "ElectronicTimeUsageTemplate",
            "idElectronic=?",
            arrayOf(idElectronic.toString())
        )
        closeDatabase()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Electronic ('idElectronic' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'electronicName' TEXT NOT NULL);")
        db.execSQL("CREATE TABLE ElectronicTimeUsageTemplate ('idElectronicTimeUsageTemplate' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'idElectronic' INTEGER NOT NULL,  'idUsageMode' INTEGER NOT NULL, 'wattage' INTEGER  NOT NULL  DEFAULT (1), 'hours' INTEGER NOT NULL DEFAULT (0), 'minutes' INTEGER NOT NULL DEFAULT (0));")
        db.execSQL("CREATE TABLE Usage ('idUsage' INTEGER PRIMARY KEY AUTOINCREMENT  NOT NULL,  'idElectronic' INTEGER  NOT NULL  DEFAULT (1), 'numberOfElectronic' INTEGER  NOT NULL  DEFAULT (0), 'totalUsageHoursPerDay' REAL NOT NULL DEFAULT (1), 'totalWattagePerDay' INTEGER NOT NULL DEFAULT (1));")
        db.execSQL("CREATE TABLE TimeUsage ('idTimeUsage' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'idUsage' INTEGER  NOT NULL  DEFAULT (1), 'idUsageMode' INTEGER NOT NULL, 'wattage' INTEGER  NOT NULL  DEFAULT (1), 'hours' INTEGER NOT NULL DEFAULT (0), 'minutes' INTEGER NOT NULL DEFAULT (0));")
        db.execSQL("CREATE TABLE UsageMode ('idUsageMode' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'usageModeName' TEXT NOT NULL);")
        try {
            val writer: Writer = StringWriter()
            val buffer = CharArray(1024)
            val reader: Reader = BufferedReader(InputStreamReader(initialDataStream, "UTF-8"))
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0, n)
            }
            val initialDataJsonStr = writer.toString()
            val initialDataJson = JSONObject(initialDataJsonStr)
            val electronicsJsonArr = initialDataJson.getJSONArray("Electronic")
            for (i in 0 until electronicsJsonArr.length()) {
                val electronicName = electronicsJsonArr.getJSONObject(i).getString("electronicName")
                val values = ContentValues()
                values.put("electronicName", electronicName)
                val idElectronic = db.insert("Electronic", null, values)
                val timeUsageTemplateJsonArray =
                    electronicsJsonArr.getJSONObject(i).getJSONArray("TimeUsageTemplate")
                for (j in 0 until timeUsageTemplateJsonArray.length()) {
                    val idUsageMode =
                        timeUsageTemplateJsonArray.getJSONObject(j).getInt("idUsageMode")
                    val wattage = timeUsageTemplateJsonArray.getJSONObject(j).getInt("wattage")
                    val hours = timeUsageTemplateJsonArray.getJSONObject(j).getInt("hours")
                    val minutes = timeUsageTemplateJsonArray.getJSONObject(j).getInt("minutes")
                    db.execSQL("INSERT into ElectronicTimeUsageTemplate('idElectronic', 'idUsageMode', 'wattage', 'hours', 'minutes') values($idElectronic,$idUsageMode,$wattage,$hours,$minutes);")
                }
            }
            val usageModeJsonArr = initialDataJson.getJSONArray("UsageMode")
            for (i in 0 until usageModeJsonArr.length()) {
                val idUsageMode = usageModeJsonArr.getJSONObject(i).getInt("idUsageMode")
                val usageModeName = usageModeJsonArr.getJSONObject(i).getString("usageModeName")
                db.execSQL("INSERT into UsageMode('idUsageMode', 'usageModeName') values($idUsageMode,'$usageModeName');")
            }
        } catch (e: Exception) {
            db.execSQL("delete from Electronic")
            db.execSQL("delete from ElectronicTimeUsageTemplate")
            db.execSQL("delete from Usage")
            db.execSQL("delete from TimeUsage")
            db.execSQL("delete from UsageMode")
            Log.d("DbConnection", e.toString())
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        thisDB = db
        db.execSQL("DROP TABLE IF EXISTS ElectronicTimeUsageTemplate ")
        db.execSQL("DROP TABLE IF EXISTS TimeUsage ")
        db.execSQL("DROP TABLE IF EXISTS Electronic ")
        db.execSQL("DROP TABLE IF EXISTS Usage ")
        db.execSQL("DROP TABLE IF EXISTS UsageMode ")
        onCreate(db)
    }

    private fun closeDatabase() {
        if (thisDB != null && thisDB!!.isOpen) {
            thisDB!!.close()
        }
    }

    private fun openReadableDatabase() {
        thisDB = readableDatabase
    }

    private fun openWritableDatabase() {
        thisDB = writableDatabase
    }

    val usageModeList: ArrayList<UsageMode>
        get() {
            val usageModeList = ArrayList<UsageMode>()
            val selectQuery = "select idUsageMode, usageModeName from UsageMode"
            openReadableDatabase()
            val cursor = thisDB!!.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val idUsageMode = cursor.getInt(0)
                    val usageModeName = cursor.getString(1)
                    usageModeList.add(UsageMode(idUsageMode, usageModeName))
                } while (cursor.moveToNext())
            }
            cursor.close()
            closeDatabase()
            return usageModeList
        }

    companion object {
        private const val DATABASE_NAME = "ElectricUsageCostSimulation.db"
        private const val DATABASE_VERSION = 25
    }
}
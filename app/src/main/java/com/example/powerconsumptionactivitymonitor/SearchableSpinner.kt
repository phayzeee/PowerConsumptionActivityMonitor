package com.example.powerconsumptionactivitymonitor

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.TypedArray
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import com.example.powerconsumptionactivitymonitor.SearchableListDialog.SearchableItem


//class SearchableSpinner : androidx.appcompat.widget.AppCompatSpinner, OnTouchListener, SearchableItem<Any?> {
//    private var _context: Context
//    private var _items: MutableList<*>? = null
//    private var _searchableListDialog: SearchableListDialog? = null
//    private var _isDirty = false
//    private var _arrayAdapter: ArrayAdapter<Nothing>?=null
//    private var _strHintText: String? = null
//    private var _isFromInit = false
//
//    constructor(context: Context) : super(context) {
//        _context = context
//        init()
//    }
//
//    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
//        _context = context
//        val a = context.obtainStyledAttributes(attrs, R.styleable.SearchableSpinner)
//        val N = a.indexCount
//        for (i in 0 until N) {
//            val attr = a.getIndex(i)
//            if (attr == R.styleable.SearchableSpinner_hintText) {
//                _strHintText = a.getString(attr)
//            }
//        }
//        a.recycle()
//        init()
//    }
//
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
//        context,
//        attrs,
//        defStyleAttr
//    ) {
//        _context = context
//        init()
//    }
//
//    private fun init() {
//        _items = ArrayList<Any?>()
//        _searchableListDialog = SearchableListDialog.newInstance(_items)
//        _searchableListDialog!!.setOnSearchableItemClickListener(this)
//        setOnTouchListener(this)
//        _arrayAdapter = adapter as ArrayAdapter<Nothing>
//        if (!TextUtils.isEmpty(_strHintText)) {
//            val arrayAdapter: ArrayAdapter<*> =
//                ArrayAdapter<Any?>(_context, android.R.layout.simple_list_item_1, arrayOf(_strHintText))
//            _isFromInit = true
//            adapter = arrayAdapter
//        }
//    }
//
//    override fun onTouch(v: View, event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_UP) {
//
//            // Refresh content #6
//            // Change Start
//            // Description: The items were only set initially, not reloading the data in the
//            // spinner every time it is loaded with items in the adapter.
//            _items!!.clear()
//            for (i in 0 until _arrayAdapter!!.count) {
//                _arrayAdapter?.getItem(i)?.let { _items!!.add(it) }
//            }
//            // Change end.
//            _searchableListDialog?.fragmentManager?.let { _searchableListDialog?.show(it, "TAG") }
//        }
//        return true
//    }
//
//    override fun setAdapter(adapter: SpinnerAdapter) {
//        if (!_isFromInit) {
//            _arrayAdapter = adapter as ArrayAdapter<Nothing>
//            if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
//                val arrayAdapter: ArrayAdapter<*> =
//                    ArrayAdapter<Any?>(_context, android.R.layout.simple_list_item_1, arrayOf(_strHintText))
//                super.setAdapter(arrayAdapter)
//            } else {
//                super.setAdapter(adapter)
//            }
//        } else {
//            _isFromInit = false
//            super.setAdapter(adapter)
//        }
//    }
//
//
//    fun setTitle(strTitle: String?) {
//        _searchableListDialog!!.setTitle(strTitle)
//    }
//
//    fun setPositiveButton(strPositiveButtonText: String?) {
//        _searchableListDialog!!.setPositiveButton(strPositiveButtonText)
//    }
//
//    private fun scanForActivity(cont: Context?): Activity? {
//        if (cont == null) return null else if (cont is Activity) return cont else if (cont is ContextWrapper) return scanForActivity(
//            cont.baseContext
//        )
//        return null
//    }
//
//    override fun getSelectedItemPosition(): Int {
//        return if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
//            SearchableSpinner.Companion.NO_ITEM_SELECTED
//        } else {
//            super.getSelectedItemPosition()
//        }
//    }
//
//    override fun getSelectedItem(): Any? {
//        return if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
//            null
//        } else {
//            super.getSelectedItem()
//        }
//    }
//
//    companion object {
//        private const val NO_ITEM_SELECTED = -1
//    }
//
//    override fun onSearchableItemClicked(item: Any?) {
//        setSelection(_items!!.indexOf(item))
//        if (!_isDirty) {
//            _isDirty = true
//            adapter = _arrayAdapter!!
//            setSelection(_items!!.indexOf(item))
//        }
//    }
//}

class SearchableSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSpinner(context, attrs, defStyleAttr), View.OnTouchListener, SearchableItem<Any?> {

    companion object {
        private const val NO_ITEM_SELECTED = -1
    }

    private var _context: Context = context
    private var _items: MutableList<Any> = ArrayList()
    private var _searchableListDialog: SearchableListDialog =
        SearchableListDialog.newInstance(_items)

    private var _isDirty: Boolean = false
    private var _arrayAdapter: ArrayAdapter<*>? = null
    private var _strHintText: String? = null
    private var _isFromInit: Boolean = false

    init {
        init()
        _context = context
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SearchableSpinner)
        val N: Int = a.indexCount
        for (i in 0 until N) {
            val attr: Int = a.getIndex(i)
            if (attr == R.styleable.SearchableSpinner_hintText) {
                _strHintText = a.getString(attr)
            }
        }
        a.recycle()
        init()
    }

//    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
//
//
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
//        context,
//        attrs,
//        defStyleAttr,
//        defStyleRes
//    ) {
//
//    }

    private fun init() {
        _searchableListDialog.setOnSearchableItemClickListener(this)
        setOnTouchListener(this)

        _arrayAdapter = adapter as ArrayAdapter<*>?
        if (!TextUtils.isEmpty(_strHintText)) {
            val arrayAdapter =
                ArrayAdapter(_context, android.R.layout.simple_list_item_1, arrayOf(_strHintText))
            _isFromInit = true
            adapter = arrayAdapter
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            _arrayAdapter?.let {
                _items.clear()
                for (i in 0 until it.count) {
                    it.getItem(i)?.let { it1 -> _items.add(it1) }
                }
                val fragmentManager = (v?.context as? AppCompatActivity)?.supportFragmentManager

                _searchableListDialog.show(fragmentManager!!, "TAG")
            }
        }
        return true
    }

    override fun setAdapter(adapter: SpinnerAdapter?) {
        if (!_isFromInit) {
            _arrayAdapter = adapter as ArrayAdapter<*>?
            if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
                val arrayAdapter =
                    ArrayAdapter(_context, android.R.layout.simple_list_item_1, arrayOf(_strHintText))
                super.setAdapter(arrayAdapter)
            } else {
                super.setAdapter(adapter)
            }
        } else {
            _isFromInit = false
            super.setAdapter(adapter)
        }
    }

    fun setTitle(strTitle: String) {
        _searchableListDialog.setTitle(strTitle)
    }

    fun setPositiveButton(strPositiveButtonText: String) {
        _searchableListDialog.setPositiveButton(strPositiveButtonText)
    }

    private fun scanForActivity(cont: Context?): Activity? {
        if (cont == null)
            return null
        else if (cont is Activity)
            return cont
        else if (cont is ContextWrapper)
            return scanForActivity((cont as ContextWrapper).baseContext)

        return null
    }

    override fun getSelectedItemPosition(): Int {
        return if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            NO_ITEM_SELECTED
        } else {
            super.getSelectedItemPosition()
        }
    }

    override fun getSelectedItem(): Any? {
        return if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            null
        } else {
            super.getSelectedItem()
        }
    }

    override fun onSearchableItemClicked(item: Any?) {
        setSelection(_items.indexOf(item))

        if (!_isDirty) {
            _isDirty = true
            setAdapter(_arrayAdapter)
            setSelection(_items.indexOf(item))
        }
    }
}

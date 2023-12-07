package com.example.powerconsumptionactivitymonitor

import android.app.AlertDialog
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import java.io.Serializable



class SearchableListDialog : DialogFragment(), SearchView.OnQueryTextListener,
    SearchView.OnCloseListener {
    private var listAdapter: ArrayAdapter<*>? = null
    private var _listViewItems: ListView? = null
    private var _searchableItem: SearchableItem<*>? = null
    private var _onSearchTextChanged: SearchableListDialog.OnSearchTextChanged? = null
    private var _searchView: SearchView? = null
    private var _strTitle: String? = null
    private var _strPositiveButtonText: String? = null
    private var _onClickListener: DialogInterface.OnClickListener? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        return inflater.let { super.onCreateView(it, container, savedInstanceState) }
    }




    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Getting the layout inflater to inflate the view in an alert dialog.
        val inflater = LayoutInflater.from(activity)

        // Crash on orientation change #7
        // Change Start
        // Description: As the instance was re initializing to null on rotating the device,
        // getting the instance from the saved instance
        if (null != savedInstanceState) {
            _searchableItem =
                savedInstanceState.getSerializable("item") as SearchableItem<*>?
        }
        // Change End
        val rootView: View = inflater.inflate(R.layout.searchablespinnerlibrary_list_dialog, null)
        setData(rootView)
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialog.setView(rootView)
        val strPositiveButton =
            if (_strPositiveButtonText == null) "CLOSE" else _strPositiveButtonText!!
        alertDialog.setPositiveButton(strPositiveButton, _onClickListener)
        val strTitle = if (_strTitle == null) "Select Item" else _strTitle!!
        alertDialog.setTitle(strTitle)
        val dialog: AlertDialog = alertDialog.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        return dialog
    }

    // Crash on orientation change #7
    // Change Start
    // Description: Saving the instance of searchable item instance.
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("item", _searchableItem)
        super.onSaveInstanceState(outState)
    }

    // Change End
    fun setTitle(strTitle: String?) {
        _strTitle = strTitle
    }

    fun setPositiveButton(strPositiveButtonText: String?) {
        _strPositiveButtonText = strPositiveButtonText
    }

    fun setPositiveButton(
        strPositiveButtonText: String?,
        onClickListener: DialogInterface.OnClickListener?
    ) {
        _strPositiveButtonText = strPositiveButtonText
        _onClickListener = onClickListener
    }

    fun setOnSearchableItemClickListener(searchableItem: SearchableItem<*>) {
        _searchableItem = searchableItem
    }

    fun setOnSearchTextChangedListener(onSearchTextChanged: SearchableListDialog.OnSearchTextChanged?) {
        _onSearchTextChanged = onSearchTextChanged
    }

    private fun setData(rootView: View) {
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        _searchView = rootView.findViewById<View>(R.id.search) as SearchView
        _searchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        _searchView!!.setIconifiedByDefault(false)
        _searchView!!.setOnQueryTextListener(this)
        _searchView!!.setOnCloseListener(this)
        _searchView!!.clearFocus()
        val mgr = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.hideSoftInputFromWindow(_searchView!!.windowToken, 0)
        val items = arguments?.getSerializable(SearchableListDialog.Companion.ITEMS) as List<*>
        _listViewItems = rootView.findViewById<View>(R.id.listItems) as ListView

        //create the adapter by passing your ArrayList data
        listAdapter = ArrayAdapter<Any?>(
            requireContext(), android.R.layout.simple_list_item_1,
            items
        )
        //attach the adapter to the list
        _listViewItems!!.adapter = listAdapter
        _listViewItems!!.isTextFilterEnabled = true
        _listViewItems!!.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                _searchableItem?.onSearchableItemClicked((listAdapter as ArrayAdapter<Any?>).getItem(position))
                dialog?.dismiss()
            }
    }

    override fun onClose(): Boolean {
        return false
    }

    override fun onQueryTextSubmit(s: String?): Boolean {
        _searchView?.clearFocus()
        return true
    }

    override fun onQueryTextChange(s: String?): Boolean {
//        listAdapter.filterData(s);
        if (TextUtils.isEmpty(s)) {
//                _listViewItems.clearTextFilter();
            (_listViewItems!!.adapter as ArrayAdapter<*>).filter.filter(null)
        } else {
            (_listViewItems!!.adapter as ArrayAdapter<*>).filter.filter(s)
        }
        _onSearchTextChanged?.onSearchTextChanged(s)
        return true
    }

    interface SearchableItem<T> : Serializable {
        fun onSearchableItemClicked(item: Any?)
    }

    interface OnSearchTextChanged {
        fun onSearchTextChanged(strText: String?)
    }

    companion object {
        private const val ITEMS = "items"
        fun newInstance(items: List<*>?): SearchableListDialog {
            val multiSelectExpandableFragment = SearchableListDialog()
            val args = Bundle()
            args.putSerializable(SearchableListDialog.Companion.ITEMS, items as Serializable?)
            multiSelectExpandableFragment.arguments = args
            return multiSelectExpandableFragment
        }
    }
}
package com.yusuffahrudin.masuyamobileapp.util

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.SearchView

class EmptySearchView: SearchView {
    lateinit var mSearchSrcTextView: SearchView.SearchAutoComplete
    internal var listener: SearchView.OnQueryTextListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setOnQueryTextListener(listener: SearchView.OnQueryTextListener?) {
        super.setOnQueryTextListener(listener)
        this.listener = listener
        mSearchSrcTextView = this.findViewById(androidx.appcompat.R.id.search_src_text)
        mSearchSrcTextView.setOnEditorActionListener { textView, i, keyEvent ->
            listener?.onQueryTextSubmit(query.toString())
            true
        }
    }
}
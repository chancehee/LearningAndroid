package com.chancehee.unsplashapp_tutorial.recyclerview

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chancehee.unsplashapp_tutorial.R
import com.chancehee.unsplashapp_tutorial.model.Photo
import com.chancehee.unsplashapp_tutorial.model.SearchData
import com.chancehee.unsplashapp_tutorial.utils.Constant.TAG

class SearchHistoryRecyclerViewAdapter(searchHistoryRecyclerViewInterface: ISearchHistoryRecyclerView)
                                        : RecyclerView.Adapter<SearchItemViewHolder>() {

    private var searchHistoryList : ArrayList<SearchData> = ArrayList()

    private var iSearchHistoryRecyclerView : ISearchHistoryRecyclerView? = null

    init{
        Log.d(TAG, "SearchHistoryRecyclerViewAdapter - init() is called")
        this.iSearchHistoryRecyclerView = searchHistoryRecyclerViewInterface
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
        val searchItemViewHolder = SearchItemViewHolder(
            LayoutInflater
            .from(parent.context)
            .inflate(R.layout.layout_search_item,parent,false)
            , this.iSearchHistoryRecyclerView!!
        )
        return searchItemViewHolder
    }

    override fun getItemCount(): Int {
        return searchHistoryList.size
    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
        val dataItem : SearchData = this.searchHistoryList[position]
        holder.bindWithView(dataItem)
    }

    fun submitList(searchHistoryList: ArrayList<SearchData>){
        this.searchHistoryList = searchHistoryList
    }
}
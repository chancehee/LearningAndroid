package com.chancehee.unsplashapp_tutorial.utils

import android.content.Context
import android.util.Log
import com.chancehee.unsplashapp_tutorial.App
import com.chancehee.unsplashapp_tutorial.model.SearchData
import com.chancehee.unsplashapp_tutorial.utils.Constant.TAG
import com.google.gson.Gson

object SharedPrefManager {
    private const val SHARED_SEARCH_HISTORY = "shared_search_history"
    private const val KEY_SEARCH_HISTORY = "key_search_history"

    // 검색 목록을 저장
    fun storeSearchHistoryList(searchHistoryList: MutableList<SearchData>){
        Log.d(TAG, "SharedPrefManager - storeSearchHistoryList() is called")

        // 매개변수로 들어온 배열을 -> 문자열로 변환
        val searchHistoryListString : String = Gson().toJson(searchHistoryList)
        Log.d(TAG, "SharedPrefManager - searchHistoryListString : $searchHistoryListString")

        // 쉐어드 가져오기
        val shared = App.instance.getSharedPreferences(SHARED_SEARCH_HISTORY, Context.MODE_PRIVATE)

        // 쉐어드 에디터 가져오기
        val editor = shared.edit()

        editor.putString(KEY_SEARCH_HISTORY, searchHistoryListString)

        editor.apply()

    }

    // 검색 목록 가져오기
    fun getSearchHistoryList() : MutableList<SearchData>{


        // 쉐어드 가져오기
        val shared = App.instance.getSharedPreferences(SHARED_SEARCH_HISTORY, Context.MODE_PRIVATE)

        val storedSearchHistoryListString = shared.getString(KEY_SEARCH_HISTORY, "")!!

        var storedSearchHistoryList = ArrayList<SearchData>()

        // 검색 목록이 값이 있다면
        if (storedSearchHistoryListString.isNotEmpty()){
            // 저장된 문자열을 -> 객체 배열로 변경
            storedSearchHistoryList = Gson().fromJson(storedSearchHistoryListString, Array<SearchData>::class.java).toMutableList() as ArrayList<SearchData>
        }

        return storedSearchHistoryList
    }

}
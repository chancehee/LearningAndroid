package com.chancehee.unsplashapp_tutorial.retrofit

import android.util.Log
import com.chancehee.unsplashapp_tutorial.utils.API
import com.chancehee.unsplashapp_tutorial.utils.Constant.TAG
import com.chancehee.unsplashapp_tutorial.utils.RESPONSE_STATE
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Response
import retrofit2.create


class RetrofitManager {

    companion object{
        val instance = RetrofitManager()
    }

    // http 콜 만들기
    // 레트로핏 인터페이스 가져오기
    private val iRetrofit : IRetrofit? = RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)

    // 사진 검색 api 호출
    fun searchPhotos(searchTerm: String?, completion: (RESPONSE_STATE,String) -> Unit) {

        val term = searchTerm.let {
            it
        } ?: ""
        // val term = searchTerm ?: ""


        val call = iRetrofit?.searchPhotos(searchTerm = term).let {
            it
        } ?: return

        call.enqueue(object : retrofit2.Callback<JsonElement> {
            // 응답실패시
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d(TAG, "RetrofitManager - onFailure() is called / t: $t")

                completion(RESPONSE_STATE.FAIL, t.toString())
            }


            // 응답성공시
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d(TAG, "RetrofitManager - onResponse() is called / response: ${response.raw()}")

                completion(RESPONSE_STATE.OKAY, response.body().toString())
            }
        })
    }

}
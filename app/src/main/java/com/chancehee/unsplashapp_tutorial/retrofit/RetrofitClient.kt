package com.chancehee.unsplashapp_tutorial.retrofit

import android.util.Log
import com.chancehee.unsplashapp_tutorial.utils.API
import com.chancehee.unsplashapp_tutorial.utils.Constant.TAG
import com.chancehee.unsplashapp_tutorial.utils.isJsonArray
import com.chancehee.unsplashapp_tutorial.utils.isJsonObject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.concurrent.TimeUnit

// object는 싱글턴이다.(메모리를 하나만 쓴다.)
object RetrofitClient {
    // 레트로핏 클라이언트 선언

    //private lateinit var retrofit: Retrofit
    private var retrofitClient: Retrofit? = null

    // 레트로핏 클라이언트 가져오기 ( 반환하는 값이 Retrofit이다.)
    fun getClient(baseUrl: String): Retrofit? {
        Log.d(TAG, "RetrofitClient - getClient() is called")


        // okhttp 인스턴스 생성
        val client = OkHttpClient.Builder()

        // 로그를 찍기 위해 로깅 인터셉터 설정
        val loggingInterceptor = HttpLoggingInterceptor(object: HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d(TAG, "RetrofitClient - log() is called / message: $message")

                when{
                    message.isJsonObject() ->
                        Log.d(TAG, JSONObject(message).toString(4))
                    message.isJsonArray() ->
                        Log.d(TAG, JSONArray(message).toString(4))
                    else ->{
                        try{
                            Log.d(TAG, JSONObject(message).toString(4))
                        }catch (e: Exception){
                            Log.d(TAG, message)
                        }
                    }
                }
            }

        })

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        // 위에서 설정한 로깅 인터셉터를 okhttp 클라이언트에 추가한다.
        client.addInterceptor(loggingInterceptor)

        // 커넥션 타임아웃
        client.connectTimeout(10,TimeUnit.SECONDS)
        client.readTimeout(10,TimeUnit.SECONDS)
        client.writeTimeout(10,TimeUnit.SECONDS)
        client.retryOnConnectionFailure(true)


        // 기본 파라미터 인터셉터 설정
        val baseParameterInterceptor : Interceptor = (object : Interceptor{

            override fun intercept(chain: Interceptor.Chain): Response {
                Log.d(TAG, "RetrofitClient - intercept() is called")
                // 오리지날 리퀘스트
                val originalRequest = chain.request()

                // ?client_id =
                // 쿼리 파라미터 추가하기
                val addedUrl = originalRequest.url.newBuilder().addQueryParameter("client_id", API.CLIENT_ID).build()

                val finalRequest = originalRequest.newBuilder()
                    .url(addedUrl)
                    .method(originalRequest.method, originalRequest.body)
                    .build()

                return chain.proceed(finalRequest)
            }

        })

        // 위에서 설정한 로깅 인터셉터를 okhttp 클라이언트에 추가한다.
        client.addInterceptor(baseParameterInterceptor)





        if(retrofitClient == null){

            // 레트로핏 빌더를 통해 인스턴스 생성
            retrofitClient = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                // 위에서 설정한 클라이언트로 레트로핏 클라이언트를 설정한다.
                .client(client.build())
                .build()
        }

        return retrofitClient
    }

}
package com.mskj.mercer.app

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import com.mskj.mercer.practice.Result
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface OssTokenApi {

    @GET("/file/oss/stsToken")
    fun queryOssToken(
        @Query("type") type: String
    ): Deferred<Result>

    companion object {
        fun create(): OssTokenApi {
            val builder = OkHttpClient.Builder()
            builder.addInterceptor(OkHttpProfilerInterceptor())
            val client = builder.build()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(client)
                .build()
                .create(OssTokenApi::class.java)
        }
    }

    @Streaming
    @GET
    fun fetchBitmap(@Url url: String): Deferred<ResponseBody>

}
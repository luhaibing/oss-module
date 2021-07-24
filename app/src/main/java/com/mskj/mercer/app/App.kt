package com.mskj.mercer.app

import android.app.Application
import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mskj.mercer.oss.OssManager
import com.mskj.mercer.oss.model.OssEntity
import com.mskj.mercer.oss.model.Ploy
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class App : Application() {

    private val simpleDateFormat by lazy { SimpleDateFormat("yyyyMMdd") }

    override fun onCreate() {
        super.onCreate()
        val create = OssTokenApi.create()
        OssManager().initialize(
            this, onLoadOssEntityForRemote = suspend {
                val response = create.queryOssToken("android").await()
                Log.e("TAG", "----------- response: $response ----------- ")
                if (response.code != 200) {
                    throw Exception(response.message)
                }
                /*
                // oos 端点
                val endpoint: String,
                val bucket: String,
                // 访问密匙Id
                val accessKeyId: String,
                // 密匙Id
                val secretKeyId: String,
                // 密匙Token
                val securityToken: String,
                // 有效时长
                val expires: Long,
                // 保存的时间点
                val timestamp: Long,
                */
                val result = response.result
                OssEntity(
                    "http://oss-cn-shenzhen.aliyuncs.com",
                    result.bucket,
                    result.accessKeyId,
                    result.accessKeySecret,
                    result.stsToken,
                    result.expiration,
                    System.currentTimeMillis()
                )
            }, dataFetcher = {
                create.fetchBitmap(it).await().byteStream()
            }, {
                val currentTime = System.currentTimeMillis()
                val date = Date(currentTime)
                "android_" + simpleDateFormat.format(date) + "_" + 72 + "_" + currentTime + ".jpg"
            }, ploy = Ploy.SPLICE, splice = {
                // 直接拼接
                BuildConfig.DIRECT_SPLICE_URL + it
            }
        )
    }

}
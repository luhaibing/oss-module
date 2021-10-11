package com.mskj.mercer.app

import android.app.Application
import android.util.Log
import com.mskj.mercer.oss.OssManager
import com.mskj.mercer.oss.model.OssEntity
import com.mskj.mercer.oss.model.Ploy
import java.text.SimpleDateFormat
import java.util.*

class App : Application() {

    private val simpleDateFormat by lazy { SimpleDateFormat("yyyyMMdd") }

    override fun onCreate() {
        super.onCreate()
        val create = OssTokenApi.create()
        OssManager().initialize(
            ctx = this, onLoadOssEntityForRemote = suspend {
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
                    "https://endpoint.file.ihk.ltd/",
                    "https://file.ihk.ltd/",
                    result.bucket,
                    result.accessKeyId,
                    result.accessKeySecret,
                    result.stsToken,
                    result.expiration,
                    System.currentTimeMillis()
                )
            }, convert = {
                val currentTime = System.currentTimeMillis()
                val date = Date(currentTime)
                "android_" + simpleDateFormat.format(date) + "_" + 72 + "_" + currentTime + ".jpg"
            }, ploy = Ploy.Dynamic { e, k ->
                Ploy.Splice("https://file.ihk.ltd").process(Unit, k)
            }
        )
    }

}
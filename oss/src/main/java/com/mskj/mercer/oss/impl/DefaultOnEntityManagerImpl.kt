package com.mskj.mercer.oss.impl

import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import com.mskj.mercer.oss.action.OnOssEntityManager
import com.mskj.mercer.oss.model.OssEntity
import com.mskj.mercer.oss.util.KEY_ALI_OSS_TOKEN

class DefaultOnEntityManagerImpl : OnOssEntityManager {

    private lateinit var ossEntityRemoteCallBack: suspend () -> OssEntity

    private val gson by lazy { Gson() }

   override fun ossEntityRemoteCallBack(block: suspend () -> OssEntity) {
        ossEntityRemoteCallBack = block
    }

    override fun saveResponseToLocal(response: OssEntity) {
        SPUtils.getInstance().put(KEY_ALI_OSS_TOKEN, gson.toJson(response))
    }

    override suspend fun loadEntityForRemote(): OssEntity {
        return ossEntityRemoteCallBack()
    }

    override fun loadEntityForLocal(): OssEntity {
        return Gson().fromJson(
            SPUtils.getInstance().getString(KEY_ALI_OSS_TOKEN),
            OssEntity::class.java
        ) ?: throw NullPointerException("local response is null.")
    }

}
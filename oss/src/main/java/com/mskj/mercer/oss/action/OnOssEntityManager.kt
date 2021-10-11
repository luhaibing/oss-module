package com.mskj.mercer.oss.action

import com.mskj.mercer.oss.model.OssEntity

interface OnOssEntityManager {

    fun saveResponseToLocal(response: OssEntity)

    suspend fun loadEntityForRemote(): OssEntity

    fun loadEntityForLocal(): OssEntity?

    fun ossEntityRemoteCallBack(block: suspend () -> OssEntity)

}
package com.mskj.mercer.oss.action

import com.mskj.mercer.oss.model.OssEntity

/**
 * 从服务获取创建OSSClient需要的信息
 */
interface OnOssEntityCallBack {

    suspend fun loadEntityForRemote(): OssEntity

}
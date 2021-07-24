package com.mskj.mercer.oss.model

import androidx.annotation.Keep

@Keep
data class OssEntity(
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
    val timestamp: Long
)
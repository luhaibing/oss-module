package com.mskj.mercer.practice

import androidx.annotation.Keep
import com.google.gson.Gson

val gson = Gson()

@Keep
data class Result(
    val code: Int,
    val i18n: Any,
    val message: String,
    val result: AliSooToken,
    val success: Boolean,
    val timestamp: Long
) {
    override fun toString(): String = gson.toJson(this)
}

@Keep
data class AliSooToken(
    val accessKeyId: String,
    val accessKeySecret: String,
    val bucket: String,
    val expiration: Long,
    // val id: Any,
    val region: String,
    val secure: Boolean,
    val stsToken: String,
    var timestamp: Long = 0
)
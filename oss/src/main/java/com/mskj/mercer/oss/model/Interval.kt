package com.mskj.mercer.oss.model

import com.alibaba.sdk.android.oss.model.Range

/**
 * @author      ：mercer
 * @date        ：2021-08-13  01:13
 * @description ：断点
 */
data class Interval(
    val start: Long,
    val end: Long = INFINITE
) {
    companion object {
        const val INFINITE = Range.INFINITE
    }
}

fun Interval?.convert(): Range? = this?.let { Range(start, end) }

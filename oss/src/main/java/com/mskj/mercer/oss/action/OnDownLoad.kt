package com.mskj.mercer.oss.action

import com.mskj.mercer.oss.model.Interval
import com.mskj.mercer.oss.model.Ploy
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.InputStream

interface OnDownLoad {

    fun fetch(key: String?): Flow<InputStream>

    fun fetch(key: String?, ploy: Ploy): Flow<InputStream>

    //////////////////////////////////////////////////////////////////

    fun downLoad(
        key: String?, file: File, interval: Interval? = null,
        onProgressListener: ((Long, Long) -> Unit)? = null
    ): Flow<File>

    fun downLoadByStream(
        key: String?, file: File, interval: Interval? = null,
        onProgressListener: ((Long, Long) -> Unit)? = null
    ): Flow<File>

}
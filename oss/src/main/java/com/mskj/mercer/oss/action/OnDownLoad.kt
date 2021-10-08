package com.mskj.mercer.oss.action

import com.mskj.mercer.oss.OssManager
import com.mskj.mercer.oss.model.Interval
import com.mskj.mercer.oss.model.Ploy
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.InputStream

interface OnDownLoad {

    //////////////////////////////////////////////////////////////////

    fun downLoad(
        key: String?, file: File, interval: Interval? = null,
        onProgressListener: ((Long, Long) -> Unit)? = null
    ): Flow<File>

    fun downLoadByStream(
        key: String?, file: File, interval: Interval? = null,
        onProgressListener: ((Long, Long) -> Unit)? = null
    ): Flow<File>

    //////////////////////////////////////////////////////////////////

    suspend fun pull(
        key: String?, file: File, interval: Interval? = null,
        onProgressListener: ((Long, Long) -> Unit)? = null
    ): File

    /**
     * 流式下载
     * 主要是文件过大时采用这用方式
     */
    suspend fun pullByStream(
        key: String?, file: File, interval: Interval? = null,
        onProgressListener: ((Long, Long) -> Unit)? = null
    ): File

}
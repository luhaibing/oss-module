package com.mskj.mercer.oss.action

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File

interface UpLoad<Output> {

    fun prepare(block: (String) -> String)

    //////////////////////////////////////////////////////////////////

    fun upLoad(
        input: Uri?, onProgressListener: ((Long, Long) -> Unit)? = null
    ): Flow<Pair<String, Output>>

    fun upLoad(
        input: File?, onProgressListener: ((Long, Long) -> Unit)? = null
    ): Flow<Pair<String, Output>>

    fun upLoad(
        input: String?, onProgressListener: ((Long, Long) -> Unit)? = null
    ): Flow<Pair<String, Output>>

    //////////////////////////////////////////////////////////////////

    suspend fun push(
        input: Uri?, onProgressListener: ((Long, Long) -> Unit)? = null
    ): Pair<String, Output>

    suspend fun push(
        input: File?, onProgressListener: ((Long, Long) -> Unit)? = null
    ): Pair<String, Output>

    suspend  fun push(
        input: String?, onProgressListener: ((Long, Long) -> Unit)? = null
    ): Pair<String, Output>

}
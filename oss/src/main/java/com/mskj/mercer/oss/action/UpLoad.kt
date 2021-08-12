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

}
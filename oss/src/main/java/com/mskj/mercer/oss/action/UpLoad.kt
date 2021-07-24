package com.mskj.mercer.oss.action

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File

interface UpLoad<Output> {

    fun prepare(block: (String) -> String)

    fun upLoad(input: String): Flow<Pair<String, Output>>

    fun upLoad(input: File): Flow<Pair<String, Output>>

    fun upLoad(input: Uri): Flow<Pair<String, Output>>

}
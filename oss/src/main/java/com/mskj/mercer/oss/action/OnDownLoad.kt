package com.mskj.mercer.oss.action

import com.mskj.mercer.oss.model.Ploy
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface OnDownLoad {

    fun downLoad(key: String): Flow<InputStream>

    fun downLoad(key: String, ploy: Ploy): Flow<InputStream>

}
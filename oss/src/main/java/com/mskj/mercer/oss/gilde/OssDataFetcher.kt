package com.mskj.mercer.oss.gilde

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.mskj.mercer.oss.OssManager
import com.mskj.mercer.oss.impl.DefaultOnDownLoadImpl
import com.mskj.mercer.oss.impl.DefaultOnDownLoadImpl.Companion.httpFetchFile
import com.mskj.mercer.oss.model.Ploy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.InputStream

class OssDataFetcher(private val key: String) : DataFetcher<InputStream> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        scope.launch {
            when (OssManager().ploy()) {
                Ploy.SPLICE -> {
                    flow {
                        val splice = OssManager().splice()
                            ?: throw NullPointerException("use  Ploy.SPLICE must set OssManager.splice.")
                        emit(httpFetchFile(splice.invoke(key)))
                    }
                }
                Ploy.DEFAULT -> {
                    flow {
                        val entity = OssManager().obtainOssEntity()
                        val result = DefaultOnDownLoadImpl.defaultFetchFile(entity, key, null, null)
                        emit(result)
                    }
                }
                else -> {
                    flow {
                        val entity = OssManager().obtainOssEntity()
                        val result = httpFetchFile(entity, key)
                        emit(result)
                    }
                }
            }.catch { throwable ->
                callback.onLoadFailed(Exception(throwable))
                throwable.printStackTrace()
            }.collect {
                callback.onDataReady(it)
            }
        }
    }

    override fun cleanup() {
        scope.cancel()
    }

    private val scope by lazy {
        CoroutineScope(Dispatchers.IO)
    }

    override fun cancel() {
        scope.cancel()
    }

    override fun getDataClass(): Class<InputStream> = InputStream::class.java

    override fun getDataSource() = DataSource.REMOTE


}
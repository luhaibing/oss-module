package com.mskj.mercer.oss.gilde

import android.util.Log
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.mskj.mercer.oss.BuildConfig
import com.mskj.mercer.oss.OssManager
import com.mskj.mercer.oss.impl.DefaultOnDownLoadImpl
import com.mskj.mercer.oss.model.OssEntity
import com.mskj.mercer.oss.model.Ploy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.lang.NullPointerException

class OssDataFetcher(private val key: String) : DataFetcher<InputStream> {

    companion object {
        val TAG = OssDataFetcher::class.java.simpleName
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        scope.launch {
            OssManager().obtainOssEntity()
                .map {
                    fetchBitmapAsync(it)
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

    private suspend fun fetchBitmapAsync(it: OssEntity): InputStream {
        return when (OssManager().ploy()) {
            Ploy.DEFAULT -> {
                DefaultOnDownLoadImpl.defaultFetchFile(it, key)
            }
            Ploy.OKHTTP -> {
                DefaultOnDownLoadImpl.okHttpFetchFile(it, key)
            }
            else -> {
                val splice = OssManager().splice()
                    ?: throw NullPointerException("use  Ploy.SPLICE must set OssManager.splice.")
                DefaultOnDownLoadImpl.okHttpFetchFile(splice.invoke(key))
            }
        }
    }


}
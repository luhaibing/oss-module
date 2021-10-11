package com.mskj.mercer.oss.gilde

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.mskj.mercer.oss.OssManager
import com.mskj.mercer.oss.model.Ploy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.InputStream

class OssDataFetcher(private val key: String) : DataFetcher<InputStream> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        scope.launch {
            when (val ploy = OssManager().ploy()) {
                is Ploy.Default -> {
                    flow {
                        emit(ploy.process(OssManager().obtainOssEntity(), key))
                    }
                }
                is Ploy.Splice -> {
                    flow {
                        emit(ploy.process(Unit, key))
                    }
                }
                is Ploy.Dynamic -> {
                    flow {
                        val input = if (ploy.useOss()) {
                            try {
                                OssManager().obtainOssEntity()
                            } catch (e: Exception) {
                                null
                            }
                        } else {
                            null
                        }
                        emit(ploy.process(input, key))
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
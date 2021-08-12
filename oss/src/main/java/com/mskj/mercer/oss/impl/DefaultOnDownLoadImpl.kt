package com.mskj.mercer.oss.impl

import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.model.GetObjectRequest
import com.alibaba.sdk.android.oss.model.GetObjectResult
import com.mskj.mercer.oss.OssManager
import com.mskj.mercer.oss.action.OnDownLoad
import com.mskj.mercer.oss.model.*
import com.mskj.mercer.oss.throwable.OssException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.*


class DefaultOnDownLoadImpl : OnDownLoad {

    override fun fetch(key: String?): Flow<InputStream> = fetch(key, OssManager().ploy())

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("recommended use Ploy.DEFAULT")
    override fun fetch(key: String?, ploy: Ploy): Flow<InputStream> =
        if (key.isNullOrBlank()) {
            flow {
                throw NullPointerException("key can not be null.")
            }
        } else {
            OssManager().obtainOssEntity().map {
                if (ploy == Ploy.HTTP) {
                    httpFetchFile(it, key)
                } else {
                    defaultFetchFile(it, key, null, null)
                }
            }
        }

    override fun downLoad(
        key: String?, file: File, interval: Interval?, onProgressListener: ((Long, Long) -> Unit)?
    ): Flow<File> = if (key.isNullOrBlank()) {
        flow {
            throw NullPointerException("key can not be null.")
        }
    } else {
        OssManager().obtainOssEntity().map {
            defaultFetchFile(it, key, interval, onProgressListener)
        }.map {
            file.mkdirs()
            if (file.exists()) {
                file.delete()
            }
            file.writeBytes(it.readBytes())
            file
        }
    }

    override fun downLoadByStream(
        key: String?, file: File, interval: Interval?, onProgressListener: ((Long, Long) -> Unit)?
    ): Flow<File> = if (key.isNullOrBlank()) {
        flow {
            throw NullPointerException("key can not be null.")
        }
    } else {
        OssManager().obtainOssEntity().map { ossEntity ->
            val get = GetObjectRequest(ossEntity.bucket, key)
            get.setProgressListener { _, currentSize, totalSize ->
                onProgressListener?.invoke(currentSize, totalSize)
            }
            interval?.convert()?.let {
                get.range = it
            }
            val oss = OssManager.ossClient(ossEntity)

            file.mkdirs()
            if (file.exists()) {
                file.delete()
            }
            val outStream = FileOutputStream(file)

            // 同步执行下载请求，返回结果
            val getResult: GetObjectResult = oss.getObject(get)
            // 获取文件输入流
            val inputStream = getResult.objectContent
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                outStream.write(buffer, 0, len)
            }
            outStream.flush()
            outStream.close()
            file
        }.flowOn(Dispatchers.IO)
    }


    companion object {

        suspend fun defaultFetchFile(
            it: OssEntity,
            key: String,
            interval: Interval?,
            onProgressListener: ((Long, Long) -> Unit)?
        ): InputStream {
            val deferred = CompletableDeferred<InputStream>()
            val get = GetObjectRequest(it.bucket, key)
            get.setProgressListener { _, currentSize, totalSize ->
                onProgressListener?.invoke(currentSize, totalSize)
            }
            interval?.convert()?.let {
                get.range = it
            }
            OssManager.ossClient(it)
                .asyncGetObject(
                    get,
                    object : OSSCompletedCallback<GetObjectRequest, GetObjectResult> {
                        override fun onSuccess(request: GetObjectRequest, result: GetObjectResult) {
                            val length = result.contentLength
                            val buffer = ByteArray(length.toInt())
                            var readCount = 0
                            while (readCount < length) {
                                try {
                                    readCount += result.objectContent.read(
                                        buffer, readCount, length.toInt() - readCount
                                    )
                                } catch (e: Exception) {
                                    deferred.completeExceptionally(e)
                                }
                            }
                            deferred.complete(ByteArrayInputStream(buffer))
                        }

                        override fun onFailure(
                            request: GetObjectRequest,
                            clientException: ClientException,
                            serviceException: ServiceException
                        ) {
                            deferred.completeExceptionally(
                                OssException(request, clientException, serviceException)
                            )
                        }
                    })
            return deferred.await()
        }

        suspend fun httpFetchFile(it: OssEntity, key: String): InputStream {
            val url = OssManager.ossClient(it).presignConstrainedObjectURL(
                it.bucket, key, it.expires
            )
            return httpFetchFile(url)
        }

        suspend fun httpFetchFile(url: String): InputStream {
            val deferred = CompletableDeferred<InputStream>()
            try {
                val result = OssManager().dataFetcher(url)
                    ?: throw NullPointerException("result can not be null.")
                deferred.complete(result)
            } catch (e: Exception) {
                deferred.completeExceptionally(e)
            }
            return deferred.await()
        }

    }

}
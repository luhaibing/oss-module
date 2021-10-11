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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.*

@Suppress("BlockingMethodInNonBlockingContext")
class DefaultOnDownLoadImpl : OnDownLoad {

    override fun downLoad(
        key: String?, file: File, interval: Interval?, onProgressListener: ((Long, Long) -> Unit)?
    ): Flow<File> = flow {
        val result = pull(key, file, interval, onProgressListener)
        emit(result)
    }

    override fun downLoadByStream(
        key: String?, file: File, interval: Interval?, onProgressListener: ((Long, Long) -> Unit)?
    ): Flow<File> = flow {
        val result = pullByStream(key, file, interval, onProgressListener)
        emit(result)
    }.flowOn(Dispatchers.IO)

    override suspend fun pull(
        key: String?, file: File, interval: Interval?, onProgressListener: ((Long, Long) -> Unit)?
    ): File {
        if (key.isNullOrBlank()) {
            throw NullPointerException("key can not be null.")
        }
        val entity = OssManager().obtainOssEntity()
        var inputStream: InputStream? = null
        try {
            inputStream = defaultFetchFile(entity, key, interval, onProgressListener)
            file.mkdirs()
            if (file.exists()) {
                file.delete()
            }
            file.writeBytes(inputStream.readBytes())
        } catch (e: Exception) {
            throw e
        } finally {
            inputStream?.close()
        }
        return file
    }

    override suspend fun pullByStream(
        key: String?,
        file: File,
        interval: Interval?,
        onProgressListener: ((Long, Long) -> Unit)?
    ): File {
        if (key.isNullOrBlank()) {
            throw NullPointerException("key can not be null.")
        }
        val entity = OssManager().obtainOssEntity()
        val get = GetObjectRequest(entity.bucket, key)
        get.setProgressListener { _, currentSize, totalSize ->
            onProgressListener?.invoke(currentSize, totalSize)
        }
        interval?.convert()?.let {
            get.range = it
        }
        val oss = OssManager.ossClient(entity)

        file.mkdirs()
        if (file.exists()) {
            file.delete()
        }
        var outStream: FileOutputStream? = null
        try {
            outStream = FileOutputStream(file)
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
        } catch (e: Exception) {
            throw e
        } finally {
            outStream?.close()
        }
        return file
    }


    companion object {

        private val client by lazy {
            OkHttpClient()
        }

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

        /*
        suspend fun httpFetchFile(it: OssEntity, key: String): InputStream {
            val url = OssManager.ossClient(it).presignConstrainedObjectURL(
                it.bucket, key, it.expires
            )
            return httpFetchFile(url)
        }
        */

        suspend fun okHttpFetchFile(url: String): InputStream {
            val deferred = CompletableDeferred<InputStream>()
            try {
                val result = dataFetcher(url)
                    ?: throw NullPointerException("result can not be null.")
                deferred.complete(result)
            } catch (e: Exception) {
                deferred.completeExceptionally(e)
            }
            return deferred.await()
        }

        private fun dataFetcher(value: String): InputStream? {
            //获取请求对象
            val request = Request.Builder().url(value).build()
            //获取响应体
            val body: ResponseBody? = client.newCall(request).execute().body
            //获取流
            return body?.byteStream()
        }

    }

}
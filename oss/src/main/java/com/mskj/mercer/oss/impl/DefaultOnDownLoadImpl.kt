package com.mskj.mercer.oss.impl

import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.model.GetObjectRequest
import com.alibaba.sdk.android.oss.model.GetObjectResult
import com.mskj.mercer.oss.OssManager
import com.mskj.mercer.oss.action.OnDownLoad
import com.mskj.mercer.oss.model.OssEntity
import com.mskj.mercer.oss.model.Ploy
import com.mskj.mercer.oss.throwable.OssException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.ByteArrayInputStream
import java.io.InputStream

class DefaultOnDownLoadImpl : OnDownLoad {

    override fun downLoad(key: String): Flow<InputStream> = downLoad(key, OssManager().ploy())

    override fun downLoad(key: String, ploy: Ploy): Flow<InputStream> =
        OssManager().obtainOssEntity().map {
            if (ploy == Ploy.HTTP) {
                httpFetchFile(it, key)
            } else {
                defaultFetchFile(it, key)
            }
        }

    companion object {

        suspend fun defaultFetchFile(it: OssEntity, key: String): InputStream {
            val deferred = CompletableDeferred<InputStream>()
            val get = GetObjectRequest(it.bucket, key)
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
                deferred.complete(result)
            } catch (e: Exception) {
                deferred.completeExceptionally(e)
            }
            return deferred.await()
        }

    }

}
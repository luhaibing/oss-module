package com.mskj.mercer.oss.impl

import android.net.Uri
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.blankj.utilcode.util.UriUtils
import com.mskj.mercer.oss.OssManager
import com.mskj.mercer.oss.action.UpLoad
import com.mskj.mercer.oss.model.Motion
import com.mskj.mercer.oss.model.OssEntity
import com.mskj.mercer.oss.throwable.OssException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class DefaultUpLoadImpl : UpLoad<String> {

    private lateinit var prepare: (String) -> String

    override fun prepare(block: (String) -> String) {
        prepare = block
    }

    //////////////////////////////////////////////////////////////////

    override fun upLoad(
        input: Uri?,
        onProgressListener: ((Long, Long) -> Unit)?
    ): Flow<Pair<String, String>> = upLoad(UriUtils.uri2File(input), onProgressListener)

    override fun upLoad(
        input: File?,
        onProgressListener: ((Long, Long) -> Unit)?
    ): Flow<Pair<String, String>> = upLoad(input?.absolutePath, onProgressListener)

    override fun upLoad(
        input: String?,
        onProgressListener: ((Long, Long) -> Unit)?
    ): Flow<Pair<String, String>> = flow {
        val result = push(input, onProgressListener)
        emit(result)
    }

    //////////////////////////////////////////////////////////////////

    override suspend fun push(
        input: Uri?,
        onProgressListener: ((Long, Long) -> Unit)?
    ): Pair<String, String> = push(UriUtils.uri2File(input), onProgressListener)

    override suspend fun push(
        input: File?,
        onProgressListener: ((Long, Long) -> Unit)?
    ): Pair<String, String> = push(input?.absolutePath, onProgressListener)

    override suspend fun push(
        input: String?,
        onProgressListener: ((Long, Long) -> Unit)?
    ): Pair<String, String> =
        if (input.isNullOrBlank()) {
            throw NullPointerException("input can not be null or blank.")
        } else {
            OssManager().obtainOssEntity().upLoadAsync(input, onProgressListener)
        }

    //////////////////////////////////////////////////////////////////

    private suspend fun OssEntity.upLoadAsync(
        input: String,
        onProgressListener: ((Long, Long) -> Unit)?
    ): Pair<String, String> {
        val deferred = CompletableDeferred<Pair<String, String>>()
        val key = prepare(input)

        val put = PutObjectRequest(bucket, key, input)
        put.setProgressCallback { _, currentSize, totalSize ->
            onProgressListener?.invoke(currentSize, totalSize)
        }
        val ossClient = OssManager.ossClient(Motion.PUSH, this)
        ossClient.asyncPutObject(
            put,
            object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                override fun onSuccess(
                    request: PutObjectRequest?,
                    result: PutObjectResult?
                ) {
                    deferred.complete(input to key)
                }

                override fun onFailure(
                    request: PutObjectRequest,
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

}
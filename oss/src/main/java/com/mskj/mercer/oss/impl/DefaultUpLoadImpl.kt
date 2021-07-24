package com.mskj.mercer.oss.impl

import android.net.Uri
import android.util.Log
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.blankj.utilcode.util.UriUtils
import com.mskj.mercer.oss.OssManager
import com.mskj.mercer.oss.action.UpLoad
import com.mskj.mercer.oss.model.OssEntity
import com.mskj.mercer.oss.throwable.OssException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class DefaultUpLoadImpl : UpLoad<String> {

    private lateinit var prepare: (String) -> String

    override fun prepare(block: (String) -> String) {
        prepare = block
    }

    override fun upLoad(input: String): Flow<Pair<String, String>> = OssManager()
        .obtainOssEntity()
        .upLoadAsync(input)

    override fun upLoad(input: File): Flow<Pair<String, String>> = upLoad(input.absolutePath)

    override fun upLoad(input: Uri): Flow<Pair<String, String>> = upLoad(UriUtils.uri2File(input))

    private fun Flow<OssEntity>.upLoadAsync(input: String): Flow<Pair<String, String>> = map {
        val deferred = CompletableDeferred<Pair<String, String>>()
        val key = prepare(input)

        val put = PutObjectRequest(it.bucket, key, input)
        val ossClient = OssManager.ossClient(it)
        ossClient.asyncPutObject(put,
            object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                override fun onSuccess(
                    request: PutObjectRequest?,
                    result: PutObjectResult?
                ) {
                    Log.e("TAG", "onSuccess")
                    deferred.complete(input to key)
                }

                override fun onFailure(
                    request: PutObjectRequest,
                    clientException: ClientException,
                    serviceException: ServiceException
                ) {
                    Log.e("TAG", "onFailure --> request          : ${request}")
                    Log.e("TAG", "onFailure --> clientException  : ${clientException}")
                    Log.e("TAG", "onFailure --> serviceException : ${serviceException}")
                    deferred.completeExceptionally(
                        OssException(
                            request,
                            clientException,
                            serviceException
                        )
                    )
                }
            })
        deferred.await()
    }

//    private fun upLoadAsync2(it: OssEntity, input: String): Deferred<Pair<String, String>> {
//        val deferred = CompletableDeferred<Pair<String, String>>()
//        val key = prepare(input)
//        val ossClient = AliOssClientManager(context, it)
//        // 构造上传请求。
//        val put = PutObjectRequest(it.bucket, key, input)
//        val asyncPutObject = ossClient.asyncPutObject(put,
//            object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
//                override fun onSuccess(
//                    request: PutObjectRequest,
//                    result: PutObjectResult
//                ) {
//                    deferred.complete(input to key)
//                }
//
//                override fun onFailure(
//                    request: PutObjectRequest,
//                    clientException: ClientException,
//                    serviceException: ServiceException
//                ) {
//                    deferred.completeExceptionally(
//                        OssException(
//                            input to key,
//                            request,
//                            clientException,
//                            serviceException
//                        )
//                    )
//                }
//            }
//        )
//        deferred.invokeOnCompletion {
//            // 取消
//            asyncPutObject.cancel()
//        }
//
//        return deferred
//    }


}
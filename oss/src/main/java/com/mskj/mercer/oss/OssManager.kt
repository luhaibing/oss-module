package com.mskj.mercer.oss

import android.annotation.SuppressLint
import android.content.Context
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.mskj.mercer.oss.action.OnDownLoad
import com.mskj.mercer.oss.action.OnOssEntityCallBack
import com.mskj.mercer.oss.action.OnOssEntityManager
import com.mskj.mercer.oss.action.UpLoad
import com.mskj.mercer.oss.impl.DefaultOnDownLoadImpl
import com.mskj.mercer.oss.impl.DefaultOnEntityManagerImpl
import com.mskj.mercer.oss.impl.DefaultUpLoadImpl
import com.mskj.mercer.oss.model.OssEntity
import com.mskj.mercer.oss.model.Ploy
import com.mskj.mercer.oss.throwable.TokenExpirationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class OssManager private constructor() : OnOssEntityManager by DefaultOnEntityManagerImpl(),
    UpLoad<String> by DefaultUpLoadImpl(),
    OnDownLoad by DefaultOnDownLoadImpl() {

    private lateinit var context: Context

    private var ploy: Ploy = Ploy.DEFAULT

    fun context() = context

    private lateinit var dataFetcher: suspend (String) -> InputStream

    suspend fun dataFetcher(value: String) = dataFetcher.invoke(value)

    fun ploy() = ploy

    // glide 加载时,直接拼接路径的方式
    private var splice: ((String) -> String)? = null

    fun splice() = splice

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val INSTANCE = OssManager()
    }

    companion object {

        operator fun invoke(): OssManager {
            return Holder.INSTANCE
        }

        fun ossClient(
            entity: OssEntity,
            conf: ClientConfiguration = defaultClientConfiguration()
        ): OSSClient {
            val ossCredentialProvider = OSSStsTokenCredentialProvider(
                entity.accessKeyId,
                entity.secretKeyId,
                entity.securityToken
            )
            return OSSClient(invoke().context(), entity.endpoint, ossCredentialProvider, conf)
        }

        private fun defaultClientConfiguration(): ClientConfiguration {
            return ClientConfiguration().apply {
                // 连接超时，默认15秒。
                connectionTimeout = 15 * 1000
                // socket超时，默认15秒。
                socketTimeout = 15 * 1000
                // 最大并发请求书，默认5个。
                maxConcurrentRequest = 5
                // 失败后最大重试次数，默认2次。
                maxErrorRetry = 2
            }
        }

    }

    /**
     * @param   onOssEntityCallBack 更新ossToken
     * @param   dataFetcher         使用使用非default方法加载图片时,拉取图片的回调
     * @param   convert             上传图片时的生成key
     * @param   ploy                策略
     * @param   splice              直接拼接图片路径时的方式
     */
    fun initialize(
        ctx: Context, onOssEntityCallBack: OnOssEntityCallBack,
        dataFetcher: suspend (String) -> InputStream,
        convert: (String)->String,
        ploy: Ploy = Ploy.DEFAULT,
        splice: ((String) -> String)? = null
    ) {
        initialize(
            ctx,
            onOssEntityCallBack::loadEntityForRemote,
            dataFetcher,
            convert,
            ploy,
            splice
        )
    }

    /**
     * @param   onOssEntityCallBack 更新ossToken
     * @param   dataFetcher         使用使用非default方法加载图片时,拉取图片的回调
     * @param   convert             上传图片时的生成key
     * @param   ploy                策略
     * @param   splice              直接拼接图片路径时的方式
     */
    fun initialize(
        ctx: Context, onLoadOssEntityForRemote: suspend () -> OssEntity,
        dataFetcher: suspend (String) -> InputStream,
        convert: (String)->String,
        ploy: Ploy = Ploy.DEFAULT,
        splice: ((String) -> String)? = null
    ) {
        this.context = ctx
        this.splice = splice
        this.ploy = ploy
        this.dataFetcher = dataFetcher
        ossEntityRemoteCallBack(onLoadOssEntityForRemote)
        prepare(convert)
    }


    /**
     * 获取 AliOssEntity
     */
    fun obtainOssEntity() = flow {
        emit(invoke().loadEntityForLocal())
    }.onEach {
        val currentTimeMillis = System.currentTimeMillis()
        if (it.timestamp + it.expires < currentTimeMillis) {
            // 过期
            throw TokenExpirationException()
        }
    }.catch {
        val value = invoke().loadEntityForRemote()
        invoke().saveResponseToLocal(value)
        emit(value)
    }

    fun obtainOssClient() = obtainOssEntity().map {
        ossClient(it)
    }

}
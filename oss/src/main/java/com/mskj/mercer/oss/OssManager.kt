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
import com.mskj.mercer.oss.model.Motion
import com.mskj.mercer.oss.model.OssEntity
import com.mskj.mercer.oss.model.Ploy
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.InputStream

@Suppress("unused")
@SuppressLint("SimpleDateFormat")
class OssManager private constructor() : OnOssEntityManager by DefaultOnEntityManagerImpl(),
    UpLoad<String> by DefaultUpLoadImpl(),
    OnDownLoad by DefaultOnDownLoadImpl() {

    private lateinit var context: Context

    private var ploy: Ploy<*> = Ploy.Default

    fun context() = context

    fun ploy() = ploy

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
        ): OSSClient = ossClient(Motion.PULL, entity, conf)

        fun ossClient(
            motion: Motion,
            entity: OssEntity,
            conf: ClientConfiguration = defaultClientConfiguration()
        ): OSSClient {
            val ossCredentialProvider = OSSStsTokenCredentialProvider(
                entity.accessKeyId,
                entity.secretKeyId,
                entity.securityToken
            )
            return OSSClient(
                invoke().context(), when (motion) {
                    Motion.PUSH -> {
                        entity.push
                    }
                    else -> {
                        entity.pull
                    }
                }, ossCredentialProvider, conf
            )
        }

        private fun defaultClientConfiguration(): ClientConfiguration {
            return ClientConfiguration().apply {
                // ?????????????????????15??????
                connectionTimeout = 15 * 1000
                // socket???????????????15??????
                socketTimeout = 15 * 1000
                // ??????????????????????????????5??????
                maxConcurrentRequest = 5
                // ????????????????????????????????????2??????
                maxErrorRetry = 2
            }
        }

    }

    /**
     * @param   ctx                         ???????????????
     * @param   onOssEntityCallBack ??????ossToken
     * @param   convert             ????????????????????????key
     * @param   ploy                ??????
     */
    fun initialize(
        ctx: Context,
        onOssEntityCallBack: OnOssEntityCallBack,
        convert: (String) -> String,
        ploy: Ploy<*> = Ploy.Default,
    ) {
        initialize(ctx, onOssEntityCallBack::loadEntityForRemote, convert, ploy)
    }

    /**
     * @param   ctx                         ???????????????
     * @param   onLoadOssEntityForRemote    ??????ossToken
     * @param   convert                     ????????????????????????key
     * @param   ploy                        ??????
     */
    fun initialize(
        ctx: Context,
        onLoadOssEntityForRemote: suspend () -> OssEntity,
        convert: (String) -> String,
        ploy: Ploy<*> = Ploy.Default,
    ) {
        this.context = ctx
        this.ploy = ploy
        ossEntityRemoteCallBack(onLoadOssEntityForRemote)
        prepare(convert)
    }

    /**
     * ?????? AliOssEntity
     */
    @Throws(Exception::class)
    suspend fun obtainOssEntity(): OssEntity {
        var entity = try {
            invoke().loadEntityForLocal()
        } catch (e: Exception) {
            null
        }
        val currentTimeMillis = System.currentTimeMillis()
        if (entity == null || entity.timestamp + entity.expires < currentTimeMillis) {
            // ???????????????????????????
            entity = invoke().loadEntityForRemote()
            invoke().saveResponseToLocal(entity)
            return entity
        }
        return entity
    }

}
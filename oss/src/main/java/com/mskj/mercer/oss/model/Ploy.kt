package com.mskj.mercer.oss.model

import com.mskj.mercer.oss.impl.DefaultOnDownLoadImpl
import java.io.InputStream

/**
 * glide加载图片时的策略
 */
sealed class Ploy<Input> {

    /**
     * 处理
     */
    abstract suspend fun process(input: Input, key: String): InputStream

    /**
     * 安全模式,推荐使用
     * 使用 oss 默认实现 下载/加载
     */
    object Default : Ploy<OssEntity>() {
        override suspend fun process(input: OssEntity, key: String): InputStream {
            return DefaultOnDownLoadImpl.defaultFetchFile(input, key, null, null)
        }
    }

    /**
     * 直接拼接路径
     * 接上
     */
    class Splice(
        private val path: String
    ) : Ploy<Unit>() {
        override suspend fun process(input: Unit, key: String): InputStream {
            return DefaultOnDownLoadImpl.okHttpFetchFile(connect(path, key))
        }

        private fun connect(path: String, key: String): String {
            var p = path
            while (p.endsWith("/")) {
                p = p.substring(0, p.length - 1)
            }
            return "$p/$key"
        }
    }

    /**
     * 动态
     * 自定义
     */
    class Dynamic(
        private val useOss: Boolean = false,
        private val block: suspend (OssEntity?, String) -> InputStream
    ) : Ploy<OssEntity?>() {
        override suspend fun process(input: OssEntity?, key: String): InputStream {
            return block(input, key)
        }
        fun useOss() = useOss
    }

}


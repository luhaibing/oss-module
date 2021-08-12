package com.mskj.mercer.oss.model

/**
 * 策略
 */
enum class Ploy {

    /**
     * 安全模式,推荐使用
     * 使用 oss 默认实现 下载/加载
     */
    DEFAULT,

    /**
     * 不使用 aliOss 的 api 加载图片 ,一般都是使用 okhttp 拉取图片
     */
    // 推荐使用 DEFAULT
    @Deprecated("recommended use DEFAULT")
    HTTP,

    /**
     * 直接拼接路径
     * 不推荐使用
     */
    @Deprecated("recommended use DEFAULT")
    SPLICE;

}
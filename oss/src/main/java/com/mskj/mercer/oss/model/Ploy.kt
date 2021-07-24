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

    OKHTTP,

    /**
     * 直接拼接路径
     * 不推荐使用
     */
    SPLICE;

}
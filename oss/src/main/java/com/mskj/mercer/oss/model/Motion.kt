package com.mskj.mercer.oss.model

sealed class Motion{

    /**
     * 上传
     */
    object PUSH: Motion()

    /**
     * 加载/下载
     */
    object PULL: Motion()

}

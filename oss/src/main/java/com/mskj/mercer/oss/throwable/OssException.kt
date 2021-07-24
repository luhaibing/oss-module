package com.mskj.mercer.oss.throwable

import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.model.OSSRequest
import java.lang.Exception

class OssException(
    // 请求的参数
    val request: OSSRequest,
    // 客户端异常
    val clientException: ClientException,
    // 服务异常
    val serviceException: ServiceException
) : Exception()
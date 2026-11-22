package com.zhixue.lite.core.network

/**
 * 服务端返回未登录/token 失效错误码时抛出，触发自动续期重试。
 */
class TokenExpiredException(message: String = "token expired") : Exception(message)

package com.zhixue.lite.core.network

/**
 * Token 失效回调：由数据层注册，触发静默重登录。
 * 网络层不依赖 data 层（避免循环依赖），通过此接口解耦。
 */
interface TokenRefreshListener {
    suspend fun onTokenExpired(): Boolean
}

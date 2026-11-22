package com.zhixue.lite.core.data.repository

import com.zhixue.lite.core.network.ApiNetworkDataSource
import com.zhixue.lite.core.network.TokenRefreshListener
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * token 失效自动续期：网络层回调 -> 静默重登录。
 * 通过 Provider 注入避免 LoginRepository <-> ApiNetworkDataSource 循环依赖。
 */
@Singleton
class TokenRefreshManager @Inject constructor(
    private val apiNetworkDataSource: ApiNetworkDataSource,
    private val loginRepository: dagger.Lazy<LoginRepository>
) : TokenRefreshListener {

    fun register() {
        apiNetworkDataSource.tokenRefreshListener = this
    }

    override suspend fun onTokenExpired(): Boolean {
        return loginRepository.get().relogin()
    }
}

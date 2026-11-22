package com.zhixue.lite.core.data.repository

interface LoginRepository {

    suspend fun login(username: String, password: String)

    /**
     * 使用已存储的凭据静默重新登录（token 失效自动续期）。
     * @return 重新登录是否成功
     */
    suspend fun relogin(): Boolean
}

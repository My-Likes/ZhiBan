package com.zhixue.lite.core.data.repository

import com.zhixue.lite.core.network.ApiNetworkDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val networkDataSource: ApiNetworkDataSource
) : LoginRepository {

    override suspend fun login(username: String, password: String) {
        val (at, userId) = networkDataSource.ssoLogin(username, password)
        val (token, userInfo, clazzInfo, childrens) = networkDataSource.casLogin(at, userId)
        val (curId) = networkDataSource.getUserInfo(token)
        val (curUserInfo, curClassInfo) = childrens
            ?.find { it.userInfo.id == curId }
            ?.let { it.userInfo to it.clazzInfo }
            ?: (userInfo to clazzInfo!!)

        userRepository.setUserInfo(
            id = curId,
            token = token,
            name = curUserInfo.name,
            loginName = curUserInfo.loginName,
            className = curClassInfo.name,
            schoolName = curUserInfo.school!!.schoolName,
            clazzId = curClassInfo.id.orEmpty()
        )
    }


    override suspend fun relogin(): Boolean {
        return runCatching {
            val (username, password) = userRepository.userData.first()
            if (username.isEmpty() || password.isEmpty()) return false
            login(username, password)
            true
        }.getOrDefault(false)
    }
}

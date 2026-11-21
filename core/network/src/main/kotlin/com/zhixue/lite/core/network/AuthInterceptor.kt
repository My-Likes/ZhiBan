package com.zhixue.lite.core.network

import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

/**
 * 智学网现行接口要求每个请求携带 auth 签名头与 XToken：
 *
 *  authguid      = 随机 UUID
 *  authtimestamp = 毫秒时间戳
 *  authtoken     = md5(authguid + authtimestamp + AUTH_SALT)
 *  authbizcode   = "0001"
 *  XToken        = 通过 /container/app/token/getToken 获取，约 600s 过期
 *
 * 缺少这些头时，报告类接口只会返回 HTTP 200 + 空 body（即“接口失效”的表象）。
 */
class AuthInterceptor : Interceptor {

    private data class CachedToken(val token: String, val fetchedAtMillis: Long)

    private val cachedToken = AtomicReference<CachedToken?>(null)

    // 独立客户端，避免 getToken 自身再次进入本拦截器造成递归
    private val bareClient = OkHttpClient()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val host = request.url.host
        // Gitee / ChangYan 不需要智学网签名头
        if (!host.endsWith("zhixue.com")) {
            return chain.proceed(request)
        }

        val authHeaders = buildAuthHeaders(withXToken = true)
        val signed = request.newBuilder()
            .header("User-Agent", USER_AGENT)
            .apply { authHeaders.forEach { (k, v) -> header(k, v) } }
            .build()

        val response = chain.proceed(signed)
        // XToken 失效：刷新一次后重试
        if (response.code == 200 && response.peekBody(1).bytes().isEmpty() && request.method == "POST") {
            response.close()
            cachedToken.set(null)
            val retryHeaders = buildAuthHeaders(withXToken = true, forceRefresh = true)
            val retry = request.newBuilder()
                .header("User-Agent", USER_AGENT)
                .apply { retryHeaders.forEach { (k, v) -> header(k, v) } }
                .build()
            return chain.proceed(retry)
        }
        return response
    }

    private fun buildAuthHeaders(withXToken: Boolean, forceRefresh: Boolean = false): Map<String, String> {
        val guid = UUID.randomUUID().toString().replace("-", "")
        val timestamp = System.currentTimeMillis().toString()
        val authToken = md5(guid + timestamp + AUTH_SALT)
        val headers = linkedMapOf(
            "authbizcode" to "0001",
            "authguid" to guid,
            "authtimestamp" to timestamp,
            "authtoken" to authToken
        )
        if (withXToken) {
            val token = obtainXToken(forceRefresh)
            if (token.isNotEmpty()) {
                headers["XToken"] = token
            }
        }
        return headers
    }

    private fun obtainXToken(forceRefresh: Boolean): String {
        if (!forceRefresh) {
            cachedToken.get()?.let { cached ->
                if (System.currentTimeMillis() - cached.fetchedAtMillis < TOKEN_TTL_MILLIS) {
                    return cached.token
                }
            }
        }
        val guid = UUID.randomUUID().toString().replace("-", "")
        val timestamp = System.currentTimeMillis().toString()
        val authToken = md5(guid + timestamp + AUTH_SALT)
        val url = "https://www.zhixue.com/container/app/token/getToken".toHttpUrl()
        val request = Request.Builder()
            .url(url)
            .header("authbizcode", "0001")
            .header("authguid", guid)
            .header("authtimestamp", timestamp)
            .header("authtoken", authToken)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()
        return try {
            bareClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return ""
                val body = response.body?.string() ?: return ""
                val json = JSONObject(body)
                if (json.optBoolean("success", false)) {
                    val token = json.optString("result", "")
                    cachedToken.set(CachedToken(token, System.currentTimeMillis()))
                    token
                } else {
                    ""
                }
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return BigInteger(1, digest).toString(16).padStart(32, '0')
    }

    private companion object {
        const val AUTH_SALT = "iflytek!@#123student"
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"
        const val TOKEN_TTL_MILLIS = 10 * 60 * 1000L
    }
}

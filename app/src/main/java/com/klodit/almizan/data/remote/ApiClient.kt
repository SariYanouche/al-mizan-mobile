/*package com.klodit.almizan.data.remote

import com.klodit.almizan.data.api.NotificationApiService
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Inet4Address
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://api.klodit.app/api/v1/"

    private val ipv4Only = object : Dns {
        override fun lookup(hostname: String): List<java.net.InetAddress> {
            return Dns.SYSTEM.lookup(hostname)
                .filter { it is Inet4Address }
                .ifEmpty { Dns.SYSTEM.lookup(hostname) }
        }
    }

    private val cookieJar = object : CookieJar {
        @Volatile
        private var accessTokenCookie: Cookie? = null

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookies.firstOrNull { it.name == "access_token" }?.let { cookie ->
                accessTokenCookie = cookie
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val cookie = accessTokenCookie
            return if (cookie != null && cookie.expiresAt > System.currentTimeMillis()) {
                listOf(cookie)
            } else {
                emptyList()
            }
        }
    }

    private fun getAccessToken(url: HttpUrl): String? {
        val cookie = cookieJar.loadForRequest(url).firstOrNull { it.name == "access_token" }
        return cookie?.value
    }

    private val httpClient = OkHttpClient.Builder()
        .dns(ipv4Only)
        .cookieJar(cookieJar)
        .addInterceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()
                .header("X-Internal-Service", "api-gateway")

            val hasAuthorization = !request.header("Authorization").isNullOrBlank()
            if (!hasAuthorization) {
                val token = getAccessToken(request.url)
                if (!token.isNullOrBlank()) {
                    builder.header("Authorization", "Bearer $token")
                }
            }

            chain.proceed(builder.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val notificationApi: NotificationApiService by lazy {
        retrofit.create(NotificationApiService::class.java)
    }
}*/

package com.klodit.almizan.data.remote

import com.klodit.almizan.data.api.NotificationApiService
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Inet4Address
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://api.klodit.app/api/v1/"

    private val ipv4Only = object : Dns {
        override fun lookup(hostname: String): List<java.net.InetAddress> {
            return Dns.SYSTEM.lookup(hostname)
                .filter { it is Inet4Address }
                .ifEmpty { Dns.SYSTEM.lookup(hostname) }
        }
    }

    private val cookieJar = object : CookieJar {
        @Volatile var accessTokenCookie: Cookie? = null
        @Volatile var refreshTokenCookie: Cookie? = null

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookies.firstOrNull { it.name == "access_token" }?.let { accessTokenCookie = it }
            cookies.firstOrNull { it.name == "refresh_token" }?.let { refreshTokenCookie = it }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val result = mutableListOf<Cookie>()
            accessTokenCookie?.takeIf { it.expiresAt > System.currentTimeMillis() }?.let { result.add(it) }
            refreshTokenCookie?.takeIf { it.expiresAt > System.currentTimeMillis() }?.let { result.add(it) }
            return result
        }
    }

    fun injectSavedToken(token: String) {
        cookieJar.accessTokenCookie = Cookie.Builder()
            .name("access_token").value(token)
            .domain("api.klodit.app").path("/")
            .expiresAt(System.currentTimeMillis() + 15 * 60 * 1000L)
            .build()
    }

    fun injectRefreshToken(token: String) {
        cookieJar.refreshTokenCookie = Cookie.Builder()
            .name("refresh_token").value(token)
            .domain("api.klodit.app").path("/api/v1/auth/refresh")
            .expiresAt(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)
            .build()
    }

    private fun getAccessToken(url: HttpUrl): String? {
        val cookie = cookieJar.loadForRequest(url).firstOrNull { it.name == "access_token" }
        return cookie?.value
    }

    private val httpClient = OkHttpClient.Builder()
        .dns(ipv4Only)
        .cookieJar(cookieJar)
        .addInterceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()
                .header("X-Internal-Service", "api-gateway")

            val hasAuthorization = !request.header("Authorization").isNullOrBlank()
            if (!hasAuthorization) {
                val token = getAccessToken(request.url)
                if (!token.isNullOrBlank()) {
                    builder.header("Authorization", "Bearer $token")
                }
            }

            chain.proceed(builder.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val notificationApi: NotificationApiService by lazy {
        retrofit.create(NotificationApiService::class.java)
    }


}
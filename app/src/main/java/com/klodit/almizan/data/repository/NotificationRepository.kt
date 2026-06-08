package com.klodit.almizan.data.repository


import com.klodit.almizan.data.api.NotificationApiService
import com.klodit.almizan.model.NotificationDto
import com.klodit.almizan.model.PaginatedNotificationsDto

class NotificationRepository(private val api: NotificationApiService) {

    suspend fun getMyNotifications(
        userId: String,
        page  : Int      = 1,
        limit : Int      = 20,
        isLue : Boolean? = null
    ): Result<PaginatedNotificationsDto> = runCatching {
        val resp = api.getMyNotifications(userId, page, limit, isLue)
        if (!resp.isSuccessful) error("HTTP ${resp.code()}: ${resp.errorBody()?.string()}")
        resp.body() ?: error("Empty response")
    }

    suspend fun getUnreadCount(userId: String): Result<Int> = runCatching {
        val resp = api.getUnreadCount(userId)
        resp.body()?.count ?: 0
    }

    suspend fun markAsRead(userId: String, id: String): Result<NotificationDto> = runCatching {
        val resp = api.markAsRead(userId, id)
        resp.body() ?: error("Empty response (${resp.code()})")
    }

    suspend fun markAllAsRead(userId: String): Result<Int> = runCatching {
        val resp = api.markAllAsRead(userId)
        resp.body()?.count ?: 0
    }
}
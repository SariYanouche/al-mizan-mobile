package com.klodit.almizan.data.api

import com.klodit.almizan.model.CountDto
import com.klodit.almizan.model.NotificationDto
import com.klodit.almizan.model.PaginatedNotificationsDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header    // ← manquant
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
interface NotificationApiService {

    @GET("/notification-service/v1/notifications/mes-notifications")
    suspend fun getMyNotifications(
        @Header("x-user-id") userId : String,
        @Query("page")       page   : Int      = 1,
        @Query("limit")      limit  : Int      = 20,
        @Query("isLue")      isLue  : Boolean? = null
    ): Response<PaginatedNotificationsDto>

    @GET("/notification-service/v1/notifications/non-lues/count")
    suspend fun getUnreadCount(
        @Header("x-user-id") userId: String
    ): Response<CountDto>

    @PATCH("/notification-service/v1/notifications/{id}/lire")
    suspend fun markAsRead(
        @Header("x-user-id") userId: String,
        @Path("id") id: String
    ): Response<NotificationDto>

    @PATCH("/notification-service/v1/notifications/marquer-toutes-lues")
    suspend fun markAllAsRead(
        @Header("x-user-id") userId: String
    ): Response<CountDto>
}
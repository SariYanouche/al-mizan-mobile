package com.klodit.almizan.model

import androidx.compose.ui.graphics.Color
import com.klodit.almizan.ui.theme.*

data class OperatorDashboardData(
    val stats: DashboardStats,
    val recentBids: List<BidShortSummary>,
    val urgentDeadlines: List<TenderDeadline>
)

data class DashboardStats(
    val activeTendersCount: Int,
    val pendingBidsCount: Int,
    val wonMarketsCount: Int,
    val activeAppealsCount: Int
)

data class BidShortSummary(
    val id: String,
    val reference: String,
    val title: String,
    val status: String,
    val lastUpdate: String
)

data class TenderDeadline(
    val id: String,
    val title: String,
    val deadlineDate: String,
    val isUrgent: Boolean
)
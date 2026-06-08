package com.klodit.almizan.ui.notifications

import android.os.Build
import androidx.annotation.RequiresApi
import com.klodit.almizan.viewmodel.notification.NotificationUiState
import com.klodit.almizan.viewmodel.notification.NotificationViewModel



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.R
import com.klodit.almizan.model.NotificationDto

import com.klodit.almizan.ui.theme.*

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ─── Category icon helper ─────────────────────────────────────────────────────

private fun categoryIcon(categorie: String) = when (categorie) {
    "PUBLICATION"  -> Icons.Outlined.Announcement
    "DEPOT"        -> Icons.Outlined.UploadFile
    "OUVERTURE"    -> Icons.Outlined.LockOpen
    "EVALUATION"   -> Icons.Outlined.Assessment
    "ATTRIBUTION"  -> Icons.Outlined.EmojiEvents
    "RECOURS"      -> Icons.Outlined.Gavel
    "SYSTEME"      -> Icons.Outlined.Settings
    "IA_DIVERGENCE",
    "IA_ERREUR"    -> Icons.Outlined.SmartToy
    else           -> Icons.Outlined.Notifications
}

private fun categoryColor(categorie: String) = when (categorie) {
    "PUBLICATION"  -> Green500
    "ATTRIBUTION"  -> Blue700
    "RECOURS"      -> Orange400
    "IA_DIVERGENCE",
    "IA_ERREUR"    -> Red600
    "EVALUATION"   -> Blue800
    else           -> Navy500
}

@RequiresApi(Build.VERSION_CODES.O)
private fun relativeTime(iso: String?): String {
    if (iso == null) return ""
    return try {
        val dt   = OffsetDateTime.parse(iso)
        val now  = OffsetDateTime.now()
        val mins = ChronoUnit.MINUTES.between(dt, now)
        when {
            mins < 1    -> "Just now"
            mins < 60   -> "${mins}m ago"
            mins < 1440 -> "${mins / 60}h ago"
            else        -> "${mins / 1440}d ago"
        }
    } catch (e: Exception) { "" }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    userId    : String = "",
    onBack    : () -> Unit = {},
    viewModel : NotificationViewModel = viewModel()
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Grey50,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.notif_title),
                            style      = MaterialTheme.typography.titleLarge,
                            color      = Navy900,
                            fontWeight = FontWeight.Bold
                        )
                        if (unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier         = Modifier
                                    .clip(CircleShape)
                                    .background(Green500)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$unreadCount",
                                    color      = NavyWhite,
                                    fontSize   = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, null, tint = Navy900)
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text(
                                stringResource(R.string.notif_mark_all_read),
                                style = MaterialTheme.typography.labelLarge,
                                color = Green600
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyWhite)
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is NotificationUiState.Loading -> {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green500)
                }
            }

            is NotificationUiState.Error -> {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ErrorOutline, null,
                            tint     = Red600,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(state.message, color = Navy500)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors  = ButtonDefaults.buttonColors(containerColor = Green500)
                        ) {
                            Text(stringResource(R.string.home_retry), color = NavyWhite)
                        }
                    }
                }
            }

            is NotificationUiState.Success -> {
                if (state.items.isEmpty()) {
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.NotificationsNone, null,
                                tint     = Navy300,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.notif_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Navy500
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier       = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(state.items, key = { it.id }) { notif ->
                            NotificationItem(
                                notif   = notif,
                                onClick = { if (!notif.isLue) viewModel.markAsRead(notif.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Single notification row ──────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun NotificationItem(
    notif   : NotificationDto,
    onClick : () -> Unit
) {
    val iconTint   = categoryColor(notif.categorie)
    val isUnread   = !notif.isLue
    val bgColor    = if (isUnread) Green50 else NavyWhite

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Category icon bubble
        Box(
            modifier         = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                categoryIcon(notif.categorie), null,
                tint     = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    notif.titre,
                    style      = MaterialTheme.typography.titleSmall,
                    color      = if (isUnread) Navy900 else Navy700,
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    relativeTime(notif.createdAt),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Navy400,
                    fontSize = 10.sp
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                notif.contenu,
                style    = MaterialTheme.typography.bodySmall,
                color    = Navy500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Unread dot
        if (isUnread) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Green500)
                    .align(Alignment.CenterVertically)
            )
        }
    }

    HorizontalDivider(color = Grey100, thickness = 0.5.dp)
}
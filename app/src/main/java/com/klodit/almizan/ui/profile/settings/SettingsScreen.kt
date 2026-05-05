package com.klodit.almizan.ui.profile.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.*
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.viewmodel.profile.ProfileViewModel

// ─────────────────────────────────────────────
//  SETTINGS SCREEN
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    localizedContext: Context,
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onChannelToggle: (NotificationChannel, Boolean) -> Unit = { _, _ -> },
    onCategoryToggle: (NotificationCategory, Boolean) -> Unit = { _, _ -> }
) {
    val auditLogs by viewModel.auditLogs.collectAsState()
    val notificationPreference by viewModel.notificationPreference.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSettingsData()
    }

    // State for notification preferences
    var channelStates by remember(notificationPreference) {
        mutableStateOf(notificationPreference.channels.toMutableMap())
    }
    var categoryStates by remember(notificationPreference) {
        mutableStateOf(notificationPreference.categories.toMutableMap())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizedContext.getString(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = NavyWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizedContext.getString(R.string.settings_back),
                            tint = NavyWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy800
                )
            )
        },
        containerColor = Navy50
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green500)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
            )
        ) {
            // Section 1: Notification Channels
            item {
                Text(
                    text = localizedContext.getString(R.string.settings_notification_channels),
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                NotificationChannelsCard(
                    localizedContext = localizedContext,
                    channelStates = channelStates,
                    onChannelToggle = { channel, enabled ->
                        channelStates = channelStates.toMutableMap().apply { put(channel, enabled) }
                        onChannelToggle(channel, enabled)
                    }
                )
            }

            // Section 2: Notification Categories
            item {
                Text(
                    text = localizedContext.getString(R.string.settings_notification_categories),
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            item {
                NotificationCategoriesCard(
                    localizedContext = localizedContext,
                    categoryStates = categoryStates,
                    onCategoryToggle = { category, enabled ->
                        categoryStates = categoryStates.toMutableMap().apply { put(category, enabled) }
                        onCategoryToggle(category, enabled)
                    }
                )
            }

            // Section 3: Activity Audit Log
            item {
                Text(
                    text = localizedContext.getString(R.string.settings_audit_log),
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            item {
                if (auditLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(localizedContext.getString(R.string.settings_no_audit_logs), color = Navy400, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    AuditLogTimeline(
                        localizedContext = localizedContext,
                        auditLogs = auditLogs
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
//  NOTIFICATION CHANNELS CARD
// ─────────────────────────────────────────────

@Composable
private fun NotificationChannelsCard(
    localizedContext: Context,
    channelStates: Map<NotificationChannel, Boolean>,
    onChannelToggle: (NotificationChannel, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NotificationChannel.entries.forEachIndexed { index, channel ->
                val isEnabled = channelStates[channel] ?: false
                val (icon, labelResId) = getChannelInfo(channel)

                ChannelToggleRow(
                    localizedContext = localizedContext,
                    icon = icon,
                    labelResId = labelResId,
                    isEnabled = isEnabled,
                    onToggle = { onChannelToggle(channel, it) }
                )

                if (index < NotificationChannel.entries.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Grey100
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelToggleRow(
    localizedContext: Context,
    icon: ImageVector,
    labelResId: Int,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isEnabled) Green50 else Grey100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEnabled) Green500 else Navy400,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = localizedContext.getString(labelResId),
            style = MaterialTheme.typography.bodyMedium,
            color = Navy900,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NavyWhite,
                checkedTrackColor = Green500,
                uncheckedThumbColor = NavyWhite,
                uncheckedTrackColor = Grey300,
                uncheckedBorderColor = Grey300
            )
        )
    }
}

private fun getChannelInfo(channel: NotificationChannel): Pair<ImageVector, Int> {
    return when (channel) {
        NotificationChannel.EMAIL -> Icons.Default.Email to R.string.settings_channel_email
        NotificationChannel.SMS -> Icons.Default.Sms to R.string.settings_channel_sms
        NotificationChannel.PUSH -> Icons.Default.Notifications to R.string.settings_channel_push
    }
}

// ─────────────────────────────────────────────
//  NOTIFICATION CATEGORIES CARD
// ─────────────────────────────────────────────

@Composable
private fun NotificationCategoriesCard(
    localizedContext: Context,
    categoryStates: Map<NotificationCategory, Boolean>,
    onCategoryToggle: (NotificationCategory, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NotificationCategory.entries.forEachIndexed { index, category ->
                val isEnabled = categoryStates[category] ?: false
                val (icon, labelResId, descResId) = getCategoryInfo(category)

                CategoryToggleRow(
                    localizedContext = localizedContext,
                    icon = icon,
                    labelResId = labelResId,
                    descResId = descResId,
                    isEnabled = isEnabled,
                    onToggle = { onCategoryToggle(category, it) }
                )

                if (index < NotificationCategory.entries.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Grey100
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryToggleRow(
    localizedContext: Context,
    icon: ImageVector,
    labelResId: Int,
    descResId: Int,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isEnabled) Green50 else Grey100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEnabled) Green500 else Navy400,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = localizedContext.getString(labelResId),
                style = MaterialTheme.typography.bodyMedium,
                color = Navy900
            )
            Text(
                text = localizedContext.getString(descResId),
                style = MaterialTheme.typography.bodySmall,
                color = Navy500
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NavyWhite,
                checkedTrackColor = Green500,
                uncheckedThumbColor = NavyWhite,
                uncheckedTrackColor = Grey300,
                uncheckedBorderColor = Grey300
            )
        )
    }
}

private fun getCategoryInfo(category: NotificationCategory): Triple<ImageVector, Int, Int> {
    return when (category) {
        NotificationCategory.PUBLICATION -> Triple(
            Icons.Default.Campaign,
            R.string.settings_cat_publication,
            R.string.settings_cat_publication_desc
        )
        NotificationCategory.EVALUATION -> Triple(
            Icons.Default.Assessment,
            R.string.settings_cat_evaluation,
            R.string.settings_cat_evaluation_desc
        )
        NotificationCategory.ATTRIBUTION -> Triple(
            Icons.Default.EmojiEvents,
            R.string.settings_cat_attribution,
            R.string.settings_cat_attribution_desc
        )
        NotificationCategory.RECOURS -> Triple(
            Icons.Default.Gavel,
            R.string.settings_cat_recours,
            R.string.settings_cat_recours_desc
        )
        NotificationCategory.SYSTEME -> Triple(
            Icons.Default.Settings,
            R.string.settings_cat_systeme,
            R.string.settings_cat_systeme_desc
        )
    }
}

// ─────────────────────────────────────────────
//  AUDIT LOG TIMELINE
// ─────────────────────────────────────────────

@Composable
private fun AuditLogTimeline(
    localizedContext: Context,
    auditLogs: List<AuditLog>
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            auditLogs.forEachIndexed { index, log ->
                val isLast = index == auditLogs.size - 1

                TimelineNode(
                    localizedContext = localizedContext,
                    auditLog = log,
                    dateFormatter = dateFormatter,
                    timeFormatter = timeFormatter,
                    isLast = isLast
                )
            }
        }
    }
}

@Composable
private fun TimelineNode(
    localizedContext: Context,
    auditLog: AuditLog,
    dateFormatter: DateTimeFormatter,
    timeFormatter: DateTimeFormatter,
    isLast: Boolean
) {
    val actionLabel = getActionLabel(localizedContext, auditLog.action)
    val actionIcon = getActionIcon(auditLog.action)

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline column (dot + line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Timeline dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Navy500),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NavyWhite)
                )
            }

            // Timeline line (not for last item)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(Grey300)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            // Action row with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = Navy700,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Navy900
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Entity
            Text(
                text = auditLog.entite,
                style = MaterialTheme.typography.bodySmall,
                color = Navy600
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Date & Time + IP Badge row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Navy400,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = auditLog.horodatage.format(dateFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = Navy500
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Navy400,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = auditLog.horodatage.format(timeFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = Navy500
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // IP Address badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Navy50
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lan,
                            contentDescription = null,
                            tint = Navy500,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = auditLog.ipAddress,
                            style = MaterialTheme.typography.labelSmall,
                            color = Navy600
                        )
                    }
                }
            }
        }
    }
}

private fun getActionLabel(context: Context, action: String): String {
    return when (action) {
        "LOGIN" -> context.getString(R.string.settings_action_login)
        "LOGOUT" -> context.getString(R.string.settings_action_logout)
        "UPLOAD_DOCUMENT" -> context.getString(R.string.settings_action_upload_doc)
        "SUBMIT_BID" -> context.getString(R.string.settings_action_submit_bid)
        "UPDATE_PROFILE" -> context.getString(R.string.settings_action_update_profile)
        "DOWNLOAD_RECEIPT" -> context.getString(R.string.settings_action_download_receipt)
        "VIEW_RESULTS" -> context.getString(R.string.settings_action_view_results)
        "FILE_APPEAL" -> context.getString(R.string.settings_action_file_appeal)
        "CHANGE_PASSWORD" -> context.getString(R.string.settings_action_change_password)
        else -> action
    }
}

private fun getActionIcon(action: String): ImageVector {
    return when (action) {
        "LOGIN" -> Icons.Default.Login
        "LOGOUT" -> Icons.Default.Logout
        "UPLOAD_DOCUMENT" -> Icons.Default.CloudUpload
        "SUBMIT_BID" -> Icons.Default.Send
        "UPDATE_PROFILE" -> Icons.Default.Person
        "DOWNLOAD_RECEIPT" -> Icons.Default.Download
        "VIEW_RESULTS" -> Icons.Default.Visibility
        "FILE_APPEAL" -> Icons.Default.Gavel
        "CHANGE_PASSWORD" -> Icons.Default.Lock
        else -> Icons.Default.Info
    }
}

// ─────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────



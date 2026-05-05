package com.klodit.almizan.ui.profile.security

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.*
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.viewmodel.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    localizedContext: Context,
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onMfaToggle: (Boolean) -> Unit = {},
    onRevokeSession: (Session) -> Unit = {},
    onLogoutAllDevices: () -> Unit = {}
) {
    val sessions by viewModel.sessions.collectAsState()
    val userSecurity by viewModel.userSecurity.collectAsState()
    val passwordLastChangedDays by viewModel.passwordLastChangedDays.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSecurityData()
    }

    var mfaEnabled by remember(userSecurity) {
        mutableStateOf(userSecurity?.mfaEnabled ?: false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizedContext.getString(R.string.security_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = NavyWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizedContext.getString(R.string.security_back),
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
            // Section 1: Password
            item {
                PasswordSection(
                    localizedContext = localizedContext,
                    passwordLastChangedDays = passwordLastChangedDays,
                    onChangePasswordClick = onChangePasswordClick
                )
            }

            // Section 2: Two-Factor Authentication
            item {
                MfaSection(
                    localizedContext = localizedContext,
                    mfaEnabled = mfaEnabled,
                    onMfaToggle = { enabled ->
                        mfaEnabled = enabled
                        onMfaToggle(enabled)
                    }
                )
            }

            // Section 3: Active Sessions Header
            item {
                Text(
                    text = localizedContext.getString(R.string.security_active_sessions),
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Session Cards
            if (sessions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(localizedContext.getString(R.string.security_no_sessions), color = Navy400, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            items(sessions) { session ->
                SessionCard(
                    localizedContext = localizedContext,
                    session = session,
                    onRevokeSession = { onRevokeSession(session) }
                )
            }

            // Logout All Devices Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onLogoutAllDevices,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red600,
                        contentColor = NavyWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizedContext.getString(R.string.security_logout_all),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PasswordSection(
    localizedContext: Context,
    passwordLastChangedDays: Int,
    onChangePasswordClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lock Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Navy50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Navy800,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localizedContext.getString(R.string.security_password_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localizedContext.getString(R.string.security_password_changed, passwordLastChangedDays),
                    style = MaterialTheme.typography.bodySmall,
                    color = Navy500
                )
            }

            // Change Password Button
            OutlinedButton(
                onClick = onChangePasswordClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Navy800
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text(
                    text = localizedContext.getString(R.string.security_change_password),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun MfaSection(
    localizedContext: Context,
    mfaEnabled: Boolean,
    onMfaToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shield Icon with status color
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (mfaEnabled) Green50 else Grey100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (mfaEnabled) Green500 else Navy400,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localizedContext.getString(R.string.security_mfa_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (mfaEnabled) Green500 else Navy400)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (mfaEnabled)
                            localizedContext.getString(R.string.security_mfa_active)
                        else
                            localizedContext.getString(R.string.security_mfa_inactive),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (mfaEnabled) Green600 else Navy500
                    )
                }
            }

            // Toggle Switch
            Switch(
                checked = mfaEnabled,
                onCheckedChange = onMfaToggle,
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
}

@Composable
private fun SessionCard(
    localizedContext: Context,
    session: Session,
    onRevokeSession: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (session.isCurrentSession) Green50 else NavyWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (session.isCurrentSession) Green500.copy(alpha = 0.15f) else Navy50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (session.userAgent.contains("Mobile", ignoreCase = true))
                        Icons.Default.PhoneAndroid
                    else
                        Icons.Default.Computer,
                    contentDescription = null,
                    tint = if (session.isCurrentSession) Green600 else Navy600,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Session Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.userAgent,
                        style = MaterialTheme.typography.titleSmall,
                        color = Navy900,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (session.isCurrentSession) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Green500.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = localizedContext.getString(R.string.security_current_device),
                                style = MaterialTheme.typography.labelSmall,
                                color = Green700,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Navy400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = session.ipAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = Navy500
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Navy400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = session.createdAt.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = Navy500
                    )
                }
            }

            // Revoke Button (only for non-current sessions)
            if (!session.isCurrentSession) {
                IconButton(
                    onClick = onRevokeSession,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Red50)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = localizedContext.getString(R.string.security_revoke_session),
                        tint = Red600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

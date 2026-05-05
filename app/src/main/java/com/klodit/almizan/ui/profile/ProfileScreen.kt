package com.klodit.almizan.ui.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.R
import com.klodit.almizan.data.profile.ProfileUiState
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    userId: String,
    token: String,
    innerPadding: PaddingValues = PaddingValues(),
    onNavigateToEdit: (profileId: String) -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeleteAccount: (profileId: String) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    val profileState by viewModel.profileUiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        Log.d("PROFILE_DEBUG", "userId=$userId token=$token")
        viewModel.fetchProfileByUserId(userId, token)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = profileState) {
            is ProfileUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.profile_loading),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            is ProfileUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchProfileByUserId(userId, token) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(stringResource(R.string.profile_retry))
                        }
                    }
                }
            }

            is ProfileUiState.Success -> {
                val profile = state.profile
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Header ─────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Navy800, Navy700)))
                            .padding(bottom = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar initials
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(Green500)
                                    .border(3.dp, NavyWhite.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = buildString {
                                        append(profile.firstName.firstOrNull()?.uppercaseChar() ?: "")
                                        append(profile.lastName.firstOrNull()?.uppercaseChar() ?: "")
                                    },
                                    color = NavyWhite,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "${profile.firstName} ${profile.lastName}",
                                color = NavyWhite,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = profile.email,
                                color = NavyWhite.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))

                            // Badges
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (profile.isVerified) {
                                    ProfileBadge(
                                        label = stringResource(R.string.topbar_verified),
                                        containerColor = Green500.copy(alpha = 0.2f),
                                        textColor = Green50,
                                        icon = Icons.Filled.Verified
                                    )
                                }
                                ProfileBadge(
                                    label = profile.tier,
                                    containerColor = Blue800.copy(alpha = 0.25f),
                                    textColor = Blue50,
                                    icon = Icons.Filled.WorkspacePremium
                                )
                            }
                        }
                    }

                    // Content pulled up over header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-20).dp)
                            .padding(horizontal = 16.dp)
                    ) {

                        // ── Organisation Info Card ─────────────────────────
                        ProfileSectionCard(title = stringResource(R.string.profile_section_org)) {
                            InfoRow(
                                icon = Icons.Outlined.Business,
                                label = stringResource(R.string.reg1_field_org_name),
                                value = profile.organizationName.ifEmpty { "—" }
                            )
                            InfoRow(
                                icon = Icons.Outlined.Badge,
                                label = stringResource(R.string.reg1_field_nif),
                                value = profile.nif.ifEmpty { "—" }
                            )
                            InfoRow(
                                icon = Icons.Outlined.Numbers,
                                label = stringResource(R.string.reg1_field_nis),
                                value = profile.nis.ifEmpty { "—" }
                            )
                            InfoRow(
                                icon = Icons.Outlined.Article,
                                label = stringResource(R.string.reg1_field_rc),
                                value = profile.rc.ifEmpty { "—" },
                                isLast = true
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── Personal Info Card ─────────────────────────────
                        ProfileSectionCard(title = stringResource(R.string.profile_section_personal)) {
                            InfoRow(
                                icon = Icons.Outlined.Person,
                                label = stringResource(R.string.profile_field_fullname),
                                value = "${profile.firstName} ${profile.lastName}"
                            )
                            InfoRow(
                                icon = Icons.Outlined.Phone,
                                label = stringResource(R.string.reg2_phone_label),
                                value = profile.phone.ifEmpty { "—" },
                                isLast = true
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── Account Actions Card ───────────────────────────
                        ProfileSectionCard(title = stringResource(R.string.profile_section_account)) {
                            ProfileMenuItem(
                                icon = Icons.Outlined.Edit,
                                label = stringResource(R.string.profile_edit_profile),
                                onClick = { onNavigateToEdit(profile.id) }
                            )
                            HorizontalDivider(
                                color = Navy100,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 52.dp)
                            )
                            ProfileMenuItem(
                                icon = Icons.Outlined.Lock,
                                label = stringResource(R.string.profile_change_password),
                                onClick = onNavigateToChangePassword
                            )
                            HorizontalDivider(
                                color = Navy100,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 52.dp)
                            )
                            ProfileMenuItem(
                                icon = Icons.Outlined.Settings,
                                label = stringResource(R.string.profile_nav_settings),
                                onClick = onNavigateToSettings,
                                isLast = true
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── Danger Zone Card ───────────────────────────────
                        ProfileSectionCard(
                            title = stringResource(R.string.profile_section_danger),
                            titleColor = Red600
                        ) {
                            ProfileMenuItem(
                                icon = Icons.Outlined.Logout,
                                label = stringResource(R.string.topbar_disconnect),
                                onClick = { showLogoutDialog = true },
                                tint = Navy700
                            )
                            HorizontalDivider(
                                color = Red50,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 52.dp)
                            )
                            ProfileMenuItem(
                                icon = Icons.Outlined.DeleteForever,
                                label = stringResource(R.string.profile_delete_account),
                                onClick = { onNavigateToDeleteAccount(profile.id) },
                                tint = Red600,
                                isLast = true
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.sovereign_footer),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 9.sp
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            else -> {}
        }

        // ── Logout Dialog ──────────────────────────────────────────────────
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Icon(Icons.Outlined.Logout, contentDescription = null, tint = Navy800)
                },
                title = {
                    Text(
                        stringResource(R.string.profile_logout_confirm_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Navy900
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.profile_logout_confirm_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Navy700
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showLogoutDialog = false; onLogout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800)
                    ) {
                        Text(stringResource(R.string.profile_logout_confirm_btn), color = NavyWhite)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text(stringResource(R.string.profile_cancel), color = Navy500)
                    }
                },
                containerColor = NavyWhite,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
//  REUSABLE COMPOSABLES
// ─────────────────────────────────────────────

@Composable
private fun ProfileBadge(
    label: String,
    containerColor: Color,
    textColor: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
        Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    titleColor: Color = Navy800,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = titleColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Navy500, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Navy500,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Navy900,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (!isLast) {
            HorizontalDivider(
                color = Navy100,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 48.dp)
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Navy800,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(tint.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Navy300,
            modifier = Modifier.size(18.dp)
        )
    }
}
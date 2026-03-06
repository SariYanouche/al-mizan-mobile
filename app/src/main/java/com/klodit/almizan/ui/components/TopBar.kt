package com.klodit.almizan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Verified
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
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.*

@Composable
fun TopBar(
    userFirstName        : String      = "",
    userLastName         : String      = "",
    isVerified           : Boolean     = false,
    tier                 : String      = "OUVERT",
    unreadCount          : Int         = 0,
    selectedLang         : AppLanguage = AppLanguage.FRENCH,
    onLanguageChange     : (AppLanguage) -> Unit = {},
    onNotificationsClick : () -> Unit  = {},
    onLogoutClick        : () -> Unit  = {}
) {
    val displayName = listOf(userFirstName, userLastName)
        .filter { it.isNotEmpty() }.joinToString(" ")
        .ifEmpty { stringResource(R.string.tab_profile) }

    // Controls visibility of the language dropdown
    var showLangMenu by remember { mutableStateOf(false) }

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        color           = Navy800,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── LEFT: Logo + App name ─────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.weight(1f)
            ) {
                Box(
                    modifier         = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Green500),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", color = NavyWhite, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text          = stringResource(R.string.app_name),
                        color         = NavyWhite,
                        fontWeight    = FontWeight.ExtraBold,
                        fontSize      = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isVerified) {
                            Icon(
                                Icons.Outlined.Verified, null,
                                tint     = Green400,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                        Text(
                            tier,
                            color         = NavyWhite.copy(alpha = 0.55f),
                            fontSize      = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // ── RIGHT: name + language + notifications + logout ───────────
            Text(
                text      = displayName,
                color     = NavyWhite.copy(alpha = 0.85f),
                fontSize  = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
                modifier  = Modifier.widthIn(max = 80.dp)
            )
            Spacer(Modifier.width(2.dp))

            // ── Language switcher ─────────────────────────────────────────
            Box {
                IconButton(
                    onClick  = { showLangMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Language, null,
                        tint     = NavyWhite.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded        = showLangMenu,
                    onDismissRequest = { showLangMenu = false }
                ) {
                    AppLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (lang == selectedLang) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Green500)
                                        )
                                    } else {
                                        Spacer(Modifier.size(8.dp))
                                    }
                                    Text(
                                        lang.label,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        color      = if (lang == selectedLang) Green600
                                        else Navy900,
                                        fontWeight = if (lang == selectedLang) FontWeight.Bold
                                        else FontWeight.Normal
                                    )
                                }
                            },
                            onClick = {
                                showLangMenu = false
                                onLanguageChange(lang)
                            }
                        )
                    }
                }
            }

            // ── Notifications ─────────────────────────────────────────────
            Box {
                IconButton(
                    onClick  = onNotificationsClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Notifications, null,
                        tint     = NavyWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (unreadCount > 0) {
                    Box(
                        modifier         = Modifier
                            .size(14.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                            .background(Red600, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (unreadCount > 9) "9+" else unreadCount.toString(),
                            color      = NavyWhite,
                            fontSize   = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Logout ────────────────────────────────────────────────────
            IconButton(
                onClick  = onLogoutClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Outlined.Logout, null,
                    tint     = NavyWhite.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
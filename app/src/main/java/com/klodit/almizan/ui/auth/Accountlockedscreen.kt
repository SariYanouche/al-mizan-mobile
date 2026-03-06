package com.klodit.almizan.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.AppLanguage
import com.klodit.almizan.ui.theme.Red50
import com.klodit.almizan.ui.theme.Red600
import com.klodit.almizan.ui.theme.RedNotice

@Composable
fun AccountLockedScreen(
    lockDurationSeconds : Int = 300,
    onResetPasswordClick: () -> Unit = {},
    onContactSupport    : () -> Unit = {},
    onTimerExpired      : () -> Unit = {},   // called when countdown reaches 0
    selectedLang        : AppLanguage = AppLanguage.FRENCH,
    onLanguageChange    : (AppLanguage) -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme

    var secondsLeft  by remember { mutableIntStateOf(lockDurationSeconds) }
    val timerExpired = secondsLeft == 0

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        // timer done — notify parent so it can pop back to login
        onTimerExpired()
    }

    val timerText = "%02d:%02d".format(secondsLeft / 60, secondsLeft % 60)

    val screenWidth   = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth     = if (screenWidth < 500.dp) screenWidth * 0.90f else 420.dp
    val overlapAmount = 32.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.primary)
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = overlapAmount + 24.dp,
                    start = 16.dp, end = 16.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Image(painterResource(R.drawable.logo), "Logo",
                    modifier = Modifier.size(44.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("AL-MIZAN", fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = cs.onPrimary, letterSpacing = 1.sp)
                    Text(stringResource(R.string.app_tagline), fontSize = 9.sp,
                        color = cs.secondary, letterSpacing = 1.sp)
                }
            }
        }

        // ── Card ─────────────────────────────────────────────────────────────
        Card(
            modifier  = Modifier.width(cardWidth).offset(y = -overlapAmount).zIndex(1f),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = cs.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier         = Modifier.size(72.dp).clip(CircleShape).background(Red50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Lock, null, tint = Red600,
                        modifier = Modifier.size(36.dp))
                }

                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.al_title),
                    style     = MaterialTheme.typography.headlineMedium,
                    color     = cs.onSurface,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.al_subtitle),
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center)

                Spacer(Modifier.height(24.dp))

                // ── Countdown ─────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (timerExpired) cs.secondaryContainer else RedNotice)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.Info, null,
                        tint     = if (timerExpired) cs.secondary else Red600,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (timerExpired)
                            "Vous pouvez réessayer maintenant"
                        else
                            "${stringResource(R.string.al_try_again)}  $timerText",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (timerExpired) cs.secondary else Red600
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Reset password button ─────────────────────────────────────
                Button(
                    onClick  = onResetPasswordClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = cs.onSurface)
                ) {
                    Icon(Icons.Outlined.Email, null, tint = cs.surface,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.al_reset_btn),
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = cs.surface)
                }

                Spacer(Modifier.height(14.dp))

                // ── Try again button (visible only when timer expires) ─────────
                if (timerExpired) {
                    Button(
                        onClick  = onTimerExpired,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = cs.secondary)
                    ) {
                        Text("Réessayer de se connecter",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = cs.onSecondary)
                    }
                    Spacer(Modifier.height(14.dp))
                }

                // ── Contact support ───────────────────────────────────────────
                Row(
                    modifier              = Modifier.clickable { onContactSupport() },
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.Info, null,
                        tint     = cs.onSurfaceVariant,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.al_contact_support),
                        fontSize   = 14.sp,
                        color      = cs.onSurface,
                        fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        LanguageSwitcher(selectedLang, onLanguageChange)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.footer_ministry),
            fontSize      = 9.sp,
            color         = cs.onSurfaceVariant,
            letterSpacing = 1.5.sp,
            textAlign     = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
    }
}
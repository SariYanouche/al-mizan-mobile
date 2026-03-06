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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
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
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.AppLanguage
import com.klodit.almizan.ui.theme.Blue50
import com.klodit.almizan.ui.theme.Blue700
import com.klodit.almizan.viewmodel.auth.AuthState

@Composable
fun ForgotPasswordScreen(
    // Matches the pattern used by LoginScreen in your NavGraph
    authState       : AuthState = AuthState.Idle,
    onClearError    : () -> Unit = {},
    onSendClick     : (email: String) -> Unit = {},   // NavGraph calls forgotPassword here
    onBackClick     : () -> Unit = {},
    onSignInClick   : () -> Unit = {},
    selectedLang    : AppLanguage = AppLanguage.FRENCH,
    onLanguageChange: (AppLanguage) -> Unit = {}
) {
    var email        by remember { mutableStateOf("") }
    var emailTouched by remember { mutableStateOf(false) }

    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val showError  = emailTouched && email.isNotEmpty() && !emailValid
    val isLoading  = authState is AuthState.Loading

    val cs            = MaterialTheme.colorScheme
    val screenWidth   = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth     = if (screenWidth < 500.dp) screenWidth * 0.90f else 420.dp
    val overlapAmount = 32.dp

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on error then clear so it doesn't re-trigger on recomposition
    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            snackbarHostState.showSnackbar(authState.message)
            onClearError()
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = cs.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(cs.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.primary)
                    .statusBarsPadding()
                    .padding(top = 8.dp, bottom = overlapAmount + 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint     = cs.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Image(
                        painterResource(R.drawable.logo), "Logo",
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "AL-MIZAN",
                            fontSize      = 18.sp,
                            fontWeight    = FontWeight.ExtraBold,
                            color         = cs.onPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            stringResource(R.string.app_tagline),
                            fontSize      = 9.sp,
                            color         = cs.secondary,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // ── Card ─────────────────────────────────────────────────────────
            Card(
                modifier  = Modifier
                    .width(cardWidth)
                    .offset(y = -overlapAmount)
                    .zIndex(1f),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = cs.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier         = Modifier.size(64.dp).clip(CircleShape).background(Blue50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Lock, null, tint = Blue700, modifier = Modifier.size(32.dp))
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        stringResource(R.string.fp_title),
                        style     = MaterialTheme.typography.headlineMedium,
                        color     = cs.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.fp_subtitle),
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = cs.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(28.dp))

                    AuthFieldLabel(stringResource(R.string.fp_email_label))
                    Spacer(Modifier.height(6.dp))

                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it; emailTouched = true },
                        placeholder   = {
                            Text(stringResource(R.string.fp_email_placeholder),
                                color = cs.onSurfaceVariant, fontSize = 13.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, null,
                                tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        },
                        isError        = showError,
                        supportingText = if (showError) {
                            { Text(stringResource(R.string.err_email_invalid), color = cs.error, fontSize = 11.sp) }
                        } else null,
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape      = RoundedCornerShape(8.dp),
                        colors     = authFieldColors()
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        // NavGraph's onSendClick lambda calls authViewModel.forgotPassword(email) { navigate() }
                        onClick  = { onSendClick(email) },
                        enabled  = emailValid && !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor         = cs.secondary,
                            disabledContainerColor = cs.secondaryContainer
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp), color = cs.onSecondary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Email, null,
                                tint = cs.onSecondary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.fp_send_btn),
                                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSecondary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.fp_remembered), fontSize = 13.sp, color = cs.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.fp_sign_in),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = cs.onSurface,
                    modifier   = Modifier.clickable { onSignInClick() }
                )
            }

            Spacer(Modifier.height(16.dp))
            LanguageSwitcher(selectedLang, onLanguageChange)
            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(R.string.footer_ministry),
                fontSize      = 9.sp,
                color         = cs.onSurfaceVariant,
                letterSpacing = 1.5.sp,
                textAlign     = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
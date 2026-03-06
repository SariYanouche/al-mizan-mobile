package com.klodit.almizan.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.AppLanguage
import com.klodit.almizan.ui.theme.Blue50
import com.klodit.almizan.ui.theme.Blue700
import com.klodit.almizan.ui.theme.Grey200
import com.klodit.almizan.ui.theme.Orange400
import com.klodit.almizan.viewmodel.auth.AuthState

private fun passwordStrength(password: String): Int = when {
    password.length < 6  -> 0
    password.length < 10 -> 1
    password.any { it.isDigit() } && password.any { it.isUpperCase() } -> 3
    else -> 2
}

@Composable
fun SetNewPasswordScreen(
    onSaveClick     : (code: String, newPassword: String) -> Unit = { _, _ -> },
    onBackClick     : () -> Unit = {},
    selectedLang    : AppLanguage = AppLanguage.FRENCH,
    onLanguageChange: (AppLanguage) -> Unit = {},
    authState       : AuthState = AuthState.Idle,
    onClearError    : () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme

    val digits          = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    var focusedIndex    by remember { mutableIntStateOf(0) }

    var newPassword        by remember { mutableStateOf("") }
    var confirmPassword    by remember { mutableStateOf("") }
    var newPassVisible     by remember { mutableStateOf(false) }
    var confirmPassVisible by remember { mutableStateOf(false) }
    var confirmTouched     by remember { mutableStateOf(false) }

    val fullCode      = digits.joinToString("")
    val codeComplete  = digits.all { it.isNotEmpty() }
    val strength      = passwordStrength(newPassword)
    val strengthLabel = when (strength) {
        1    -> stringResource(R.string.password_weak)
        2    -> stringResource(R.string.password_medium)
        3    -> stringResource(R.string.password_strong)
        else -> ""
    }
    val strengthColor = when (strength) {
        1    -> cs.error
        2    -> Orange400
        3    -> cs.secondary
        else -> cs.outline
    }
    val passwordsMatch = newPassword == confirmPassword
    val showMismatch   = confirmTouched && confirmPassword.isNotEmpty() && !passwordsMatch
    val isLoading      = authState is AuthState.Loading
    val canSave        = codeComplete && newPassword.length >= 6 && passwordsMatch
            && confirmPassword.isNotEmpty() && !isLoading

    val screenWidth   = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth     = if (screenWidth < 500.dp) screenWidth * 0.90f else 420.dp
    val overlapAmount = 32.dp

    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage      = (authState as? AuthState.Error)?.message
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
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
                        Icon(
                            Icons.Outlined.Lock, null,
                            tint     = Blue700,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        stringResource(R.string.snp_title),
                        style     = MaterialTheme.typography.headlineMedium,
                        color     = cs.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.snp_subtitle),
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = cs.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(28.dp))

                    // ── Recovery code boxes ───────────────────────────────────
                    AuthFieldLabel(stringResource(R.string.snp_recovery_label))
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        digits.forEachIndexed { index, digit ->
                            val isFocused = focusedIndex == index
                            BasicTextField(
                                value         = digit,
                                onValueChange = { newVal ->
                                    val clean = newVal.filter { it.isDigit() }.take(1)
                                    digits[index] = clean
                                    if (clean.isNotEmpty() && index < 5) {
                                        focusedIndex = index + 1
                                        focusRequesters[index + 1].requestFocus()
                                    }
                                    if (clean.isEmpty() && index > 0) {
                                        focusedIndex = index - 1
                                        focusRequesters[index - 1].requestFocus()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isFocused) cs.surface else cs.surfaceVariant)
                                    .border(
                                        width = if (isFocused) 1.5.dp else 1.dp,
                                        color = if (isFocused) cs.secondary else cs.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .focusRequester(focusRequesters[index])
                                    .onFocusChanged { if (it.isFocused) focusedIndex = index },
                                textStyle = TextStyle(
                                    fontSize   = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = cs.onSurface,
                                    textAlign  = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                cursorBrush     = SolidColor(cs.secondary),
                                singleLine      = true,
                                decorationBox   = { inner ->
                                    Box(contentAlignment = Alignment.Center) { inner() }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── New password ──────────────────────────────────────────
                    AuthFieldLabel(stringResource(R.string.snp_new_pass_label))
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value         = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder   = {
                            Text(
                                stringResource(R.string.snp_new_pass_ph),
                                color    = cs.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Lock, null,
                                tint     = cs.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            TextButton(
                                onClick        = { newPassVisible = !newPassVisible },
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Text(
                                    if (newPassVisible)
                                        stringResource(R.string.password_hide)
                                    else
                                        stringResource(R.string.password_show),
                                    fontSize   = 11.sp,
                                    color      = cs.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        visualTransformation = if (newPassVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape      = RoundedCornerShape(8.dp),
                        colors     = authFieldColors()
                    )

                    // Strength bar
                    if (newPassword.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            repeat(3) { i ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f).height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (i < strength) strengthColor else Grey200)
                                )
                            }
                            Text(
                                strengthLabel,
                                fontSize   = 10.sp,
                                color      = strengthColor,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.width(56.dp),
                                textAlign  = TextAlign.End
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Confirm password ──────────────────────────────────────
                    AuthFieldLabel(stringResource(R.string.snp_confirm_label))
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value         = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmTouched = true },
                        placeholder   = {
                            Text(
                                stringResource(R.string.snp_confirm_ph),
                                color    = cs.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Lock, null,
                                tint     = cs.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            TextButton(
                                onClick        = { confirmPassVisible = !confirmPassVisible },
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Text(
                                    if (confirmPassVisible)
                                        stringResource(R.string.password_hide)
                                    else
                                        stringResource(R.string.password_show),
                                    fontSize   = 11.sp,
                                    color      = cs.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        visualTransformation = if (confirmPassVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        isError        = showMismatch,
                        supportingText = if (showMismatch) {
                            {
                                Text(
                                    stringResource(R.string.snp_password_mismatch),
                                    color    = cs.error,
                                    fontSize = 11.sp
                                )
                            }
                        } else null,
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape      = RoundedCornerShape(8.dp),
                        colors     = authFieldColors()
                    )

                    Spacer(Modifier.height(24.dp))

                    // ── Save button ───────────────────────────────────────────
                    Button(
                        onClick  = { onSaveClick(fullCode, newPassword) },
                        enabled  = canSave,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor         = cs.secondary,
                            disabledContainerColor = cs.secondaryContainer
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                color       = cs.onSecondary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Check, null,
                                tint     = cs.onSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.snp_save_btn),
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = cs.onSecondary
                            )
                        }
                    }
                }
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

    LaunchedEffect(Unit) { focusRequesters[0].requestFocus() }
}
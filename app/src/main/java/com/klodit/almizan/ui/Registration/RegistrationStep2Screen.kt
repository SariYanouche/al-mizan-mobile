package com.klodit.almizan.ui.Registration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.klodit.almizan.R
import com.klodit.almizan.ui.auth.AuthFieldLabel
import com.klodit.almizan.ui.auth.LanguageSwitcher
import com.klodit.almizan.ui.auth.authFieldColors
import com.klodit.almizan.ui.theme.AppLanguage
import com.klodit.almizan.ui.theme.Grey200
import com.klodit.almizan.ui.theme.Orange400

private fun isValidAlgerianPhone(phone: String): Boolean {
    val cleaned = phone.replace(" ", "").replace("-", "")
    return cleaned.matches(Regex("^(0[5-7][0-9]{8})$")) ||
            cleaned.matches(Regex("^(\\+213[5-7][0-9]{8})$"))
}

private fun isValidEmail(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

private fun passwordStrength(password: String): Int {
    if (password.isEmpty()) return 0
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return score
}

@Composable
fun RegistrationStep2Screen(
    onContinueClick: (phone: String, email: String, password: String, nom: String, prenom: String) -> Unit = { _, _, _, _, _ -> },
    onBackClick     : () -> Unit = {},
    onInfoClick     : () -> Unit = {},
    selectedLang    : AppLanguage = AppLanguage.FRENCH,
    onLanguageChange: (AppLanguage) -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme

    var phone           by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var nom    by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var phoneTouched    by remember { mutableStateOf(false) }
    var emailTouched    by remember { mutableStateOf(false) }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    val phoneValid     = isValidAlgerianPhone(phone)
    val emailValid     = isValidEmail(email)
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword
    val canContinue = phoneValid && emailValid && password.length >= 6
            && passwordsMatch && nom.isNotBlank() && prenom.isNotBlank()

    val strength      = passwordStrength(password)
    val strengthLabel = when (strength) {
        1    -> stringResource(R.string.password_weak)
        2    -> stringResource(R.string.password_medium)
        3    -> stringResource(R.string.password_strong)
        else -> ""
    }
    val strengthColor = when (strength) {
        1 -> cs.error; 2 -> Orange400; 3 -> cs.secondary; else -> cs.outline
    }

    val screenWidth   = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth     = if (screenWidth < 500.dp) screenWidth * 0.90f else 420.dp
    val overlapAmount = 32.dp

    Column(modifier = Modifier.fillMaxSize().background(cs.background)) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally) {

            // header
            Box(modifier = Modifier.fillMaxWidth().background(cs.primary)
                .statusBarsPadding().padding(bottom = overlapAmount + 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = cs.onPrimary, modifier = Modifier.size(24.dp))
                    }
                    Text("Registration", fontSize = 17.sp,
                        fontWeight = FontWeight.Bold, color = cs.onPrimary)
                    IconButton(onClick = onInfoClick) {
                        Icon(Icons.Outlined.Info, "Info",
                            tint = cs.onPrimary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // step progress
            Card(modifier = Modifier.width(cardWidth).offset(y = -overlapAmount).zIndex(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.reg2_step_label),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = cs.secondary, letterSpacing = 1.sp)
                        Text("66%", fontSize = 11.sp, color = cs.onSurfaceVariant,
                            fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(5.dp)
                        .clip(RoundedCornerShape(3.dp)).background(Grey200)) {
                        Box(modifier = Modifier.fillMaxWidth(0.66f).fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp)).background(cs.secondary))
                    }
                }
            }

            Spacer(Modifier.height((-overlapAmount.value + 8).dp))

            Column(modifier = Modifier.width(cardWidth)) {
                Text(stringResource(R.string.reg2_section_title),
                    style = MaterialTheme.typography.headlineSmall, color = cs.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.reg2_section_sub),
                    fontSize = 13.sp, color = cs.secondary)
                Spacer(Modifier.height(24.dp))


                // Last name
                AuthFieldLabel(stringResource(R.string.reg2_nom_label))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = nom,
                    onValueChange = { nom = it },
                    placeholder   = { Text(stringResource(R.string.reg2_ph_nom), color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(8.dp),
                    colors        = authFieldColors()
                )

                Spacer(Modifier.height(16.dp))

                // First name
                AuthFieldLabel( stringResource(R.string.reg2_prenom_label))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = prenom,
                    onValueChange = { prenom = it },
                    placeholder   = { Text(stringResource(R.string.reg2_ph_prenom), color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(8.dp),
                    colors        = authFieldColors()
                )

                Spacer(Modifier.height(16.dp))
                // phone
                AuthFieldLabel(stringResource(R.string.reg2_phone_label))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = phone,
                    onValueChange = { phone = it; phoneTouched = true },
                    placeholder   = { Text(stringResource(R.string.reg2_ph_phone),
                        color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    leadingIcon   = { Icon(Icons.Outlined.Phone, null,
                        tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                    isError       = phoneTouched && phone.isNotEmpty() && !phoneValid,
                    supportingText = {
                        if (phoneTouched && phone.isNotEmpty() && !phoneValid)
                            Text(stringResource(R.string.err_phone_invalid),
                                fontSize = 10.sp, color = cs.error)
                    },
                    modifier        = Modifier.fillMaxWidth(),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape           = RoundedCornerShape(8.dp),
                    colors          = authFieldColors()
                )

                Spacer(Modifier.height(16.dp))

                // email
                AuthFieldLabel(stringResource(R.string.reg2_email_label))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it; emailTouched = true },
                    placeholder   = { Text(stringResource(R.string.reg2_ph_email),
                        color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    leadingIcon   = { Icon(Icons.Outlined.Email, null,
                        tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                    isError       = emailTouched && email.isNotEmpty() && !emailValid,
                    supportingText = {
                        if (emailTouched && email.isNotEmpty() && !emailValid)
                            Text(stringResource(R.string.err_email_invalid),
                                fontSize = 10.sp, color = cs.error)
                    },
                    modifier        = Modifier.fillMaxWidth(),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape           = RoundedCornerShape(8.dp),
                    colors          = authFieldColors()
                )

                Spacer(Modifier.height(16.dp))

                // password
                AuthFieldLabel(stringResource(R.string.reg2_password_label))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = password,
                    onValueChange = { password = it },
                    placeholder   = { Text("••••••••", color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    leadingIcon   = { Icon(Icons.Outlined.Lock, null,
                        tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                    trailingIcon  = {
                        TextButton(onClick = { passwordVisible = !passwordVisible },
                            contentPadding = PaddingValues(horizontal = 10.dp)) {
                            Text(if (passwordVisible) "Masquer" else "Afficher",
                                fontSize = 11.sp, color = cs.secondary, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(8.dp),
                    colors     = authFieldColors()
                )

                if (password.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        repeat(3) { i ->
                            Box(modifier = Modifier.weight(1f).height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i < strength) strengthColor else Grey200))
                        }
                        Text(strengthLabel, fontSize = 10.sp, color = strengthColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // confirm password
                AuthFieldLabel(stringResource(R.string.reg2_confirm_label))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder   = { Text("••••••••", color = cs.onSurfaceVariant, fontSize = 13.sp) },
                    leadingIcon   = { Icon(Icons.Outlined.Lock, null,
                        tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                    trailingIcon  = {
                        TextButton(onClick = { confirmVisible = !confirmVisible },
                            contentPadding = PaddingValues(horizontal = 10.dp)) {
                            Text(if (confirmVisible) "Masquer" else "Afficher",
                                fontSize = 11.sp, color = cs.secondary, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    visualTransformation = if (confirmVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    isError  = confirmPassword.isNotEmpty() && !passwordsMatch,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(8.dp),
                    colors     = authFieldColors()
                )

                if (confirmPassword.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (passwordsMatch) Icons.Outlined.Check
                            else Icons.Outlined.Close,
                            contentDescription = null,
                            tint = if (passwordsMatch) cs.secondary else cs.error,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (passwordsMatch) stringResource(R.string.passwords_match)
                            else stringResource(R.string.err_passwords_no_match),
                            fontSize = 11.sp,
                            color = if (passwordsMatch) cs.secondary else cs.error)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // admin info box
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(cs.tertiaryContainer).padding(14.dp),
                    verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Lock, null, tint = cs.tertiary,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(stringResource(R.string.reg2_admin_title),
                            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                        Spacer(Modifier.height(3.dp))
                        Text(stringResource(R.string.reg2_admin_body),
                            fontSize = 12.sp, color = cs.onSurfaceVariant, lineHeight = 17.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
                LanguageSwitcher(selectedLang, onLanguageChange)
                Spacer(Modifier.height(24.dp))
            }
        }

        // bottom bar
        Column(modifier = Modifier.fillMaxWidth().background(cs.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Outlined.CheckCircle, null, tint = cs.secondary,
                    modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
                Text(stringResource(R.string.reg2_identity),
                    fontSize = 9.sp, color = cs.onSurfaceVariant,
                    letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = { onContinueClick(phone, email, password, nom, prenom) },
                enabled = canContinue,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = cs.secondary,
                    disabledContainerColor = cs.secondaryContainer)) {
                Text(stringResource(R.string.reg2_continue_btn),
                    fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = cs.onSecondary)
            }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.reg2_back_label),
                fontSize = 13.sp, color = cs.onSurfaceVariant,
                modifier = Modifier.clickable { onBackClick() })
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.footer_ministry),
                fontSize = 9.sp, color = cs.onSurfaceVariant,
                letterSpacing = 0.5.sp, textAlign = TextAlign.Center)
        }
    }
}
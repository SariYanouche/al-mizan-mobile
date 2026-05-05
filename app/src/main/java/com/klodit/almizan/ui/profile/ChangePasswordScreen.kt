package com.klodit.almizan.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.R
import com.klodit.almizan.data.repository.ProfileRepository
import com.klodit.almizan.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    token: String,
    onBack: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrent by remember { mutableStateOf(false) }
    var showNew     by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var currentError by remember { mutableStateOf(false) }
    var newError     by remember { mutableStateOf(false) }
    var confirmError by remember { mutableStateOf(false) }
    var matchError   by remember { mutableStateOf(false) }

    var isLoading   by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var apiError    by remember { mutableStateOf<String?>(null) }

    // Use a coroutine scope tied to this composable
    val scope = rememberCoroutineScope()
    val repository = remember { ProfileRepository() }

    val strength = remember(newPassword) {
        when {
            newPassword.length < 6 -> 0
            newPassword.length < 10 && (newPassword.any { it.isDigit() } || newPassword.any { !it.isLetterOrDigit() }) -> 1
            newPassword.length >= 10 && newPassword.any { it.isDigit() } && newPassword.any { it.isUpperCase() } -> 3
            else -> 2
        }
    }
    val strengthLabel = when (strength) {
        1    -> Triple(stringResource(R.string.password_weak),   Red600,    0.33f)
        2    -> Triple(stringResource(R.string.password_medium), Orange400, 0.66f)
        3    -> Triple(stringResource(R.string.password_strong), Green500,  1f)
        else -> Triple("", Navy100, 0f)
    }

    fun validate(): Boolean {
        currentError = currentPassword.isBlank()
        newError     = newPassword.length < 8
        matchError   = newPassword != confirmPassword
        confirmError = confirmPassword.isBlank()
        return !currentError && !newError && !matchError && !confirmError
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_change_password),
                        style = MaterialTheme.typography.titleMedium,
                        color = NavyWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = NavyWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Security notice
            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blue50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = Blue800, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.profile_password_notice), style = MaterialTheme.typography.bodySmall, color = Blue800)
                }
            }

            // Current password
            PasswordField(
                label         = stringResource(R.string.profile_current_password),
                value         = currentPassword,
                onValueChange = { currentPassword = it; currentError = false },
                showPassword  = showCurrent,
                onToggleShow  = { showCurrent = !showCurrent },
                isError       = currentError,
                errorMessage  = stringResource(R.string.profile_field_required)
            )

            HorizontalDivider(color = Navy100, thickness = 0.5.dp)

            // New password
            PasswordField(
                label         = stringResource(R.string.snp_new_pass_label),
                value         = newPassword,
                onValueChange = { newPassword = it; newError = false; matchError = false },
                showPassword  = showNew,
                onToggleShow  = { showNew = !showNew },
                isError       = newError,
                errorMessage  = stringResource(R.string.snp_new_pass_ph),
                placeholder   = stringResource(R.string.snp_new_pass_ph)
            )

            // Strength bar
            if (newPassword.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress    = { strengthLabel.third },
                        modifier    = Modifier.fillMaxWidth().height(4.dp).padding(horizontal = 2.dp),
                        color       = strengthLabel.second,
                        trackColor  = Navy100
                    )
                    Text(strengthLabel.first, color = strengthLabel.second, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Confirm password
            PasswordField(
                label         = stringResource(R.string.snp_confirm_label),
                value         = confirmPassword,
                onValueChange = { confirmPassword = it; confirmError = false; matchError = false },
                showPassword  = showConfirm,
                onToggleShow  = { showConfirm = !showConfirm },
                isError       = confirmError || matchError,
                errorMessage  = if (matchError)
                    stringResource(R.string.err_passwords_no_match)
                else
                    stringResource(R.string.profile_field_required),
                placeholder   = stringResource(R.string.snp_confirm_ph)
            )

            // Match indicator
            if (confirmPassword.isNotEmpty() && !matchError) {
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Green500, modifier = Modifier.size(14.dp))
                    Text(stringResource(R.string.passwords_match), color = Green500, fontSize = 11.sp)
                }
            }

            // API error
            apiError?.let {
                Text(
                    it,
                    color    = Red600,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .background(Red50, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .fillMaxWidth()
                )
            }

            // Success banner
            if (showSuccess) {
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Green50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier              = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Green500, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.profile_password_changed), style = MaterialTheme.typography.bodySmall, color = Green700)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Save button — now calls the real API
            Button(
                onClick = {
                    apiError    = null
                    showSuccess = false
                    if (validate()) {
                        isLoading = true
                        scope.launch {
                            repository.changePassword(currentPassword, newPassword)
                                .onSuccess {
                                    isLoading       = false
                                    showSuccess     = true
                                    currentPassword = ""
                                    newPassword     = ""
                                    confirmPassword = ""
                                }
                                .onFailure { e ->
                                    isLoading = false
                                    apiError  = e.localizedMessage ?: "Erreur réseau"
                                }
                        }
                    }
                },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Navy800)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NavyWhite, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = NavyWhite, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_save_btn), color = NavyWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    showPassword: Boolean,
    onToggleShow: () -> Unit,
    isError: Boolean = false,
    errorMessage: String = "",
    placeholder: String = ""
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style        = MaterialTheme.typography.labelSmall,
            color        = if (isError) Red600 else Navy500,
            fontWeight   = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = { Text(placeholder.ifEmpty { label }, color = Navy300) },
            isError       = isError,
            singleLine    = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = if (isError) Red600 else Navy500, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                IconButton(onClick = onToggleShow) {
                    Icon(
                        if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint   = Navy500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            shape  = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Navy800,
                unfocusedBorderColor = Navy100,
                errorBorderColor     = Red600,
                focusedContainerColor   = Navy30,
                unfocusedContainerColor = Navy30
            )
        )
        if (isError && errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Red600, style = MaterialTheme.typography.labelSmall)
        }
    }
}
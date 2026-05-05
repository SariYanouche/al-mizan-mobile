package com.klodit.almizan.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.R
import com.klodit.almizan.data.profile.ProfileUiState      // FIX: correct package
import com.klodit.almizan.data.profile.UpdateProfileRequest
import com.klodit.almizan.data.profile.UpdateUiState       // FIX: correct package
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profileId: String,
    userId: String,
    token: String,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    // FIX: was viewModel.profileState / viewModel.updateState
    val profileState by viewModel.profileUiState.collectAsState()
    val updateState  by viewModel.updateUiState.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    var phone     by remember { mutableStateOf("") }

    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError  by remember { mutableStateOf(false) }
    var phoneError     by remember { mutableStateOf(false) }

    var showSuccess by remember { mutableStateOf(false) }

    // Pre-fill fields when profile is already loaded
    LaunchedEffect(profileState) {
        if (profileState is ProfileUiState.Success) {
            val p = (profileState as ProfileUiState.Success).profile
            firstName = p.firstName
            lastName  = p.lastName
            phone     = p.phone
        }
    }

    // Handle update result
    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateUiState.Success -> {
                showSuccess = true
                viewModel.fetchProfileByUserId(userId, token)
                viewModel.resetUpdateState()
            }
            else -> Unit
        }
    }

    fun validate(): Boolean {
        firstNameError = firstName.isBlank()
        lastNameError  = lastName.isBlank()
        phoneError     = phone.isBlank() || !phone.startsWith("0")
        return !firstNameError && !lastNameError && !phoneError
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_edit_profile),
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

            // ── Info notice ──────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blue50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Blue800,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Text(
                        stringResource(R.string.profile_edit_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = Blue800
                    )
                }
            }

            // ── First Name ───────────────────────────────────────────────
            ProfileInputField(
                label        = stringResource(R.string.reg2_prenom_label),
                value        = firstName,
                onValueChange = { firstName = it; firstNameError = false },
                placeholder  = stringResource(R.string.reg2_ph_prenom),
                isError      = firstNameError,
                errorMessage = stringResource(R.string.profile_field_required),
                leadingIcon  = Icons.Outlined.Person
            )

            // ── Last Name ────────────────────────────────────────────────
            ProfileInputField(
                label        = stringResource(R.string.reg2_nom_label),
                value        = lastName,
                onValueChange = { lastName = it; lastNameError = false },
                placeholder  = stringResource(R.string.reg2_ph_nom),
                isError      = lastNameError,
                errorMessage = stringResource(R.string.profile_field_required),
                leadingIcon  = Icons.Outlined.Person
            )

            // ── Phone ────────────────────────────────────────────────────
            ProfileInputField(
                label        = stringResource(R.string.reg2_phone_label),
                value        = phone,
                onValueChange = { phone = it; phoneError = false },
                placeholder  = stringResource(R.string.reg2_ph_phone),
                isError      = phoneError,
                errorMessage = stringResource(R.string.err_phone_invalid),
                leadingIcon  = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone
            )

            // ── Error from API ───────────────────────────────────────────
            if (updateState is UpdateUiState.Error) {
                Text(
                    text = (updateState as UpdateUiState.Error).message,
                    color = Red600,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .background(Red50, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .fillMaxWidth()
                )
            }

            // ── Success Banner ───────────────────────────────────────────
            if (showSuccess) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Green50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Green500,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            stringResource(R.string.profile_update_success),
                            style = MaterialTheme.typography.bodySmall,
                            color = Green700
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Save Button ──────────────────────────────────────────────
            Button(
                onClick = {
                    showSuccess = false
                    if (validate()) {
                        viewModel.updateProfile(
                            profileId, token,
                            UpdateProfileRequest(
                                firstName.trim(),
                                lastName.trim(),
                                phone.trim()
                            )
                        )
                    }
                },
                enabled = updateState !is UpdateUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green500)
            ) {
                if (updateState is UpdateUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = NavyWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Outlined.Save,
                        contentDescription = null,
                        tint = NavyWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.profile_save_btn),
                        color = NavyWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Reusable input field
// ─────────────────────────────────────────────
@Composable
fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String = "",
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isError) Red600 else Navy500,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Navy300) },
            isError = isError,
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        it,
                        contentDescription = null,
                        tint = if (isError) Red600 else Navy500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Navy800,
                unfocusedBorderColor = Navy100,
                errorBorderColor     = Red600,
                focusedContainerColor   = Navy30,
                unfocusedContainerColor = Navy30
            ),
            singleLine = true
        )
        if (isError && errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Red600, style = MaterialTheme.typography.labelSmall)
        }
    }
}
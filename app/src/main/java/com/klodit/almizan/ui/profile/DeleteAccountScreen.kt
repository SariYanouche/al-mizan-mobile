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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.R
import com.klodit.almizan.data.profile.DeleteUiState
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreen(
    profileId: String,
    token: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val deleteState by viewModel.deleteUiState.collectAsState()
    var showDialog  by remember { mutableStateOf(false) }

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteUiState.Success) onDeleted()
    }
    android.util.Log.d("DELETE_DEBUG", "token passed to screen = '${token.take(30)}'")
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_delete_account),
                        style      = MaterialTheme.typography.titleMedium,
                        color      = NavyWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = NavyWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Red600)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Warning icon
            Box(
                modifier        = Modifier.size(80.dp).background(Red50, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = Red600, modifier = Modifier.size(40.dp))
            }

            Text(
                stringResource(R.string.profile_delete_title),
                style      = MaterialTheme.typography.titleLarge,
                color      = Red600,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Text(
                stringResource(R.string.profile_delete_subtitle),
                style     = MaterialTheme.typography.bodyMedium,
                color     = Navy700,
                textAlign = TextAlign.Center
            )

            // Consequences list
            Card(
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = RedNotice),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.profile_delete_consequences_title),
                        style         = MaterialTheme.typography.labelSmall,
                        color         = Red600,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    listOf(
                        R.string.profile_delete_con1,
                        R.string.profile_delete_con2,
                        R.string.profile_delete_con3,
                        R.string.profile_delete_con4
                    ).forEach { resId ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.Top
                        ) {
                            Text("•", color = Red600, fontWeight = FontWeight.Bold)
                            Text(stringResource(resId), style = MaterialTheme.typography.bodySmall, color = Navy800)
                        }
                    }
                }
            }

            // API error banner
            if (deleteState is DeleteUiState.Error) {
                Text(
                    text     = (deleteState as DeleteUiState.Error).message,
                    color    = Red600,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .background(Red50, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            // Delete button — no typing required, just tap to open dialog
            Button(
                onClick  = { showDialog = true },
                enabled  = deleteState !is DeleteUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = Red600,
                    disabledContainerColor = Red600.copy(alpha = 0.35f)
                )
            ) {
                if (deleteState is DeleteUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NavyWhite, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = NavyWhite, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_delete_btn), color = NavyWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // Cancel button
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.profile_cancel), color = Navy700)
            }
        }
    }

    // Final confirmation dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = { Icon(Icons.Outlined.Warning, contentDescription = null, tint = Red600) },
            title = {
                Text(stringResource(R.string.profile_delete_dialog_title), color = Red600, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(stringResource(R.string.profile_delete_dialog_body), color = Navy700, style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        viewModel.deleteProfile(profileId, token)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red600)
                ) {
                    Text(stringResource(R.string.profile_delete_btn), color = NavyWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.profile_cancel), color = Navy500)
                }
            },
            containerColor = NavyWhite,
            shape          = RoundedCornerShape(16.dp)
        )
    }
}
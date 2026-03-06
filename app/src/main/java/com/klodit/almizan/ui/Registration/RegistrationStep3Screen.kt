package com.klodit.almizan.ui.Registration

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.klodit.almizan.R
import com.klodit.almizan.ui.auth.LanguageSwitcher
import com.klodit.almizan.ui.theme.AppLanguage
import com.klodit.almizan.ui.theme.Blue800
import com.klodit.almizan.ui.theme.Blue50
import com.klodit.almizan.ui.theme.BlueBorder
import com.klodit.almizan.ui.theme.Grey200
import com.klodit.almizan.viewmodel.auth.AuthState

private enum class DocState { EMPTY, ANALYZING, DONE }


@Composable
fun RegistrationStep3Screen(
    onSubmitClick   : () -> Unit = {},
    onBackClick     : () -> Unit = {},
    selectedLang    : AppLanguage = AppLanguage.FRENCH,
    onLanguageChange: (AppLanguage) -> Unit = {},
    authState       : AuthState = AuthState.Idle,
    uploadState     : AuthState = AuthState.Idle,
    onPickFile        : () -> Unit = {},
    onClearError    : () -> Unit = {},
    onClearUploadError: () -> Unit = {}

) {
    val cs = MaterialTheme.colorScheme

    var docState         by remember { mutableStateOf(DocState.EMPTY) }
    var docFileName      by remember { mutableStateOf("") }
    var analysisProgress by remember { mutableStateOf(0f) }
    val scope            = rememberCoroutineScope()

    val isSubmitting = authState is AuthState.Loading

    val screenWidth   = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth     = if (screenWidth < 500.dp) screenWidth * 0.90f else 420.dp
    val overlapAmount = 32.dp

    val animatedProgress by animateFloatAsState(
        targetValue   = analysisProgress,
        animationSpec = tween(400),
        label         = "progress"
    )

    // Error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    //succes registration
    var showSuccessDialog by remember { mutableStateOf(false) }

    val errorMessage = (authState as? AuthState.Error)?.message

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onClearError()
        }
    }
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            showSuccessDialog = true
        }
    }
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is AuthState.Success -> {
                analysisProgress = 1f
                delay(300)
                docState = DocState.DONE
            }
            is AuthState.Error -> {
                docState         = DocState.EMPTY
                docFileName      = ""
                analysisProgress = 0f
                snackbarHostState.showSnackbar(
                    (uploadState as AuthState.Error).message
                )
                onClearUploadError()
            }
            is AuthState.Loading -> {
                // animate progress while uploading
                scope.launch {
                    for (step in listOf(0.2f, 0.4f, 0.6f, 0.78f)) {
                        delay(600)
                        if (docState == DocState.ANALYZING)
                            analysisProgress = step
                    }
                }
            }
            else -> {}
        }
    }






    fun simulateUpload(fakeName: String) {
        docFileName      = fakeName
        docState         = DocState.ANALYZING
        analysisProgress = 0f
        scope.launch {
            for (step in listOf(0.2f, 0.4f, 0.6f, 0.78f, 0.91f, 1f)) {
                delay(500)
                analysisProgress = step
            }
            delay(300)
            docState = DocState.DONE
        }
    }

    // ── Success Dialog ────────────────────────────────────────────────────────────
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },   // prevent dismiss by tapping outside
            containerColor   = cs.surface,
            shape            = RoundedCornerShape(20.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()) {

                    // green circle with checkmark
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(cs.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle, null,
                            tint     = cs.secondary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        stringResource(R.string.reg_success_title),
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = cs.onSurface,
                        textAlign  = TextAlign.Center
                    )
                }
            },
            text = {
                Text(
                    stringResource(R.string.reg_success_body),
                    fontSize  = 13.sp,
                    color     = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier  = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick  = {
                        showSuccessDialog = false
                        onSubmitClick()   // this triggers NavGraph navigation to Login
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = cs.secondary)
                ) {
                    Text(
                        stringResource(R.string.reg_success_btn),
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = cs.onSecondary
                    )
                }
            }
        )
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
        ) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // header
                Box(
                    modifier = Modifier.fillMaxWidth().background(cs.primary)
                        .statusBarsPadding().padding(bottom = overlapAmount + 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, null,
                                tint = cs.onPrimary, modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            "Registration", fontSize = 17.sp,
                            fontWeight = FontWeight.Bold, color = cs.onPrimary
                        )
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Outlined.Info, null,
                                tint = cs.onPrimary, modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // progress card (100%)
                Card(
                    modifier  = Modifier.width(cardWidth).offset(y = -overlapAmount).zIndex(1f),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.reg3_step_label),
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color      = cs.secondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "100%", fontSize = 11.sp,
                                color = cs.onSurfaceVariant, fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(5.dp)
                                .clip(RoundedCornerShape(3.dp)).background(cs.secondary)
                        )
                    }
                }

                Spacer(Modifier.height((-overlapAmount.value + 8).dp))

                Column(modifier = Modifier.width(cardWidth)) {

                    Text(
                        stringResource(R.string.reg3_section_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = cs.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.reg3_section_sub),
                        fontSize = 13.sp, color = cs.onSurfaceVariant, lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(24.dp))

                    // document card
                    when (docState) {

                        DocState.EMPTY -> {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.5.dp, cs.outline, RoundedCornerShape(14.dp))
                                    .background(cs.surface)
                                    .clickable { onPickFile() }
                                    .padding(vertical = 36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier.size(64.dp).clip(CircleShape)
                                            .background(cs.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.UploadFile, null,
                                            tint = cs.secondary, modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(14.dp))
                                    Text(
                                        stringResource(R.string.reg3_upload_title),
                                        fontSize   = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = cs.onSurface
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        stringResource(R.string.reg3_upload_sub),
                                        fontSize = 12.sp, color = cs.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Box(
                                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                            .background(cs.secondary)
                                            .padding(horizontal = 20.dp, vertical = 9.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.reg3_upload_btn),
                                            fontSize      = 11.sp,
                                            fontWeight    = FontWeight.Bold,
                                            color         = cs.onSecondary,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }

                        DocState.ANALYZING -> {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, cs.secondary, RoundedCornerShape(12.dp))
                                    .background(cs.secondaryContainer)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                                        .background(cs.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(28.dp),
                                        color       = cs.secondary,
                                        strokeWidth = 2.5.dp
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.reg3_analyzing),
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = cs.onSurface
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        docFileName, fontSize = 11.sp,
                                        color = cs.secondary, fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)).background(Grey200)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(animatedProgress)
                                                .fillMaxHeight().clip(RoundedCornerShape(3.dp))
                                                .background(cs.secondary)
                                        )
                                    }
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        stringResource(R.string.reg3_analyzing_msg),
                                        fontSize = 10.sp, color = cs.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }

                        DocState.DONE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, BlueBorder, RoundedCornerShape(12.dp))
                                    .background(Blue50)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                                        .background(cs.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.PictureAsPdf, null,
                                        tint = Blue800, modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.CheckCircle, null,
                                            tint = cs.secondary, modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            stringResource(R.string.reg3_analyzed),
                                            fontSize   = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = cs.secondary
                                        )
                                    }
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        docFileName, fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold, color = cs.onSurface
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        stringResource(R.string.reg3_analyzed_msg),
                                        fontSize = 11.sp, color = cs.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        stringResource(R.string.reg3_change_doc),
                                        fontSize   = 11.sp,
                                        color      = Blue800,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier   = Modifier.clickable {
                                            docState         = DocState.EMPTY
                                            docFileName      = ""
                                            analysisProgress = 0f
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // privacy notice
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(Blue50).padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Outlined.Shield, null, tint = Blue800, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                stringResource(R.string.reg3_privacy_title),
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color      = cs.onSurface
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                stringResource(R.string.reg3_privacy_body),
                                fontSize = 12.sp, color = cs.onSurfaceVariant, lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    LanguageSwitcher(selectedLang, onLanguageChange)
                    Spacer(Modifier.height(24.dp))
                }
            }

            // bottom bar
            Column(
                modifier = Modifier.fillMaxWidth().background(cs.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.Lock, null, tint = cs.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        stringResource(R.string.e2ee_secured),
                        fontSize      = 9.sp,
                        color         = cs.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        fontWeight    = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick  = onSubmitClick,
                    //enabled  = docState == DocState.DONE && !isSubmitting,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = cs.secondary,
                        disabledContainerColor = cs.secondaryContainer
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(22.dp),
                            color       = cs.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            stringResource(R.string.reg3_submit_btn),
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = cs.onSecondary
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.reg3_back_label),
                    fontSize = 13.sp, color = cs.onSurfaceVariant,
                    modifier = Modifier.clickable { onBackClick() }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.footer_ministry),
                    fontSize      = 9.sp,
                    color         = cs.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    textAlign     = TextAlign.Center
                )
            }
        }
    }
}
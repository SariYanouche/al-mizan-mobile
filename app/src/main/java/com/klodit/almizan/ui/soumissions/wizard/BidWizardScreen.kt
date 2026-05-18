package com.klodit.almizan.ui.bidwizard

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.R
import com.klodit.almizan.model.BidWizardState
import com.klodit.almizan.model.SubmissionResult
import com.klodit.almizan.model.UploadedDocument
import com.klodit.almizan.ui.bidwizard.components.StepIndicator
import com.klodit.almizan.ui.soumissions.wizard.steps.*
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.BidWizardViewModel

@Composable
fun BidWizardScreen(
    localizedContext: Context,
    appelOffreId: String,
    onExit: () -> Unit,
    onSubmitBid: (BidWizardState) -> Unit = {},
    viewModel: BidWizardViewModel = viewModel()
) {
    val wizardState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submitError by viewModel.submitError.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()

    val context = LocalContext.current

    // Initialize the Wizard
    LaunchedEffect(appelOffreId) {
        viewModel.initWizard(appelOffreId)
    }

    // Success Screen Routing
    if (submissionResult != null) {
        SuccessScreen(
            result = submissionResult!!,
            localizedContext = localizedContext,
            onReturnToDashboard = onExit
        )
        return
    }

    // File Picker Launchers

    val techOfferLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val size = getFileSize(context, it)
            val name = getFileName(context, it) ?: "offre_technique.pdf"
            viewModel.updateTechOffer(UploadedDocument(nom = name, tailleOctets = size, fichierUrl = it.toString(), hashSha256 = "0x..."))
        }
    }

    val cautionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val size = getFileSize(context, it)
            val name = getFileName(context, it) ?: "caution.pdf"
            val updatedCaution = wizardState.caution.copy(
                document = UploadedDocument(nom = name, tailleOctets = size, fichierUrl = it.toString(), hashSha256 = "0x...")
            )
            viewModel.updateCaution(updatedCaution)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy50)
            .navigationBarsPadding()
    ) {
        // Shared Top Bar
        WizardTopBar(
            localizedContext = localizedContext,
            currentStep = wizardState.currentStep,
            totalSteps = wizardState.totalSteps,
            onBackClick = {
                if (wizardState.currentStep > 1) viewModel.prevStep() else onExit()
            },
            onSaveExitClick = onExit
        )

        // Progress Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyWhite)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            StepIndicator(currentStep = wizardState.currentStep, localizedContext = localizedContext)
        }

        Spacer(Modifier.height(24.dp))

        // Step Content Host
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (wizardState.currentStep) {
                1 -> Step1LotSelection(
                    state = wizardState,
                    localizedContext = localizedContext,
                    viewModel = viewModel,
                    onLotSelected = { viewModel.selectLot(it) },
                    onNext = { viewModel.nextStep() }
                )
                2 -> Step2TechnicalOffer(
                    state = wizardState,
                    localizedContext = localizedContext,
                    onUploadClick = { techOfferLauncher.launch("application/pdf") },
                    onBack = { viewModel.prevStep() },
                    onNext = { viewModel.nextStep() }
                )
                3 -> Step3FinancialOffer(
                    state = wizardState,
                    localizedContext = localizedContext,
                    viewModel = viewModel,
                    onBack = { viewModel.prevStep() },
                    onNext = { viewModel.nextStep() }
                )
                4 -> Step4BankGuarantee(
                    state = wizardState,
                    localizedContext = localizedContext,
                    viewModel = viewModel,
                    onUploadClick = { cautionLauncher.launch("application/pdf") },
                    onBack = { viewModel.prevStep() },
                    onNext = { viewModel.nextStep() }
                )
                5 -> Step5FinalReview(
                    state = wizardState,
                    localizedContext = localizedContext,
                    isSubmitting = isSubmitting,
                    onCertificationToggle = { viewModel.toggleCertification(it) },
                    onBack = { viewModel.prevStep() },
                    onSubmit = {
                        onSubmitBid(wizardState)
                        viewModel.submitBid(localizedContext) 
                    }
                )
            }
        }

        // Error display for final submission
        if (submitError != null && wizardState.currentStep == 5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Red50)
                    .padding(12.dp)
            ) {
                Text(
                    text = submitError!!,
                    color = Red600,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WizardTopBar(
    localizedContext: Context,
    currentStep: Int,
    totalSteps: Int,
    onBackClick: () -> Unit,
    onSaveExitClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NavyWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Navy700)
            }
            Text(
                text = String.format(localizedContext.getString(R.string.wizard_step_of), currentStep, totalSteps),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
            TextButton(onClick = onSaveExitClick) {
                Text(
                    text = localizedContext.getString(R.string.wizard_save_exit),
                    color = Green600,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SUCCESS SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SuccessScreen(
    result: SubmissionResult,
    localizedContext: Context,
    onReturnToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy50),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Green50),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Green500, modifier = Modifier.size(40.dp))
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = localizedContext.getString(R.string.wizard_success_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Navy900
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = String.format(localizedContext.getString(R.string.wizard_success_desc), result.tenderReference),
            fontSize = 13.sp,
            color = Navy600,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Green50,
            border = androidx.compose.foundation.BorderStroke(1.dp, Green500.copy(alpha = 0.3f))
        ) {
            Text(
                text = String.format(localizedContext.getString(R.string.wizard_success_timestamp), result.timestamp),
                color = Green700,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onReturnToDashboard,
            colors = ButtonDefaults.buttonColors(containerColor = Green500),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Text(
                text = localizedContext.getString(R.string.wizard_success_btn_view),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────────────────────────────────────────
private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && index >= 0) name = cursor.getString(index)
    }
    return name
}

private fun getFileSize(context: Context, uri: Uri): Long {
    var size = 0L
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst() && index >= 0) size = cursor.getLong(index)
    }
    return size
}
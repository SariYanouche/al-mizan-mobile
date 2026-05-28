package com.klodit.almizan.ui.bidwizard

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.*
import java.util.UUID
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.viewmodel.RecoursViewModel

// ─────────────────────────────────────────────────────────────
//  File Appeal Screen (Déposer un recours)
//  Fully localized via localizedContext.getString(R.string.*)
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileAppealScreen(
    submissionId: String,
    localizedContext: Context,
    onBackClick: () -> Unit,
    onSubmitAppeal: (String) -> Unit, // Returns the Transaction ID
    viewModel: RecoursViewModel = viewModel()
) {
    val context = LocalContext.current
    val isSubmitting by viewModel.isLoading.collectAsState()
    val submitResult by viewModel.submitResult.collectAsState()
    val aos by viewModel.availableAos.collectAsState()
    val attributions by viewModel.availableAttributions.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAppealOptions()
    }

    var selectedAoId by remember { mutableStateOf("") }
    var selectedAttrId by remember { mutableStateOf("") }
    var justification by remember { mutableStateOf("") }
    var certificationAccepted by remember { mutableStateOf(false) }
    var showSuccessScreen by remember { mutableStateOf(false) }
    var appealTransactionId by remember { mutableStateOf("") }

    var expandedAo by remember { mutableStateOf(false) }
    var expandedAttr by remember { mutableStateOf(false) }

    // ── File upload state ──
    var uploadedFileUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedFileName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(submitResult) {
        if (submitResult?.isSuccess == true) {
            appealTransactionId = submitResult!!.getOrNull() ?: "TX-RECOURS-SUCCESS"
            showSuccessScreen = true
            viewModel.resetSubmitResult()
        }
    }

    // ── Success Screen ──
    if (showSuccessScreen) {
        AppealSuccessScreen(
            localizedContext = localizedContext,
            transactionId = appealTransactionId,
            onReturnClick = { onSubmitAppeal(appealTransactionId) }
        )
        return
    }

    val canSubmit = justification.isNotBlank() && certificationAccepted && selectedAoId.isNotBlank() && selectedAttrId.isNotBlank()

    Column(modifier = Modifier.fillMaxSize().background(Navy50)) {
        // ── Top Bar ──
        Surface(modifier = Modifier.fillMaxWidth(), color = Navy800) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NavyWhite) }
                Text(localizedContext.getString(R.string.appeal_title), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = NavyWhite, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(48.dp))
            }
        }

        // ── Form Body ──
        Column(modifier = Modifier.fillMaxSize().weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {

            // Warning Banner
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFFFFF3E0)) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Outlined.Warning, null, tint = Color(0xFFE65100), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(localizedContext.getString(R.string.appeal_warning), color = Color(0xFFE65100), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── AO Dropdown ──
            Text(localizedContext.getString(R.string.appeal_ao_label), color = Navy900, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expandedAo, onExpandedChange = { expandedAo = !expandedAo }) {
                OutlinedTextField(
                    value = aos.find { it.id == selectedAoId }?.reference ?: localizedContext.getString(R.string.appeal_select_ao),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = Green500),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedAo, onDismissRequest = { expandedAo = false }) {
                    aos.forEach { ao ->
                        DropdownMenuItem(
                            text = { Text(ao.reference ?: ao.id, fontSize = 13.sp) },
                            onClick = { selectedAoId = ao.id; expandedAo = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Attribution Dropdown ──
            val filteredAttributions = attributions.filter { it.aoId == selectedAoId }
            Text(localizedContext.getString(R.string.appeal_attribution_label), color = Navy900, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expandedAttr, onExpandedChange = { expandedAttr = !expandedAttr }) {
                OutlinedTextField(
                    value = filteredAttributions.find { it.id == selectedAttrId }?.let { attr ->
                        localizedContext.getString(
                            R.string.appeal_attribution_item_format,
                            attr.dateAttribution?.take(10) ?: "—",
                            attr.montantAttribue ?: "—"
                        )
                    } ?: localizedContext.getString(R.string.appeal_select_attribution),
                    onValueChange = {},
                    readOnly = true,
                    enabled = selectedAoId.isNotBlank(),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = Green500),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedAttr, onDismissRequest = { expandedAttr = false }) {
                    filteredAttributions.forEach { attr ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    localizedContext.getString(
                                        R.string.appeal_attribution_item_format,
                                        attr.dateAttribution?.take(10) ?: "—",
                                        attr.montantAttribue ?: "—"
                                    ),
                                    fontSize = 13.sp
                                )
                            },
                            onClick = { selectedAttrId = attr.id; expandedAttr = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Justification ──
            Text(localizedContext.getString(R.string.appeal_justification_label), color = Navy900, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = justification,
                onValueChange = { justification = it },
                placeholder = { Text(localizedContext.getString(R.string.appeal_justification_placeholder), fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green500)
            )

            Spacer(Modifier.height(24.dp))

            // ── File Upload Section ──
            AppealFileUploadSection(
                localizedContext = localizedContext,
                uploadedFileName = uploadedFileName,
                onFileSelected = { uri ->
                    uploadedFileUri = uri
                    uploadedFileName = uri?.lastPathSegment ?: "document.pdf"
                }
            )

            Spacer(Modifier.height(24.dp))

            // ── Certification Checkbox ──
            Row(modifier = Modifier.fillMaxWidth().clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { certificationAccepted = !certificationAccepted }) {
                Checkbox(checked = certificationAccepted, onCheckedChange = { certificationAccepted = it }, colors = CheckboxDefaults.colors(checkedColor = Green500))
                Text(localizedContext.getString(R.string.appeal_certification), color = Navy700, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
            }
        }

        // ── Bottom Submit Bar ──
        Column(modifier = Modifier.fillMaxWidth().background(NavyWhite).padding(20.dp).navigationBarsPadding()) {
            Button(
                onClick = { viewModel.submitAppeal(selectedAoId, selectedAttrId, justification) },
                enabled = canSubmit && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Red600, disabledContainerColor = Grey300),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(
                    if (isSubmitting) localizedContext.getString(R.string.appeal_processing) else localizedContext.getString(R.string.appeal_submit),
                    color = NavyWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Extracted Composable: File Upload Section
// ─────────────────────────────────────────────────────────────

@Composable
private fun AppealFileUploadSection(
    localizedContext: Context,
    uploadedFileName: String?,
    onFileSelected: (Uri?) -> Unit
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onFileSelected(uri)
    }

    Text(localizedContext.getString(R.string.appeal_upload_label), color = Navy900, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))

    val hasFile = uploadedFileName != null
    val dashedBorderColor = if (hasFile) Green500 else Grey300
    val dropBgColor = if (hasFile) Green50 else Navy30

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(dropBgColor)
            .drawBehind {
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
                drawRoundRect(
                    color = dashedBorderColor,
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                )
            }
            .clickable { filePickerLauncher.launch("application/*") }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.UploadFile,
                contentDescription = null,
                tint = if (hasFile) Green500 else Navy500,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))

            if (hasFile) {
                Text(uploadedFileName!!, color = Green500, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            } else {
                Text(
                    localizedContext.getString(R.string.appeal_upload_prompt),
                    color = Navy500,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                localizedContext.getString(R.string.appeal_max_size),
                color = Navy400,
                fontSize = 11.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Extracted Composable: Success Screen
// ─────────────────────────────────────────────────────────────

@Composable
private fun AppealSuccessScreen(
    localizedContext: Context,
    transactionId: String,
    onReturnClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy50)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Green50),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, null, tint = Green500, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            localizedContext.getString(R.string.appeal_success_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Navy900
        )
        Spacer(Modifier.height(16.dp))
        Text(
            localizedContext.getString(R.string.appeal_success_desc),
            fontSize = 14.sp,
            color = Navy500,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onReturnClick,
            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                localizedContext.getString(R.string.appeal_back_to_list),
                color = NavyWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
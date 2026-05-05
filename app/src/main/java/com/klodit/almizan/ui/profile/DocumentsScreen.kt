package com.klodit.almizan.ui.profile

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.klodit.almizan.R
import com.klodit.almizan.ui.theme.*
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.viewmodel.profile.ProfileViewModel

// ─────────────────────────────────────────────
//  DOCUMENTS SCREEN
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    localizedContext: Context,
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onUpdateDocument: (DocumentUiModel) -> Unit = {}
) {
    val documents by viewModel.documents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDocumentsData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizedContext.getString(R.string.docs_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = NavyWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizedContext.getString(R.string.docs_back),
                            tint = NavyWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onUploadClick) {
                        Icon(
                            imageVector = Icons.Outlined.CloudUpload,
                            contentDescription = localizedContext.getString(R.string.docs_upload_new),
                            tint = NavyWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy800
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onUploadClick,
                containerColor = Green500,
                contentColor = NavyWhite,
                shape = CircleShape,
                modifier = Modifier.shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = localizedContext.getString(R.string.docs_upload_new)
                )
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green500)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
            )
        ) {
            if (documents.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(localizedContext.getString(R.string.docs_no_documents), color = Navy400, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                // Summary Header
                item {
                    DocumentsSummaryHeader(
                        localizedContext = localizedContext,
                        documents = documents
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Document Cards
                items(documents, key = { it.id }) { document ->
                    DocumentCard(
                        localizedContext = localizedContext,
                        document = document,
                        onUpdateClick = { onUpdateDocument(document) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  SUMMARY HEADER
// ─────────────────────────────────────────────

@Composable
private fun DocumentsSummaryHeader(
    localizedContext: Context,
    documents: List<DocumentUiModel>
) {
    val validCount = documents.count { it.getStatus() == DocumentStatus.VALID }
    val expiredCount = documents.count { it.getStatus() == DocumentStatus.EXPIRED }
    val flaggedCount = documents.count { it.getStatus() == DocumentStatus.AI_FLAG }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Blue50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = localizedContext.getString(R.string.docs_summary_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    count = validCount,
                    label = localizedContext.getString(R.string.docs_status_valid),
                    backgroundColor = Green50,
                    textColor = Green600
                )
                SummaryStatItem(
                    count = expiredCount,
                    label = localizedContext.getString(R.string.docs_status_expired),
                    backgroundColor = Red50,
                    textColor = Red600
                )
                SummaryStatItem(
                    count = flaggedCount,
                    label = localizedContext.getString(R.string.docs_status_ai_flag),
                    backgroundColor = Color(0xFFFFF3E0),
                    textColor = Orange400
                )
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    count: Int,
    label: String,
    backgroundColor: Color,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.8f)
        )
    }
}

// ─────────────────────────────────────────────
//  DOCUMENT CARD
// ─────────────────────────────────────────────

@Composable
private fun DocumentCard(
    localizedContext: Context,
    document: DocumentUiModel,
    onUpdateClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val status = document.getStatus()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Document Type Icon
                DocumentTypeIcon(type = document.type)

                Spacer(modifier = Modifier.width(12.dp))

                // Document Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = getDocumentTypeName(localizedContext, document.type),
                        style = MaterialTheme.typography.titleMedium,
                        color = Navy900,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = document.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Navy500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status Badge
                StatusBadge(
                    localizedContext = localizedContext,
                    status = status
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Navy50)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = localizedContext.getString(R.string.docs_expiration),
                    value = document.dateExpiration.format(dateFormatter),
                    isHighlighted = document.isExpired
                )
                DetailItem(
                    label = localizedContext.getString(R.string.docs_size),
                    value = document.formattedFileSize
                )
                DetailItem(
                    label = localizedContext.getString(R.string.docs_ai_score),
                    value = "${(document.ocrScoreConfiance * 100).toInt()}%",
                    isHighlighted = document.ocrScoreConfiance < 0.80
                )
            }

            // AI Flag Section
            if (document.hasAiFlag) {
                Spacer(modifier = Modifier.height(12.dp))

                // Expandable AI Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF8E1))
                        .clickable { isExpanded = !isExpanded }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = Orange400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizedContext.getString(R.string.docs_ai_anomaly_detected),
                        style = MaterialTheme.typography.bodySmall,
                        color = Navy700,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (isExpanded) 
                            Icons.Default.KeyboardArrowUp 
                        else 
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Orange400
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF8E1))
                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                    ) {
                        HorizontalDivider(
                            color = Orange400.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Confidence Score
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${localizedContext.getString(R.string.docs_confidence)}:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Navy700
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ConfidenceIndicator(score = document.ocrScoreConfiance)
                        }

                        if (!document.ocrAnomalies.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${localizedContext.getString(R.string.docs_anomalies)}:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Navy700,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = document.ocrAnomalies,
                                style = MaterialTheme.typography.bodySmall,
                                color = Navy600,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Update Button for Expired
            if (status == DocumentStatus.EXPIRED) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onUpdateClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red600
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizedContext.getString(R.string.docs_update_btn),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  DOCUMENT TYPE ICON
// ─────────────────────────────────────────────

@Composable
private fun DocumentTypeIcon(type: DocumentType) {
    val (backgroundColor, iconColor) = when (type) {
        DocumentType.NIF -> Blue50 to Blue700
        DocumentType.NIS -> Green50 to Green600
        DocumentType.RC -> Color(0xFFFCE4EC) to Color(0xFFD81B60)
        DocumentType.CNAS -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        DocumentType.CASNOS -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
        DocumentType.BILAN -> Color(0xFFFFF3E0) to Orange400
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ─────────────────────────────────────────────
//  STATUS BADGE
// ─────────────────────────────────────────────

@Composable
private fun StatusBadge(
    localizedContext: Context,
    status: DocumentStatus
) {
    val (backgroundColor, textColor, text) = when (status) {
        DocumentStatus.VALID -> Triple(
            Green50,
            Green600,
            localizedContext.getString(R.string.docs_status_valid)
        )
        DocumentStatus.EXPIRED -> Triple(
            Red50,
            Red600,
            localizedContext.getString(R.string.docs_status_expired)
        )
        DocumentStatus.AI_FLAG -> Triple(
            Color(0xFFFFF3E0),
            Orange400,
            localizedContext.getString(R.string.docs_status_ai_flag)
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontSize = 10.sp
        )
    }
}

// ─────────────────────────────────────────────
//  DETAIL ITEM
// ─────────────────────────────────────────────

@Composable
private fun DetailItem(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Navy500
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = if (isHighlighted) Red600 else Navy900,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────
//  CONFIDENCE INDICATOR
// ─────────────────────────────────────────────

@Composable
private fun ConfidenceIndicator(score: Double) {
    val percentage = (score * 100).toInt()
    val color = when {
        score >= 0.90 -> Green500
        score >= 0.75 -> Orange400
        else -> Red600
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Navy100)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(score.toFloat())
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────
//  HELPER FUNCTIONS
// ─────────────────────────────────────────────

private fun getDocumentTypeName(context: Context, type: DocumentType): String {
    return when (type) {
        DocumentType.NIF -> context.getString(R.string.docs_type_nif)
        DocumentType.NIS -> context.getString(R.string.docs_type_nis)
        DocumentType.RC -> context.getString(R.string.docs_type_rc)
        DocumentType.CNAS -> context.getString(R.string.docs_type_cnas)
        DocumentType.CASNOS -> context.getString(R.string.docs_type_casnos)
        DocumentType.BILAN -> context.getString(R.string.docs_type_bilan)
    }
}

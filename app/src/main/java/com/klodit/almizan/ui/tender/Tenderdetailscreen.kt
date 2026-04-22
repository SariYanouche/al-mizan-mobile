package com.klodit.almizan.ui.tender

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.model.tender.Tender
import com.klodit.almizan.model.tender.TenderLot
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.tender.TenderDetailState
import com.klodit.almizan.viewmodel.tender.TenderDetailViewModel


// ─── Date helper ──────────────────────────────────────────────────────────────
private fun String?.toDisplayDate(): String =
    if (this != null && this.length >= 10) this.take(10) else "—"

@Composable
fun TenderDetailScreen(
    tenderId    : String,
    onBack      : () -> Unit,
    viewModel   : TenderDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(tenderId) { viewModel.fetchTender(tenderId) }

    Scaffold(
        containerColor = Navy50,
        topBar = {
            TenderDetailTopBar(onBack = onBack)
        }
    ) { innerPadding ->

        when (val s = state) {
            is TenderDetailState.Loading -> {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Navy800)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading tender…", color = Navy500, fontSize = 14.sp)
                    }
                }
            }

            is TenderDetailState.Error -> {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.error,
                            modifier           = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text      = s.message,
                            color     = MaterialTheme.colorScheme.error,
                            fontSize  = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchTender(tenderId) },
                            colors  = ButtonDefaults.buttonColors(containerColor = Navy800),
                            shape   = RoundedCornerShape(10.dp)
                        ) { Text("Retry", color = NavyWhite) }
                    }
                }
            }

            is TenderDetailState.Success -> {
                TenderDetailContent(
                    tender       = s.tender,
                    innerPadding = innerPadding
                )
            }
        }
    }
}

// ─── Top bar ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TenderDetailTopBar(onBack: () -> Unit) {
    Surface(
        color           = Navy800,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBackIosNew, null, tint = NavyWhite, modifier = Modifier.size(20.dp))
            }
            Text(
                text       = "Tender Details",
                color      = NavyWhite,
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.weight(1f)
            )
        }
    }
}

// ─── Main content ─────────────────────────────────────────────────────────────
@Composable
private fun TenderDetailContent(tender: Tender, innerPadding: PaddingValues) {

    val statusColor = when (tender.statut) {
        "PUBLIE" -> Green500
        "ANNULE" -> Navy500
        else     -> Navy500
    }

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        // ── Hero header ───────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Navy800, Navy700))
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    // Status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.18f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text       = tender.statut,
                            color      = if (tender.statut == "PUBLIE") Green400 else Navy300,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text       = tender.objet,
                        color      = NavyWhite,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text     = tender.reference,
                        color    = NavyWhite.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        HeroChip(icon = Icons.Outlined.LocationOn, label = tender.wilaya)
                        HeroChip(icon = Icons.Outlined.Category,   label = tender.secteurActivite)
                    }
                }
            }
        }

        // ── Key dates card ────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(16.dp))
            DetailCard(
                title = "Key Dates",
                icon  = Icons.Outlined.CalendarMonth,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                DateRow(
                    label    = "Publication Date",
                    value    = tender.datePublication.toDisplayDate(),
                    icon     = Icons.Outlined.PublishedWithChanges,
                    isLast   = false
                )
                DateRow(
                    label    = "Submission Deadline",
                    value    = tender.dateLimiteSoumission.toDisplayDate(),
                    icon     = Icons.Outlined.Schedule,
                    highlight = true,
                    isLast   = false
                )
                DateRow(
                    label    = "CDC Retrieval Deadline",
                    value    = tender.dateLimiteRetraitCdc.toDisplayDate(),
                    icon     = Icons.Outlined.AssignmentReturn,
                    isLast   = true
                )
            }
        }

        // ── General info card ─────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(12.dp))
            DetailCard(
                title    = "General Information",
                icon     = Icons.Outlined.Info,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                InfoDetailRow(
                    label  = "Procedure Type",
                    value  = tender.typeProcedure.replace("_", " "),
                    icon   = Icons.Outlined.Gavel,
                    isLast = false
                )
                InfoDetailRow(
                    label  = "Wilaya",
                    value  = tender.wilaya,
                    icon   = Icons.Outlined.LocationOn,
                    isLast = false
                )
                InfoDetailRow(
                    label  = "Sector",
                    value  = tender.secteurActivite,
                    icon   = Icons.Outlined.BusinessCenter,
                    isLast = false
                )
                InfoDetailRow(
                    label  = "Estimated Amount",
                    value  = tender.montantEstime?.let { "DZD $it" } ?: "—",
                    icon   = Icons.Outlined.Payments,
                    isLast = false
                )
                InfoDetailRow(
                    label  = "Created At",
                    value  = tender.createdAt.toDisplayDate(),
                    icon   = Icons.Outlined.AccessTime,
                    isLast = true
                )
            }
        }

        // ── Lots card ─────────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(12.dp))
            DetailCard(
                title    = "Lots (${tender.lots.size})",
                icon     = Icons.Outlined.Layers,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (tender.lots.isEmpty()) {
                    EmptySection(message = "No lots defined for this tender.")
                } else {
                    tender.lots.forEachIndexed { index, lot ->
                        LotRow(lot = lot, isLast = index == tender.lots.lastIndex)
                    }
                }
            }
        }

        // ── Eligibility criteria card ─────────────────────────────────────────
        item {
            Spacer(Modifier.height(12.dp))
            DetailCard(
                title    = "Eligibility Criteria",
                icon     = Icons.Outlined.FactCheck,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                EmptySection(message = "No eligibility criteria specified.")
            }
        }

        // ── Evaluation criteria card ──────────────────────────────────────────
        item {
            Spacer(Modifier.height(12.dp))
            DetailCard(
                title    = "Evaluation Criteria",
                icon     = Icons.Outlined.Assessment,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                EmptySection(message = "No evaluation criteria specified.")
            }
        }

        // ── Apply button ──────────────────────────────────────────────────────
        if (tender.statut == "PUBLIE") {
            item {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick        = { /* TODO: navigate to bid submission */ },
                    colors         = ButtonDefaults.buttonColors(containerColor = Green500),
                    shape          = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier       = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp)
                ) {
                    Icon(Icons.Outlined.Send, null, tint = NavyWhite, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Submit a Bid", color = NavyWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Hero chip ────────────────────────────────────────────────────────────────
@Composable
private fun HeroChip(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NavyWhite.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(icon, null, tint = NavyWhite.copy(alpha = 0.7f), modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, color = NavyWhite.copy(alpha = 0.9f), fontSize = 12.sp)
    }
}

// ─── Detail card wrapper ──────────────────────────────────────────────────────
@Composable
private fun DetailCard(
    title    : String,
    icon     : ImageVector,
    modifier : Modifier = Modifier,
    content  : @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = NavyWhite)
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Navy50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Navy800, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text          = title.uppercase(),
                    color         = Navy700,
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
            HorizontalDivider(color = Navy100, thickness = 0.5.dp)
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

// ─── Date row ─────────────────────────────────────────────────────────────────
@Composable
private fun DateRow(
    label     : String,
    value     : String,
    icon      : ImageVector,
    highlight : Boolean = false,
    isLast    : Boolean = false
) {
    Column {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, null,
                tint     = if (highlight) Green500 else Navy500,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Navy500, fontSize = 11.sp, letterSpacing = 0.3.sp)
                Text(
                    text       = value,
                    color      = if (highlight) Green500 else Navy900,
                    fontSize   = 14.sp,
                    fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
        if (!isLast) HorizontalDivider(color = Navy100, thickness = 0.5.dp, modifier = Modifier.padding(start = 46.dp))
    }
}

// ─── Info detail row ──────────────────────────────────────────────────────────
@Composable
private fun InfoDetailRow(
    label  : String,
    value  : String,
    icon   : ImageVector,
    isLast : Boolean = false
) {
    Column {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Navy500, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Navy500, fontSize = 11.sp, letterSpacing = 0.3.sp)
                Text(value, color = Navy900, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (!isLast) HorizontalDivider(color = Navy100, thickness = 0.5.dp, modifier = Modifier.padding(start = 46.dp))
    }
}

// ─── Lot row ──────────────────────────────────────────────────────────────────
@Composable
private fun LotRow(lot: TenderLot, isLast: Boolean = false) {
    val lotStatusColor = when (lot.statut) {
        "PUBLIE" -> Green500
        "ANNULE" -> Navy500
        else     -> Navy500
    }
    Column {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier         = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Navy50),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = lot.numero,
                    color      = Navy800,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lot.designation, color = Navy900, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                lot.montantEstime?.let {
                    Spacer(Modifier.height(2.dp))
                    Text("DZD $it", color = Navy500, fontSize = 12.sp)
                }
            }
            lot.statut?.let { status ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(lotStatusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(status, color = lotStatusColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (!isLast) HorizontalDivider(color = Navy100, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────
@Composable
private fun EmptySection(message: String) {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = Navy500, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}
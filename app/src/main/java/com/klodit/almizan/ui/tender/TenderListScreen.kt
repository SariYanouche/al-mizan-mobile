package com.klodit.almizan.ui.tender

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.model.tender.Tender
import com.klodit.almizan.ui.search.FilterState
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.tender.TenderViewModel

// ─── Date helper ──────────────────────────────────────────────────────────────
private fun String?.toDateOnly(): String? =
    if (this != null && this.length >= 10) this.take(10) else null

// ─── Screen ───────────────────────────────────────────────────────────────────
@Composable
fun TenderListScreen(
    innerPadding         : PaddingValues,
    localizedContext     : Context,
    activeFilter         : FilterState    = FilterState(),
    initialSearchQuery   : String         = "",
    initialSector        : String         = "",
    initialWilaya        : String         = "",
    onNavigateToFilter   : () -> Unit,
    onNavigateToDetail   : (String) -> Unit,
    viewModel            : TenderViewModel = viewModel()
) {
    val tenders   by viewModel.tenders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.error.collectAsState()

    val filterTabs = listOf("All", "PUBLIE", "ANNULE")
    var selectedTab  by remember { mutableStateOf("All") }
    var searchQuery  by remember { mutableStateOf(initialSearchQuery) }
    var selectedSector by remember { mutableStateOf(initialSector) }
    var selectedWilaya by remember { mutableStateOf(initialWilaya) }

    LaunchedEffect(Unit) { viewModel.fetchTenders() }

    // ── Debug log ─────────────────────────────────────────────────────────────
    LaunchedEffect(tenders) {
        if (tenders.isNotEmpty()) {
            Log.d("TENDER_DATE_DEBUG", "=== Sample tender dates ===")
            tenders.take(5).forEach { t ->
                Log.d("TENDER_DATE_DEBUG",
                    "id=${t.id} | " +
                            "datePublication=${t.datePublication} | " +
                            "dateLimiteSoumission=${t.dateLimiteSoumission} | " +
                            "toDateOnly=${t.datePublication.toDateOnly()}"
                )
            }
            Log.d("TENDER_DATE_DEBUG",
                "activeFilter: from=${activeFilter.dateFrom} to=${activeFilter.dateTo}"
            )
        }
    }

    // ── Filtering ─────────────────────────────────────────────────────────────
    val visible = remember(tenders, selectedTab, searchQuery, selectedSector, selectedWilaya, activeFilter) {
        tenders.filter { t ->

            val matchesTab = selectedTab == "All" || t.statut == selectedTab

            val matchesSearch = searchQuery.isBlank() ||
                    t.objet.contains(searchQuery, ignoreCase = true) ||
                    t.reference.contains(searchQuery, ignoreCase = true) ||
                    t.wilaya.contains(searchQuery, ignoreCase = true) ||
                    t.secteurActivite.contains(searchQuery, ignoreCase = true)

            // Home page quick filters (sector from dropdown, wilaya from input)
            val matchesQuickSector = selectedSector.isBlank() ||
                    t.secteurActivite.equals(selectedSector, ignoreCase = true)
            val matchesQuickWilaya = selectedWilaya.isBlank() ||
                    t.wilaya.contains(selectedWilaya, ignoreCase = true)

            // Advanced filters (from detailed filter)
            val matchesSector = activeFilter.selectedSectors.isEmpty() ||
                    t.secteurActivite in activeFilter.selectedSectors
            val matchesStatus = activeFilter.selectedStatuses.isEmpty() ||
                    t.statut in activeFilter.selectedStatuses
            val matchesWilaya = activeFilter.selectedWilayas.isEmpty() ||
                    t.wilaya in activeFilter.selectedWilayas

            val matchesDate = run {
                val fromFilter = activeFilter.dateFrom
                val toFilter   = activeFilter.dateTo
                if (fromFilter == null && toFilter == null) return@run true
                val deadlineDate = t.dateLimiteSoumission.toDateOnly() ?: return@run false
                val afterFrom = fromFilter == null || deadlineDate >= fromFilter
                val beforeTo  = toFilter   == null || deadlineDate <= toFilter
                afterFrom && beforeTo
            }

            matchesTab && matchesSearch && matchesQuickSector && matchesQuickWilaya &&
                    matchesSector && matchesStatus && matchesWilaya && matchesDate
        }
    }

    val filterIsActive = activeFilter != FilterState()

    // ── Layout ────────────────────────────────────────────────────────────────
    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {

        // ── Search bar + filter button ────────────────────────────────────────
        item {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Search tenders…", color = Navy500, fontSize = 14.sp) },
                    leadingIcon   = { Icon(Icons.Default.Search, null, tint = Navy500) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = NavyWhite,
                        focusedContainerColor   = NavyWhite,
                        unfocusedBorderColor    = Navy100,
                        focusedBorderColor      = Navy800,
                        cursorColor             = Navy800
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                )

                BadgedBox(
                    badge = { if (filterIsActive) Badge(containerColor = Green500) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .size(52.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (filterIsActive) Green500.copy(alpha = 0.08f) else NavyWhite)
                            .border(
                                1.dp,
                                if (filterIsActive) Green500 else Navy100,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onNavigateToFilter() }
                    ) {
                        Icon(
                            Icons.Outlined.Tune,
                            null,
                            tint     = if (filterIsActive) Green500 else Navy800,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── Status tabs ───────────────────────────────────────────────────────
        item {
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterTabs) { tab ->
                    TenderFilterChip(
                        label    = tab,
                        selected = selectedTab == tab,
                        onClick  = { selectedTab = tab }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Active filter summary ─────────────────────────────────────────────
        if (filterIsActive) {
            item {
                ActiveFilterSummary(
                    filter   = activeFilter,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Loading ───────────────────────────────────────────────────────────
        if (isLoading) {
            item {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Navy800) }
            }
        }

        // ── Error ─────────────────────────────────────────────────────────────
        error?.let { msg ->
            item {
                Text(
                    text     = "Failed to load tenders: $msg",
                    color    = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // ── Empty state ───────────────────────────────────────────────────────
        if (!isLoading && error == null && visible.isEmpty()) {
            item {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No tenders found.", color = Navy500, fontSize = 14.sp)
                        if (filterIsActive) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text     = "Try adjusting or resetting your filters.",
                                color    = Navy500,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // ── Tender cards ──────────────────────────────────────────────────────
        items(visible, key = { it.id }) { tender ->
            TenderCard(
                tender             = tender,
                onNavigateToDetail = onNavigateToDetail,
                modifier           = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

// ─── Active filter summary ────────────────────────────────────────────────────
@Composable
private fun ActiveFilterSummary(filter: FilterState, modifier: Modifier = Modifier) {
    val parts = buildList {
        if (filter.selectedSectors.isNotEmpty())
            add("${filter.selectedSectors.size} sector(s)")
        if (filter.selectedStatuses.isNotEmpty())
            add("${filter.selectedStatuses.size} status(es)")
        if (filter.selectedWilayas.isNotEmpty())
            add("${filter.selectedWilayas.size} wilaya(s)")
        if (filter.dateFrom != null || filter.dateTo != null)
            add("${filter.dateFrom ?: "Any"} → ${filter.dateTo ?: "Any"}")
    }

    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Green500.copy(alpha = 0.08f))
            .border(1.dp, Green500.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.FilterList,
            contentDescription = null,
            tint               = Green500,
            modifier           = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text       = "Filters: ${parts.joinToString(" · ")}",
            color      = Green500,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Filter chip ──────────────────────────────────────────────────────────────
@Composable
private fun TenderFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) Navy800 else NavyWhite)
            .border(1.dp, if (selected) Navy800 else Navy100, RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text       = label,
            color      = if (selected) NavyWhite else Navy700,
            fontSize   = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ─── Tender card ──────────────────────────────────────────────────────────────
@Composable
fun TenderCard(
    tender             : Tender,
    onNavigateToDetail : (String) -> Unit,
    modifier           : Modifier = Modifier
) {
    val statusColor = when (tender.statut) {
        "PUBLIE" -> Green500
        "ANNULE" -> Navy500
        else     -> Navy500
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header row ────────────────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Navy50)
                ) {
                    Icon(
                        Icons.Outlined.AccountBalance,
                        null,
                        tint     = Navy800,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text       = tender.secteurActivite,
                    color      = Navy700,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.weight(1f),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = tender.statut,
                        color      = statusColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Title + reference ─────────────────────────────────────────────
            Spacer(Modifier.height(10.dp))
            Text(
                text       = tender.objet,
                color      = Navy900,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(tender.reference, color = Navy500, fontSize = 12.sp)

            // ── Meta row ──────────────────────────────────────────────────────
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = Navy500, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(tender.wilaya, color = Navy500, fontSize = 12.sp)
                }
                tender.dateLimiteSoumission?.let { date ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, tint = Navy500, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(date.take(10), color = Navy500, fontSize = 12.sp)
                    }
                }
            }

            // ── Lots count ────────────────────────────────────────────────────
            if (tender.lots.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("${tender.lots.size} lot(s)", color = Navy500, fontSize = 12.sp)
            }

            // ── Divider + button ──────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Grey100)
            Spacer(Modifier.height(12.dp))

            Button(
                onClick        = { onNavigateToDetail(tender.id) },
                colors         = ButtonDefaults.buttonColors(containerColor = statusColor),
                shape          = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Icon(Icons.Outlined.Visibility, null, tint = NavyWhite, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "View Details",
                    color      = NavyWhite,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
package com.klodit.almizan.ui.soumissions

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.R
import com.klodit.almizan.model.*
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.MyBidsViewModel
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private enum class SubmissionFilter { ALL, ACTIVE_BIDS, RESULTS_APPEALS }

private fun formatDate(isoDate: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(isoDate)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        isoDate.take(10) 
    }
}

private fun calculateDaysLeft(endDateIso: String): Int? {
    return try {
        val endDate = ZonedDateTime.parse(endDateIso)
        val days = ChronoUnit.DAYS.between(ZonedDateTime.now(), endDate).toInt()
        if (days >= 0) days else null
    } catch (e: Exception) { null }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.FRANCE)
    return "${formatter.format(amount.toLong())} DZD"
}

@Composable
fun MyBidsScreen(
    localizedContext: Context,
    viewModel: MyBidsViewModel = viewModel(),
    onStartBidWizard: (appelOffreId: String) -> Unit = {},
    onTrackStatus: (submissionId: String) -> Unit = {},
    onViewResults: (submissionId: String) -> Unit = {},
    onFileAppeal: (submissionId: String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val cdcs by viewModel.cdcs.collectAsState()
    val submissions by viewModel.submissions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
        MyBidsTabRow(selectedTab, { selectedTab = it }, localizedContext)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green500)
            }
        } else {
            when (selectedTab) {
                0 -> PurchasedCDCsContent(localizedContext, cdcs, onStartBidWizard)
                1 -> MySubmissionsContent(localizedContext, submissions, onTrackStatus, onViewResults, onFileAppeal)
            }
        }
    }
}

@Composable
private fun MyBidsTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit, localizedContext: Context) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF0F4F8)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(52.dp).padding(4.dp)) {
            TabButton(localizedContext.getString(R.string.mybids_tab_my_submissions), selectedTab == 1, { onTabSelected(1) }, Modifier.weight(1f))
            TabButton(localizedContext.getString(R.string.mybids_tab_purchased_cdcs), selectedTab == 0, { onTabSelected(0) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxHeight().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) NavyWhite else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = if (isSelected) Navy900 else Navy500, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun PurchasedCDCsContent(localizedContext: Context, cdcs: List<PurchasedCDC>, onStartBidWizard: (String) -> Unit) {
    var hideSubmitted by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCDCs = cdcs.filter {
        (searchQuery.isBlank() || it.objet.contains(searchQuery, true) || it.reference.contains(searchQuery, true)) &&
        (!hideSubmitted || it.uiStatus != PurchasedCDCStatus.SUBMITTED)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { ToggleFilterChip(localizedContext.getString(R.string.mybids_filter_hide_submitted), hideSubmitted) { hideSubmitted = !hideSubmitted } }
            }
            Spacer(Modifier.height(12.dp))
        }
        item {
            SearchBarWithSort(searchQuery, { searchQuery = it }, localizedContext.getString(R.string.mybids_search_tenders))
            Spacer(Modifier.height(16.dp))
        }
        if (filteredCDCs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(localizedContext.getString(R.string.mybids_no_results), color = Navy400, fontSize = 13.sp)
                }
            }
        }
        items(filteredCDCs, key = { it.appelOffreId }) { cdc ->
            PurchasedCDCCard(cdc, localizedContext, { onStartBidWizard(cdc.appelOffreId) }, Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        }
    }
}

@Composable
private fun MySubmissionsContent(
    localizedContext: Context,
    submissions: List<BidSubmission>,
    onTrackStatus: (String) -> Unit,
    onViewResults: (String) -> Unit,
    onFileAppeal: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(SubmissionFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSubmissions = submissions.filter { sub ->
        (searchQuery.isBlank() || sub.objet.contains(searchQuery, true) || sub.reference.contains(searchQuery, true)) &&
        when (selectedFilter) {
            SubmissionFilter.ALL -> true
            SubmissionFilter.ACTIVE_BIDS -> sub.uiStatus == SubmissionUIStatus.IN_EVALUATION
            SubmissionFilter.RESULTS_APPEALS -> sub.uiStatus != SubmissionUIStatus.IN_EVALUATION
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { ToggleFilterChip(localizedContext.getString(R.string.mybids_filter_all), selectedFilter == SubmissionFilter.ALL) { selectedFilter = SubmissionFilter.ALL } }
                item { ToggleFilterChip(localizedContext.getString(R.string.mybids_filter_active_bids), selectedFilter == SubmissionFilter.ACTIVE_BIDS) { selectedFilter = SubmissionFilter.ACTIVE_BIDS } }
                item { ToggleFilterChip(localizedContext.getString(R.string.mybids_filter_results_appeals), selectedFilter == SubmissionFilter.RESULTS_APPEALS) { selectedFilter = SubmissionFilter.RESULTS_APPEALS } }
            }
            Spacer(Modifier.height(12.dp))
        }
        item {
            SearchBarWithSort(searchQuery, { searchQuery = it }, localizedContext.getString(R.string.mybids_search_submissions))
            Spacer(Modifier.height(16.dp))
        }
        if (filteredSubmissions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(localizedContext.getString(R.string.mybids_no_submissions), color = Navy400, fontSize = 13.sp)
                }
            }
        }
        items(filteredSubmissions, key = { it.soumissionId }) { sub ->
            SubmissionCard(sub, localizedContext, { onTrackStatus(sub.soumissionId) }, { onViewResults(sub.soumissionId) }, { onFileAppeal(sub.soumissionId) }, Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        }
    }
}

@Composable
private fun ToggleFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(NavyWhite)
            .border(1.dp, if (isSelected) Green500 else Color(0xFFE5E7EB), RoundedCornerShape(50.dp))
            .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(Icons.Default.Check, null, tint = Green500, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(label, color = if (isSelected) Green500 else Navy700, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal)
        }
    }
}

@Composable
private fun SearchBarWithSort(query: String, onQueryChange: (String) -> Unit, placeholder: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = query, onValueChange = onQueryChange,
            placeholder = { Text(placeholder, color = Navy500, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Navy500) },
            singleLine = true, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = NavyWhite, focusedContainerColor = NavyWhite, focusedBorderColor = Navy800),
            modifier = Modifier.weight(1f).height(52.dp)
        )
    }
}

@Composable
private fun PurchasedCDCCard(cdc: PurchasedCDC, localizedContext: Context, onStartBidWizard: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NavyWhite), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("${localizedContext.getString(R.string.mybids_tender)} ${cdc.reference}", color = Navy500, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(cdc.objet, color = Navy900, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text("${localizedContext.getString(R.string.mybids_deadline)}: ${formatDate(cdc.dateLimiteDepot)}", color = Navy400, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {}, shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Green500), modifier = Modifier.weight(1f).height(46.dp)) {
                    Text(localizedContext.getString(R.string.mybids_read_cdc), color = Green500, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                if (cdc.uiStatus != PurchasedCDCStatus.SUBMITTED) {
                    Button(onClick = onStartBidWizard, colors = ButtonDefaults.buttonColors(containerColor = Green500), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(46.dp)) {
                        Text(if (cdc.uiStatus == PurchasedCDCStatus.IN_PROGRESS) localizedContext.getString(R.string.mybids_resume_bid) else localizedContext.getString(R.string.mybids_start_bid_wizard), color = NavyWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionCard(sub: BidSubmission, localizedContext: Context, onTrackStatus: () -> Unit, onViewResults: () -> Unit, onFileAppeal: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NavyWhite), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(sub.uiStatus.backgroundColor).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(sub.uiStatus.label, color = sub.uiStatus.color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("${localizedContext.getString(R.string.mybids_ref)} ${sub.reference}", color = Navy500, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(12.dp))
            Text(sub.objet, color = Navy900, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text("${localizedContext.getString(R.string.mybids_submitted)}: ${formatDate(sub.dateDepot)}", color = Navy400, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            
            when (sub.uiStatus) {
                SubmissionUIStatus.IN_EVALUATION -> {
                    Button(onClick = onTrackStatus, colors = ButtonDefaults.buttonColors(containerColor = Navy800), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(46.dp)) {
                        Text(localizedContext.getString(R.string.mybids_track_status), color = NavyWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                SubmissionUIStatus.NOT_RETAINED -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onViewResults, shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.weight(1f).height(46.dp)) {
                            Text(localizedContext.getString(R.string.mybids_view_results), color = Navy700, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = onFileAppeal, colors = ButtonDefaults.buttonColors(containerColor = Red600), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(46.dp)) {
                            Text(localizedContext.getString(R.string.mybids_file_appeal), color = NavyWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                SubmissionUIStatus.AWARDED -> {
                    OutlinedButton(onClick = onViewResults, shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Green500), modifier = Modifier.fillMaxWidth().height(46.dp)) {
                        Text(localizedContext.getString(R.string.mybids_view_results), color = Green500, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
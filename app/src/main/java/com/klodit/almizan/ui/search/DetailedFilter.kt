package com.klodit.almizan.ui.search

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.klodit.almizan.model.tender.Tender
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ─── Colors ───────────────────────────────────────────────────────────────────
private val NavyDark    = Color(0xFF364150)
private val SlateGrey   = Color(0xFF475569)
private val Green500    = Color(0xFF4CAF50)
private val Green100    = Color(0xFFE8F5E9)
private val Grey50      = Color(0xFFF8F6F6)
private val GreyBg      = Color(0xFFF8FAFC)
private val BorderGrey  = Color(0xFFE2E8F0)
private val DividerGrey = Color(0xFFF1F5F9)
private val White       = Color(0xFFFFFFFF)
private val ErrorRed    = Color(0xFFE53935)

// ─── Filter state ─────────────────────────────────────────────────────────────
data class FilterState(
    val selectedSectors   : Set<String> = emptySet(),
    val selectedStatuses  : Set<String> = emptySet(),
    val selectedWilayas   : Set<String> = emptySet(),
    val selectedProcedures: Set<String> = emptySet(),
    val dateFrom          : String?     = null,
    val dateTo            : String?     = null
)

fun <T> Set<T>.toggle(item: T): Set<T> =
    if (contains(item)) this - item else this + item

// ─── Derive filter options from live data ─────────────────────────────────────
fun deriveFilterOptions(tenders: List<Tender>) = Triple(
    tenders.map { it.secteurActivite }.distinct().sorted(),
    tenders.map { it.statut          }.distinct().sorted(),
    tenders.map { it.wilaya          }.distinct().sorted()
)

// ─── Date helpers ─────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
private val isoFormatter   = DateTimeFormatter.ofPattern("yyyy-MM-dd")
@RequiresApi(Build.VERSION_CODES.O)
private val labelFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@RequiresApi(Build.VERSION_CODES.O)
private fun millisToIso(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.of("UTC"))
        .format(isoFormatter)

@RequiresApi(Build.VERSION_CODES.O)
private fun isoToLabel(iso: String): String = runCatching {
    java.time.LocalDate.parse(iso, isoFormatter).format(labelFormatter)
}.getOrDefault(iso)

// ─── Main composable ──────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedFilterScreen(
    localizedContext : Context?  = null,
    tenders          : List<Tender> = emptyList(),
    filterState      : FilterState  = FilterState(),
    localeTag        : String       = "",
    onApply          : (FilterState) -> Unit = {},
    onDismiss        : () -> Unit            = {}
) {
    var state by remember { mutableStateOf(filterState) }

    val (dynamicSectors, dynamicStatuses, dynamicWilayas) = remember(tenders) {
        deriveFilterOptions(tenders)
    }

    val fallbackSectors  = listOf("Construction", "IT & Digital", "Health", "Education", "Energy", "Transport")
    val fallbackStatuses = listOf("PUBLIE", "ANNULE", "EN_EVALUATION", "ATTRIBUE")
    val fallbackWilayas  = listOf(
        "Alger", "Oran", "Constantine", "Annaba", "Blida", "Batna",
        "Sétif", "Sidi Bel Abbès", "Biskra", "Tébessa", "Tlemcen",
        "Béjaïa", "Médéa", "Mostaganem", "Ouargla", "Tizi Ouzou"
    )

    val sectors  = if (dynamicSectors.isNotEmpty())  dynamicSectors  else fallbackSectors
    val statuses = if (dynamicStatuses.isNotEmpty()) dynamicStatuses else fallbackStatuses
    val wilayas  = if (dynamicWilayas.isNotEmpty())  dynamicWilayas  else fallbackWilayas

    var wilayaQuery by remember { mutableStateOf("") }
    val filteredWilayas = remember(wilayaQuery, wilayas) {
        if (wilayaQuery.isBlank()) wilayas
        else wilayas.filter { it.contains(wilayaQuery, ignoreCase = true) }
    }

    // ── Date picker visibility ────────────────────────────────────────────────
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker   by remember { mutableStateOf(false) }

    // ── Date range validation ─────────────────────────────────────────────────
    val dateRangeError = remember(state.dateFrom, state.dateTo) {
        if (state.dateFrom != null && state.dateTo != null && state.dateFrom!! > state.dateTo!!)
            "'From' date must be before 'To' date"
        else null
    }

    // ── From date picker ──────────────────────────────────────────────────────
    if (showFromPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        state = state.copy(dateFrom = millisToIso(millis))
                    }
                    showFromPicker = false
                }) { Text("OK", color = Green500) }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) {
                    Text("Cancel", color = SlateGrey)
                }
            }
        ) {
            DatePicker(
                state  = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor  = Green500,
                    todayDateBorderColor       = Green500,
                    selectedDayContentColor    = White,
                    selectedYearContainerColor = Green500,
                    selectedYearContentColor   = White,
                    currentYearContentColor    = Green500
                )
            )
        }
    }

    // ── To date picker ────────────────────────────────────────────────────────
    if (showToPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        state = state.copy(dateTo = millisToIso(millis))
                    }
                    showToPicker = false
                }) { Text("OK", color = Green500) }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) {
                    Text("Cancel", color = SlateGrey)
                }
            }
        ) {
            DatePicker(
                state  = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor  = Green500,
                    todayDateBorderColor       = Green500,
                    selectedDayContentColor    = White,
                    selectedYearContainerColor = Green500,
                    selectedYearContentColor   = White,
                    currentYearContentColor    = Green500
                )
            )
        }
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────
    Scaffold(containerColor = Grey50) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Grey50)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Category,
                    contentDescription = null,
                    tint               = NavyDark,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text       = "Filters",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = NavyDark,
                    modifier   = Modifier.weight(1f)
                )
                // Reset: applies empty filter immediately then dismisses
                Text(
                    text     = "Reset",
                    color    = Green500,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable {
                            onApply(FilterState())   // push the cleared filter right away
                            onDismiss()              // go back — no need to tap Apply
                        }
                        .padding(end = 12.dp)
                )
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = "Close",
                    tint               = NavyDark,
                    modifier           = Modifier
                        .size(22.dp)
                        .clickable { onDismiss() }
                )
            }

            // ── Body ──────────────────────────────────────────────────────────
            LazyColumn(
                modifier       = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                // ── Sectors ───────────────────────────────────────────────────
                item {
                    FilterSection(title = "Sectors") {
                        ChipGroup(
                            items    = sectors,
                            selected = state.selectedSectors,
                            onToggle = { state = state.copy(selectedSectors = state.selectedSectors.toggle(it)) }
                        )
                    }
                    HorizontalDivider(color = DividerGrey)
                }

                // ── Status ────────────────────────────────────────────────────
                item {
                    FilterSection(title = "Status") {
                        ChipGroup(
                            items    = statuses,
                            selected = state.selectedStatuses,
                            onToggle = { state = state.copy(selectedStatuses = state.selectedStatuses.toggle(it)) }
                        )
                    }
                    HorizontalDivider(color = DividerGrey)
                }

                // ── Wilaya ────────────────────────────────────────────────────
                item {
                    FilterSection(title = "Wilaya") {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GreyBg)
                                .border(1.dp, BorderGrey, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, null, tint = SlateGrey, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value         = wilayaQuery,
                                onValueChange = { wilayaQuery = it },
                                singleLine    = true,
                                cursorBrush   = SolidColor(Green500),
                                textStyle     = TextStyle(color = NavyDark, fontSize = 14.sp),
                                modifier      = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (wilayaQuery.isEmpty()) {
                                        Text("Search wilaya…", color = SlateGrey, fontSize = 14.sp)
                                    }
                                    inner()
                                }
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        ChipGroup(
                            items    = filteredWilayas,
                            selected = state.selectedWilayas,
                            onToggle = { state = state.copy(selectedWilayas = state.selectedWilayas.toggle(it)) }
                        )
                    }
                    HorizontalDivider(color = DividerGrey)
                }

                // ── Date range ────────────────────────────────────────────────
                item {
                    FilterSection(title = "Date Range") {

                        // ── From / To fields ──────────────────────────────────
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DatePickerField(
                                label    = "From",
                                value    = state.dateFrom,
                                modifier = Modifier.weight(1f),
                                hasError = dateRangeError != null,
                                onClear  = { state = state.copy(dateFrom = null) },
                                onClick  = { showFromPicker = true }
                            )
                            DatePickerField(
                                label    = "To",
                                value    = state.dateTo,
                                modifier = Modifier.weight(1f),
                                hasError = dateRangeError != null,
                                onClear  = { state = state.copy(dateTo = null) },
                                onClick  = { showToPicker = true }
                            )
                        }

                        // ── Validation error ──────────────────────────────────
                        if (dateRangeError != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(text = dateRangeError, color = ErrorRed, fontSize = 12.sp)
                        }

                        // ── Range summary ─────────────────────────────────────
                        if (state.dateFrom != null || state.dateTo != null) {
                            Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Green100)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint               = Green500,
                                    modifier           = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = buildString {
                                        append(state.dateFrom?.let { isoToLabel(it) } ?: "Any")
                                        append("  →  ")
                                        append(state.dateTo?.let { isoToLabel(it) } ?: "Any")
                                    },
                                    color      = Green500,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // ── Apply button ──────────────────────────────────────────────────
            Surface(shadowElevation = 8.dp, color = White) {
                Button(
                    onClick  = { if (dateRangeError == null) onApply(state) },
                    enabled  = dateRangeError == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape  = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = Green500,
                        disabledContainerColor = Green500.copy(alpha = 0.4f)
                    )
                ) {
                    Text("Apply Filters", color = White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    }
}

// ─── Date picker field ────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DatePickerField(
    label    : String,
    value    : String?,
    modifier : Modifier = Modifier,
    hasError : Boolean  = false,
    onClear  : () -> Unit,
    onClick  : () -> Unit
) {
    val isSet       = value != null
    val borderColor = when {
        hasError -> ErrorRed
        isSet    -> Green500
        else     -> BorderGrey
    }
    val bgColor = when {
        hasError -> ErrorRed.copy(alpha = 0.05f)
        isSet    -> Green500.copy(alpha = 0.07f)
        else     -> GreyBg
    }

    Column(modifier = modifier) {

        // ── Label ─────────────────────────────────────────────────────────────
        Text(
            text          = label,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = if (isSet) Green500 else SlateGrey,
            letterSpacing = 0.5.sp,
            modifier      = Modifier.padding(bottom = 4.dp)
        )

        // ── Tappable field ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector        = Icons.Default.DateRange,
                    contentDescription = null,
                    tint               = if (isSet) Green500 else SlateGrey,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = if (isSet) isoToLabel(value!!) else "Select date",
                    color      = if (isSet) NavyDark else SlateGrey,
                    fontSize   = 13.sp,
                    fontWeight = if (isSet) FontWeight.Medium else FontWeight.Normal,
                    maxLines   = 1
                )
            }

            // ── Clear X ───────────────────────────────────────────────────────
            if (isSet) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint               = SlateGrey,
                    modifier           = Modifier
                        .size(15.dp)
                        .clickable(onClick = onClear)
                )
            }
        }
    }
}

// ─── Section wrapper ──────────────────────────────────────────────────────────
@Composable
private fun FilterSection(
    title   : String,
    content : @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text          = title.uppercase(),
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 11.sp,
            color         = SlateGrey,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

// ─── Chip group ───────────────────────────────────────────────────────────────
@Composable
private fun ChipGroup(
    items    : List<String>,
    selected : Set<String>,
    onToggle : (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    FilterChip(
                        label    = item,
                        selected = item in selected,
                        onClick  = { onToggle(item) }
                    )
                }
            }
        }
    }
}

// ─── Single chip ──────────────────────────────────────────────────────────────
@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Green500 else GreyBg)
            .border(1.dp, if (selected) Green500 else BorderGrey, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text       = label,
            color      = if (selected) White else SlateGrey,
            fontSize   = 13.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            maxLines   = 1
        )
    }
}
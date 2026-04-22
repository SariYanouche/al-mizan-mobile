package com.klodit.almizan.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klodit.almizan.R
import com.klodit.almizan.model.tender.Tender
import com.klodit.almizan.ui.theme.*
import com.klodit.almizan.viewmodel.HomeStats
import com.klodit.almizan.viewmodel.HomeUiState
import com.klodit.almizan.viewmodel.HomeViewModel
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

// ─── Color Palette (Grey-based, no blue) ──────────────────────────────────────
private val NavyDark    = Color(0xFF364150)
private val SlateGrey   = Color(0xFF475569)
private val BorderGrey  = Color(0xFFE2E8F0)
private val GreyBg      = Color(0xFFF8FAFC)

// ─── Entry point ──────────────────────────────────────────────────────────────

// ─── Data class for home search ───────────────────────────────────────────────
data class HomeSearchParams(
    val query: String = "",
    val sector: String = "",
    val wilaya: String = ""
)

@Composable
fun HomeScreen(
    innerPadding           : PaddingValues,
    onNavigateToDetail     : (String) -> Unit = {},
    onNavigateToTenderList : () -> Unit       = {},
    onSearchTenders        : (HomeSearchParams) -> Unit = {},
    onNavigateToStatistics : () -> Unit       = {},
    //allTenders             : List<Tender>     = emptyList(),
    allTenders: List<com.klodit.almizan.model.tender.Tender> = emptyList(),
    viewModel              : HomeViewModel    = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var searchQuery  by remember { mutableStateOf("") }
    var selectedSector by remember { mutableStateOf("") }
    var selectedWilaya by remember { mutableStateOf("") }

    // Derive available sectors from tenders
    val availableSectors = remember(allTenders) {
        allTenders.map { it.secteurActivite }.distinct().sorted()
    }

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(Navy50)
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            HeroSection(
                searchQuery     = searchQuery,
                onSearchChange  = { searchQuery = it },
                selectedSector  = selectedSector,
                onSectorChange  = { selectedSector = it },
                availableSectors = availableSectors,
                selectedWilaya  = selectedWilaya,
                onWilayaChange  = { selectedWilaya = it },
                onSearch        = {
                    onSearchTenders(
                        HomeSearchParams(
                            query = searchQuery,
                            sector = selectedSector,
                            wilaya = selectedWilaya
                        )
                    )
                }
            )
        }

        item {
            when (uiState) {
                is HomeUiState.Loading ->
                    StatsBar(stats = null, isLoading = true)
                is HomeUiState.Success ->
                    StatsBar(stats = (uiState as HomeUiState.Success).stats, isLoading = false)
                is HomeUiState.Error ->
                    StatsBar(stats = null, isLoading = false)
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            ExplorePlatformSection(onNavigateToStatistics = onNavigateToStatistics)
        }

        item {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.home_latest_tenders),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Navy900
                )
                TextButton(onClick = onNavigateToTenderList) {
                    Text(
                        stringResource(R.string.home_view_all),
                        style = MaterialTheme.typography.titleSmall,
                        color = Green500
                    )
                }
            }
        }

        when (uiState) {
            is HomeUiState.Loading -> {
                items(3) { TenderCardSkeleton() }
            }
            is HomeUiState.Success -> {
                val tenders = (uiState as HomeUiState.Success).latestTenders
                if (tenders.isEmpty()) {
                    item { EmptyTendersPlaceholder() }
                } else {
                    items(tenders, key = { it.id }) { tender ->
                        HomeTenderCard(
                            tender   = tender,
                            onClick  = { onNavigateToDetail(tender.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            is HomeUiState.Error -> {
                item {
                    ErrorCard(
                        message = (uiState as HomeUiState.Error).message,
                        onRetry = { viewModel.loadHomeData() }
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
            PillarsSection()
        }
    }
}

// ─── Hero Section ─────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(
    searchQuery      : String,
    onSearchChange   : (String) -> Unit,
    selectedSector   : String,
    onSectorChange   : (String) -> Unit,
    availableSectors : List<String> = emptyList(),
    selectedWilaya   : String,
    onWilayaChange   : (String) -> Unit,
    onSearch         : () -> Unit
) {
    var showSectorDropdown by remember { mutableStateOf(false) }

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(NavyWhite)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = Navy900)) {
                    append(stringResource(R.string.home_hero_prefix))
                }
                withStyle(SpanStyle(color = Green500)) {
                    append(stringResource(R.string.home_hero_highlight))
                }
                withStyle(SpanStyle(color = Navy900)) {
                    append(stringResource(R.string.home_hero_suffix))
                }
            },
            style     = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            stringResource(R.string.home_hero_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Navy600,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        // ── Search input ──────────────────────────────────────────────────────
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            placeholder   = {
                Text(
                    stringResource(R.string.home_search_placeholder),
                    color = Navy500,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Navy500)
            },
            modifier   = Modifier.fillMaxWidth(),
            shape      = RoundedCornerShape(12.dp),
            singleLine = true,
            colors     = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = NavyWhite,
                focusedContainerColor   = NavyWhite,
                unfocusedBorderColor    = BorderGrey,
                focusedBorderColor      = Navy800,
                cursorColor             = Navy800
            )
        )

        Spacer(Modifier.height(12.dp))

        Spacer(Modifier.height(12.dp))

// ── Sector + Wilaya side by side ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sector dropdown
            Box(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreyBg)
                        .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                        .clickable { showSectorDropdown = !showSectorDropdown }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (selectedSector.isNotEmpty()) selectedSector
                            else stringResource(R.string.home_filter_sector_hint),
                            color = if (selectedSector.isNotEmpty()) NavyDark else SlateGrey,
                            fontSize = 14.sp,
                            fontWeight = if (selectedSector.isNotEmpty()) FontWeight.Medium else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = SlateGrey, modifier = Modifier.size(20.dp))
                    }
                }

                DropdownMenu(
                    expanded = showSectorDropdown,
                    onDismissRequest = { showSectorDropdown = false },
                    modifier = Modifier.background(NavyWhite)
                ) {
                    if (availableSectors.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No sectors available", color = SlateGrey, fontSize = 13.sp) },
                            onClick = { showSectorDropdown = false }
                        )
                    } else {
                        availableSectors.forEach { sector ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sector,
                                        color = if (sector == selectedSector) Green500 else NavyDark,
                                        fontWeight = if (sector == selectedSector) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onSectorChange(sector)
                                    showSectorDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Wilaya input
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreyBg)
                    .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = SlateGrey, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                BasicTextField(
                    value = selectedWilaya,
                    onValueChange = onWilayaChange,
                    singleLine = true,
                    cursorBrush = SolidColor(Green500),
                    textStyle = TextStyle(color = NavyDark, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (selectedWilaya.isEmpty()) {
                            Text(stringResource(R.string.home_filter_wilaya_hint), color = SlateGrey, fontSize = 14.sp)
                        }
                        inner()
                    }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Search Button ─────────────────────────────────────────────────────
        Button(
            onClick  = onSearch,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Green500)
        ) {
            Icon(Icons.Default.Search, null, tint = NavyWhite, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.home_search_btn),
                style = MaterialTheme.typography.labelLarge,
                color = NavyWhite,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

// ─── Stats Bar ────────────────────────────────────────────────────────────────

@Composable
private fun StatsBar(stats: HomeStats?, isLoading: Boolean) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(Green500)
            .padding(vertical = 18.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        StatItem(
            value = if (isLoading) "—" else formatStatNumber(stats?.activeTenders ?: 0),
            label = stringResource(R.string.home_stat_active_tenders),
            icon  = Icons.Outlined.Description
        )
        StatDivider()
        StatItem(
            value = if (isLoading) "—" else formatStatNumber(stats?.awarded ?: 0),
            label = stringResource(R.string.home_stat_awarded),
            icon  = Icons.Outlined.CheckCircle
        )
        StatDivider()
        StatItem(
            value = if (isLoading) "—" else formatStatNumber(stats?.total ?: 0),
            label = stringResource(R.string.home_stat_operators),
            icon  = Icons.Outlined.Groups
        )
    }
}

private fun formatStatNumber(n: Int): String =
    if (n >= 1000) "${n / 1000},${(n % 1000).toString().padStart(3, '0')}" else "$n"

@Composable
private fun StatItem(value: String, label: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = NavyWhite, modifier = Modifier.size(22.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = NavyWhite, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = NavyWhite.copy(alpha = 0.85f))
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(44.dp)
            .width(1.dp)
            .background(NavyWhite.copy(alpha = 0.25f))
    )
}

// ─── Explore Platform ─────────────────────────────────────────────────────────

private data class PlatformShortcut(val icon: ImageVector, val titleRes: Int, val subtitleRes: Int)

@Composable
private fun ExplorePlatformSection(onNavigateToStatistics: () -> Unit) {
    val shortcuts = listOf(
        PlatformShortcut(Icons.Outlined.BarChart,     R.string.home_shortcut_stats_title,  R.string.home_shortcut_stats_sub),
        PlatformShortcut(Icons.Outlined.Gavel,        R.string.home_shortcut_legal_title,  R.string.home_shortcut_legal_sub),
        PlatformShortcut(Icons.Outlined.SupportAgent, R.string.home_shortcut_help_title,   R.string.home_shortcut_help_sub)
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            stringResource(R.string.home_explore_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Navy900
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            shortcuts.forEachIndexed { index, shortcut ->
                ShortcutCard(
                    shortcut = shortcut,
                    modifier = Modifier.weight(1f),
                    onClick  = if (index == 0) onNavigateToStatistics else {{}}
                )
            }
        }
    }
}

@Composable
private fun ShortcutCard(shortcut: PlatformShortcut, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier  = modifier
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clickable {  onClick() },
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = NavyWhite)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Green50),
                contentAlignment = Alignment.Center
            ) {
                Icon(shortcut.icon, contentDescription = null, tint = Green600, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(shortcut.titleRes),
                style    = MaterialTheme.typography.titleSmall,
                color    = Navy900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                stringResource(shortcut.subtitleRes),
                style    = MaterialTheme.typography.bodySmall,
                color    = Navy500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Home Tender Card ─────────────────────────────────────────────────────────

@Composable
fun HomeTenderCard(
    tender   : Tender,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier
) {
    val daysLeft = remember(tender.dateLimiteSoumission) {
        try {
            val deadline = OffsetDateTime.parse(tender.dateLimiteSoumission)
            ChronoUnit.DAYS.between(OffsetDateTime.now(), deadline).toInt().coerceAtLeast(0)
        } catch (e: Exception) { -1 }
    }

    val deadlineLabel = when {
        daysLeft < 0  -> stringResource(R.string.home_deadline_unknown)
        daysLeft == 0 -> stringResource(R.string.home_deadline_today)
        daysLeft == 1 -> stringResource(R.string.home_deadline_tomorrow)
        else          -> stringResource(R.string.home_deadline_days, daysLeft)
    }

    val deadlineColor = when {
        daysLeft in 0..7  -> Red600
        daysLeft in 8..14 -> Orange400
        else              -> Green600
    }

    val statusColor = when (tender.statut.uppercase()) {
        "PUBLIE"                -> Green500
        "EN_COURS"              -> Green600
        "OUVERTURE_PLIS"        -> Blue700
        "EVALUATION"            -> Orange400
        "ATTRIBUE"              -> Blue800
        "ANNULE", "CLOTURE"     -> Navy500
        else                    -> Navy500
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Navy50)
                ) {
                    Icon(Icons.Outlined.AccountBalance, null, tint = Navy800, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    tender.secteurActivite,
                    style    = MaterialTheme.typography.titleSmall,
                    color    = Navy700,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        tender.statut,
                        style      = MaterialTheme.typography.labelSmall,
                        color      = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(tender.objet, style = MaterialTheme.typography.titleMedium, color = Navy900, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(tender.reference, style = MaterialTheme.typography.bodySmall, color = Navy500)

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = Navy500, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(tender.wilaya, style = MaterialTheme.typography.bodySmall, color = Navy500)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = deadlineColor, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(deadlineLabel, style = MaterialTheme.typography.bodySmall, color = deadlineColor, fontWeight = FontWeight.Medium)
                }
            }

            if (tender.lots.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.home_lots_count, tender.lots.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = Navy500
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Grey100)
            Spacer(Modifier.height(12.dp))

            Button(
                onClick        = onClick,
                colors         = ButtonDefaults.buttonColors(containerColor = statusColor),
                shape          = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                modifier       = Modifier.fillMaxWidth().height(40.dp)
            ) {
                Icon(Icons.Outlined.Visibility, null, tint = NavyWhite, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.home_view_details),
                    style      = MaterialTheme.typography.labelLarge,
                    color      = NavyWhite,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Skeleton loader ──────────────────────────────────────────────────────────

@Composable
private fun TenderCardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SkeletonBox(80.dp, 18.dp)
                SkeletonBox(70.dp, 18.dp)
            }
            Spacer(Modifier.height(12.dp))
            SkeletonBox(220.dp, 15.dp)
            Spacer(Modifier.height(6.dp))
            SkeletonBox(160.dp, 15.dp)
            Spacer(Modifier.height(6.dp))
            SkeletonBox(100.dp, 12.dp)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Grey100)
            Spacer(Modifier.height(12.dp))
            SkeletonBox(400.dp, 40.dp)
        }
    }
}

@Composable
private fun SkeletonBox(width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(Grey200)
    )
}

// ─── Empty / Error ────────────────────────────────────────────────────────────

@Composable
private fun EmptyTendersPlaceholder() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.SearchOff, null, tint = Navy300, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.home_empty_tenders),
            style = MaterialTheme.typography.bodyMedium,
            color = Navy500
        )
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RedNotice)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.ErrorOutline, null, tint = Red600, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Red600, modifier = Modifier.weight(1f))
            TextButton(onClick = onRetry) {
                Text(
                    stringResource(R.string.home_retry),
                    style      = MaterialTheme.typography.titleSmall,
                    color      = Red600,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Pillars Section ──────────────────────────────────────────────────────────

private data class Pillar(val icon: ImageVector, val titleRes: Int, val descRes: Int)

@Composable
private fun PillarsSection() {
    val pillars = listOf(
        Pillar(Icons.Outlined.Visibility, R.string.home_pillar1_title, R.string.home_pillar1_desc),
        Pillar(Icons.Outlined.Lock,       R.string.home_pillar2_title, R.string.home_pillar2_desc),
        Pillar(Icons.Outlined.AutoGraph,  R.string.home_pillar3_title, R.string.home_pillar3_desc)
    )

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(Navy900)
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.home_pillars_title),
            style     = MaterialTheme.typography.headlineSmall,
            color     = NavyWhite,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.home_pillars_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Navy300,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        pillars.forEach { pillar ->
            PillarItem(pillar)
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PillarItem(pillar: Pillar) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier         = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Navy800),
            contentAlignment = Alignment.Center
        ) {
            Icon(pillar.icon, null, tint = Green500, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(pillar.titleRes),
            style     = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp),
            color     = NavyWhite,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(pillar.descRes),
            style     = MaterialTheme.typography.bodyMedium,
            color     = Navy300,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 16.dp)
        )
    }
}
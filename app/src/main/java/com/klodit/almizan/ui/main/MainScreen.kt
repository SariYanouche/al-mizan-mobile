package com.klodit.almizan.ui.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel

import com.klodit.almizan.ui.bidwizard.BidStatusScreen
import com.klodit.almizan.ui.bidwizard.BidWizardScreen
import com.klodit.almizan.ui.bidwizard.EvaluationResultsScreen
import com.klodit.almizan.ui.bidwizard.FileAppealScreen
import com.klodit.almizan.ui.components.AlMizanBottomBar
import com.klodit.almizan.ui.components.BottomNavDestination
import com.klodit.almizan.ui.components.TopBar
import com.klodit.almizan.ui.home.HomeScreen
import com.klodit.almizan.ui.notifications.NotificationScreen
import com.klodit.almizan.ui.profile.ProfileScreen
import com.klodit.almizan.ui.profile.settings.SettingsScreen
import com.klodit.almizan.ui.search.FilterState
import com.klodit.almizan.ui.soumissions.MyBidsScreen
import com.klodit.almizan.ui.tender.TenderListScreen
import com.klodit.almizan.viewmodel.MainViewModel
import com.klodit.almizan.viewmodel.profile.ProfileViewModel
import com.klodit.almizan.ui.theme.AppLanguage
import com.klodit.almizan.viewmodel.notification.NotificationViewModel
import com.klodit.almizan.ui.home.HomeSearchParams
import com.klodit.almizan.ui.statistics.StatisticsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel(),
    activeFilter: FilterState = FilterState(),
    userId: String = "",
    token: String = "",
    allTenders: List<com.klodit.almizan.model.tender.Tender> = emptyList(),
    onNavigateToLogin: () -> Unit = {},
    selectedLang: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateToFilter: () -> Unit = {},
    onNavigateToTenderDetail: (String) -> Unit = {},
    onNavigateToEditProfile: (String) -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToDeleteAccount: (String) -> Unit = {}
) {
    val currentRoute by viewModel.currentRoute.collectAsState()
    val userFirstName by viewModel.userFirstName.collectAsState()
    val userLastName by viewModel.userLastName.collectAsState()
    val isVerified by viewModel.isVerified.collectAsState()
    val tier by viewModel.tier.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    //val language by viewModel.language.collectAsState()

    val showBidWizard by viewModel.showBidWizard.collectAsState()
    val currentBidAppelOffreId by viewModel.currentBidAppelOffreId.collectAsState()
    val showBidStatus by viewModel.showBidStatus.collectAsState()
    val currentStatusSubmissionId by viewModel.currentStatusSubmissionId.collectAsState()
    val showEvaluationResults by viewModel.showEvaluationResults.collectAsState()
    val currentEvalSubmissionId by viewModel.currentEvalSubmissionId.collectAsState()
    val showFileAppeal by viewModel.showFileAppeal.collectAsState()
    val currentAppealSubmissionId by viewModel.currentAppealSubmissionId.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val showNotifications by viewModel.showNotifications.collectAsState()

    // ── Search query state for HomeScreen → TenderListScreen ────────────────

    var tenderSearchQuery by remember { mutableStateOf("") }
    var tenderSearchSector by remember { mutableStateOf("") }
    var tenderSearchWilaya by remember { mutableStateOf("") }

    /*
    val localizedContext = remember(language) {
        val locale = java.util.Locale(language.locale)
        val config = android.content.res.Configuration(
            viewModel.getApplication<android.app.Application>().resources.configuration
        )
        config.setLocale(locale)
        viewModel.getApplication<android.app.Application>().createConfigurationContext(config)
    }*/
    val localizedContext = remember(selectedLang) {
        val locale = java.util.Locale(selectedLang.locale)

        val config = android.content.res.Configuration(
            viewModel.getApplication<android.app.Application>()
                .resources.configuration
        )

        config.setLocale(locale)

        viewModel.getApplication<android.app.Application>()
            .createConfigurationContext(config)
    }


    LaunchedEffect(userId) {
         if (userId.isNotEmpty()) {
             profileViewModel.fetchProfileByUserId(userId, token)
         }
     }


    /*
    LaunchedEffect(userId, currentRoute) {
        if (userId.isNotEmpty() &&
            (profileViewModel.profileUiState.value !is com.klodit.almizan.data.profile.ProfileUiState.Success
                    || currentRoute == BottomNavDestination.Profile.route)
        ) {
            profileViewModel.fetchProfileByUserId(userId, token)
        }
    }*/

    val profileState by profileViewModel.profileUiState.collectAsState()
    LaunchedEffect(profileState) {
        if (profileState is com.klodit.almizan.data.profile.ProfileUiState.Success) {
            val p = (profileState as com.klodit.almizan.data.profile.ProfileUiState.Success).profile
            viewModel.onProfileLoaded(
                userId = p.userId,
                profileId = p.id,
                firstName = p.firstName,
                lastName = p.lastName,
                isVerified = p.isVerified,
                tier = p.tier
            )
        }
    }

    // Après les autres LaunchedEffect
    val notifUnreadCount by notificationViewModel.unreadCount.collectAsState()
    LaunchedEffect(notifUnreadCount) {
        viewModel.setUnreadCount(notifUnreadCount)
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            notificationViewModel.loadForUser(userId)
        }
    }

    // Bid wizard and appeals overlays
    if (showBidWizard && currentBidAppelOffreId != null) {
        BidWizardScreen(
            localizedContext = localizedContext,
            appelOffreId = currentBidAppelOffreId!!,
            onExit = { viewModel.closeBidWizard() },
            onSubmitBid = { /* handled in view model */ }
        )
        return
    }

    if (showBidStatus && currentStatusSubmissionId != null) {
        BidStatusScreen(
            submissionId = currentStatusSubmissionId!!,
            localizedContext = localizedContext,
            onBackClick = { viewModel.closeBidStatus() },
            onContactSupport = { }
        )
        return
    }

    if (showEvaluationResults && currentEvalSubmissionId != null) {
        EvaluationResultsScreen(
            submissionId = currentEvalSubmissionId!!,
            localizedContext = localizedContext,
            onBackClick = { viewModel.closeEvaluationResults() },
            onFileAppeal = {
                viewModel.closeEvaluationResults()
                viewModel.openFileAppeal(currentEvalSubmissionId!!)
            }
        )
        return
    }

    if (showFileAppeal && currentAppealSubmissionId != null) {
        FileAppealScreen(
            submissionId = currentAppealSubmissionId!!,
            localizedContext = localizedContext,
            onBackClick = { viewModel.closeFileAppeal() },
            onSubmitAppeal = { viewModel.closeFileAppeal() }
        )
        return
    }

    if (showSettings) {
        SettingsScreen(
            localizedContext = localizedContext,
            onBackClick = { viewModel.closeSettings() }
        )
        return
    }

    if (showNotifications) {
        NotificationScreen(
            userId    = userId,
            onBack = { viewModel.closeNotifications() },
            viewModel = notificationViewModel
        )
        return
    }

    val showStatistics by viewModel.showStatistics.collectAsState()


    if (showStatistics) {
        StatisticsScreen(onBack = { viewModel.closeStatistics() })
        return
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopBar(
                userFirstName = userFirstName,
                userLastName = userLastName,
                isVerified = isVerified,
                tier = tier,
                unreadCount = unreadCount,
                selectedLang = selectedLang,
                onLanguageChange = onLanguageChange,
                onNotificationsClick = { viewModel.openNotifications() },
                onLogoutClick = {
                    viewModel.onLogout()
                    onNavigateToLogin()
                }
            )
        },
        bottomBar = {
            AlMizanBottomBar(
                currentRoute = currentRoute,
                localizedContext = localizedContext,
                onDestinationSelected = { viewModel.onTabSelected(it) }
            )
        }
    ) { innerPadding ->
        when (currentRoute) {
            BottomNavDestination.Home.route -> HomeScreen(
                innerPadding            = innerPadding,
                onNavigateToDetail      = onNavigateToTenderDetail,
                onNavigateToTenderList  = {
                    // Clear search when using "View All" button
                    tenderSearchQuery = ""
                    viewModel.onTabSelected(BottomNavDestination.Tenders)
                },
                onSearchTenders         = { params ->
                    tenderSearchQuery  = params.query
                    tenderSearchSector = params.sector
                    tenderSearchWilaya = params.wilaya
                    viewModel.onTabSelected(BottomNavDestination.Tenders)
                },
                allTenders = allTenders,
                onNavigateToStatistics  = { viewModel.openStatistics() },

            )

            BottomNavDestination.Tenders.route -> TenderListScreen(
                innerPadding         = innerPadding,
                localizedContext     = localizedContext,
                activeFilter         = activeFilter,
                initialSearchQuery   = tenderSearchQuery,
                initialSector        = tenderSearchSector,
                initialWilaya        = tenderSearchWilaya,
                onNavigateToFilter   = onNavigateToFilter,
                onNavigateToDetail   = onNavigateToTenderDetail
            )

            BottomNavDestination.MyBids.route -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MyBidsScreen(
                    localizedContext = localizedContext,
                    onStartBidWizard = { appelOffreId -> viewModel.openBidWizard(appelOffreId) },
                    onTrackStatus = { submissionId -> viewModel.openBidStatus(submissionId) },
                    onViewResults = { submissionId -> viewModel.openEvaluationResults(submissionId) },
                    onFileAppeal = { submissionId -> viewModel.openFileAppeal(submissionId) }
                )
            }

            BottomNavDestination.Profile.route -> ProfileScreen(
                userId = userId,
                token = token,
                viewModel = profileViewModel,
                innerPadding = innerPadding,
                onNavigateToEdit = onNavigateToEditProfile,
                onNavigateToChangePassword = onNavigateToChangePassword,
                onNavigateToDeleteAccount = onNavigateToDeleteAccount,
                onNavigateToSettings = { viewModel.openSettings() },
                onLogout = {
                    viewModel.onLogout()
                    onNavigateToLogin()
                }
            )

            else -> HomeScreen(innerPadding)
        }
    }
}
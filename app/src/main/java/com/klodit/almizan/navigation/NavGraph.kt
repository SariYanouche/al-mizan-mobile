package com.klodit.almizan.navigation

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.klodit.almizan.ui.auth.*
import com.klodit.almizan.ui.Registration.RegistrationStep1Screen
import com.klodit.almizan.ui.Registration.RegistrationStep2Screen
import com.klodit.almizan.ui.Registration.RegistrationStep3Screen
import com.klodit.almizan.ui.main.MainScreen
import com.klodit.almizan.ui.profile.ChangePasswordScreen
import com.klodit.almizan.ui.profile.DeleteAccountScreen
import com.klodit.almizan.ui.profile.EditProfileScreen
import com.klodit.almizan.ui.search.DetailedFilterScreen
import com.klodit.almizan.ui.search.FilterState
import com.klodit.almizan.ui.theme.AppLanguage
import com.klodit.almizan.util.LocaleHelper
import com.klodit.almizan.viewmodel.tender.TenderViewModel
import com.klodit.almizan.viewmodel.auth.AuthState
import com.klodit.almizan.viewmodel.auth.AuthViewModel
import com.klodit.almizan.viewmodel.profile.ProfileViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import com.klodit.almizan.data.remote.ApiClient
import com.klodit.almizan.data.remote.TokenStorage
import com.klodit.almizan.ui.tender.TenderDetailScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// ─── Route constants ──────────────────────────────────────────────────────────
private object Routes {
    const val LOGIN              = "login"
    const val FORGOT_PASSWORD    = "forgot_password"
    const val SET_NEW_PASSWORD   = "set_new_password"
    const val ACCOUNT_LOCKED     = "account_locked"
    const val REGISTRATION_STEP1 = "registration_step1"
    const val REGISTRATION_STEP2 = "registration_step2"
    const val REGISTRATION_STEP3 = "registration_step3"
    const val TERMS              = "terms"
    const val PRIVACY            = "privacy"
    const val MAIN               = "main"
    const val FILTER             = "filter"
    const val TENDER_DETAIL      = "tender/{tenderId}"
    const val OTP_VERIFY         = "otp/{email}"
}

// ─── Profile route constants ──────────────────────────────────────────────────
object ProfileRoutes {
    const val EDIT_PROFILE    = "profile/edit/{profileId}"
    const val CHANGE_PASSWORD = "profile/change-password"
    const val DELETE_ACCOUNT  = "profile/delete/{profileId}"

    fun editProfile(profileId: String)   = "profile/edit/$profileId"
    fun deleteAccount(profileId: String) = "profile/delete/$profileId"
}

// ─── Nav graph ────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(onAuthSuccess: () -> Unit = {}) {
    val navController                      = rememberNavController()
    val authViewModel: AuthViewModel       = viewModel()
    val tenderViewModel: TenderViewModel   = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val baseContext = LocalContext.current
    val notificationViewModel: com.klodit.almizan.viewmodel.notification.NotificationViewModel = viewModel()

    //remmeber me
    var isCheckingSession by remember { mutableStateOf(true) }
    var shouldNavigateToMain by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedUserId       = TokenStorage.getUserId(baseContext)
        val savedRefreshToken = TokenStorage.getRefreshToken(baseContext)
        if (!savedUserId.isNullOrBlank() && !savedRefreshToken.isNullOrBlank()) {
            authViewModel.tryRestoreSession(
                context   = baseContext,
                onSuccess = {
                    shouldNavigateToMain = true
                    isCheckingSession = false
                },
                onFailure = {
                    isCheckingSession = false
                }
            )
        } else {
            isCheckingSession = false
        }
    }

    if (isCheckingSession) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return@NavGraph
    }



    var selectedLang by remember {
        mutableStateOf(LocaleHelper.currentLanguage(baseContext))
    }

    val localizedContext = remember(selectedLang) {
        LocaleHelper.applyLocale(baseContext, selectedLang)
    }

    val layoutDirection = if (selectedLang == AppLanguage.ARABIC)
        LayoutDirection.Rtl else LayoutDirection.Ltr

    val onLanguageChange: (AppLanguage) -> Unit = { lang ->
        LocaleHelper.setLocale(baseContext, lang)
        selectedLang = lang
    }

    var activeFilter by remember { mutableStateOf(FilterState()) }
    val tenders by tenderViewModel.tenders.collectAsState()

    LaunchedEffect(Unit) { tenderViewModel.fetchTenders() }

    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        pickedUri = uri /*
        if (uri != null) authViewModel.uploadDocument(baseContext, uri) {}*/
    }





    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalLayoutDirection provides layoutDirection
    ) {
        NavHost(navController = navController, startDestination = Routes.LOGIN) {



            // ── Login ─────────────────────────────────────────────────────────
            composable(Routes.LOGIN) {
                LoginScreen(
                    selectedLang     = selectedLang,
                    onLanguageChange = onLanguageChange,
                    authState        = authViewModel.authState,
                    onClearError     = { authViewModel.clearError() },
                    onLoginClick = { email, password, rememberMe ->
                        authViewModel.login(
                            email      = email,
                            password   = password,
                            rememberMe = rememberMe,
                            context    = baseContext,
                            onSuccess  = { _, _ ->
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onLocked = { navController.navigate(Routes.ACCOUNT_LOCKED) }
                        )
                    },
                    onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                    onRegisterClick       = { navController.navigate(Routes.REGISTRATION_STEP1) },
                    onBiometricsClick     = {}
                )
            }

            // ── Forgot password ───────────────────────────────────────────────
            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    selectedLang     = selectedLang,
                    onLanguageChange = onLanguageChange,
                    authState        = authViewModel.authState,
                    onClearError     = { authViewModel.clearError() },
                    onSendClick      = { email ->
                        authViewModel.forgotPassword(email) {
                            navController.navigate("verification/${Uri.encode(email)}")
                        }
                    },
                    onBackClick   = { navController.popBackStack() },
                    onSignInClick = { navController.popBackStack(Routes.LOGIN, false) }
                )
            }

            // ── OTP verification (forgot password) ────────────────────────────
            composable("verification/{email}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                VerificationScreen(
                    selectedLang     = selectedLang,
                    onLanguageChange = onLanguageChange,
                    authState        = authViewModel.authState,
                    onClearError     = { authViewModel.clearError() },
                    onVerifyClick    = { code ->
                        authViewModel.verifyToken(code) {
                            navController.navigate(Routes.SET_NEW_PASSWORD) {
                                popUpTo(Routes.FORGOT_PASSWORD) { inclusive = true }
                            }
                        }
                    },
                    onResendClick = { authViewModel.forgotPassword(email) {} },
                    onLogoutClick = { navController.popBackStack(Routes.LOGIN, false) }
                )
            }

            // ── Set new password ──────────────────────────────────────────────
            composable(Routes.SET_NEW_PASSWORD) {
                SetNewPasswordScreen(
                    selectedLang     = selectedLang,
                    onLanguageChange = onLanguageChange,
                    authState        = authViewModel.authState,
                    onClearError     = { authViewModel.clearError() },
                    onSaveClick      = { code, newPassword ->
                        authViewModel.resetPassword(code, newPassword) {
                            navController.popBackStack(Routes.LOGIN, false)
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ── Account locked ────────────────────────────────────────────────
            composable(Routes.ACCOUNT_LOCKED) {
                AccountLockedScreen(
                    lockDurationSeconds  = 300 + (authViewModel.failedLoginAttempts - 5) * 60,
                    selectedLang         = selectedLang,
                    onLanguageChange     = onLanguageChange,
                    onResetPasswordClick = {
                        authViewModel.resetFailedAttempts()
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    },
                    onTimerExpired = {
                        authViewModel.resetFailedAttempts()
                        navController.popBackStack(Routes.LOGIN, false)
                    },
                    onContactSupport = {}
                )
            }

            // ── Registration step 1 ───────────────────────────────────────────
            composable(Routes.REGISTRATION_STEP1) {
                RegistrationStep1Screen(
                    selectedLang     = selectedLang,
                    onLanguageChange = onLanguageChange,
                    onBackClick      = { navController.popBackStack() },
                    onTermsClick     = { navController.navigate(Routes.TERMS) },
                    onPrivacyClick   = { navController.navigate(Routes.PRIVACY) },
                    onContinueClick  = { orgName, nif, nis, rc, type, role, wilaya, commune, adresse ->
                        authViewModel.saveStep1(orgName, nif, nis, rc, type, role, wilaya, commune, adresse)
                        navController.navigate(Routes.REGISTRATION_STEP2)
                    }
                )
            }

            // ── Registration step 2 ───────────────────────────────────────────
            composable(Routes.REGISTRATION_STEP2) {
                RegistrationStep2Screen(
                    selectedLang     = selectedLang,
                    onLanguageChange = onLanguageChange,
                    onBackClick      = { navController.popBackStack() },
                    onContinueClick  = { phone, email, password, nom, prenom ->
                        authViewModel.saveStep2(phone, email, password, nom, prenom)
                        navController.navigate(Routes.REGISTRATION_STEP3)
                    }
                )
            }

            // ── Registration step 3 ───────────────────────────────────────────
            composable(Routes.REGISTRATION_STEP3) {
                RegistrationStep3Screen(
                    selectedLang       = selectedLang,
                    onLanguageChange   = onLanguageChange,
                    onBackClick        = { authViewModel.clearError(); navController.popBackStack() },
                    authState          = authViewModel.authState,
                    uploadState        = authViewModel.uploadState,
                    onPickFile = {
                        // filePickerLauncher.launch(arrayOf("application/pdf", "image/*"))

                    },
                    onClearError       = { authViewModel.clearError() },
                    onClearUploadError = { authViewModel.clearUploadError() },
                    onSubmitClick      = {
                        if (authViewModel.authState is AuthState.Success) {
                            val email = authViewModel.getRegisteredEmail()
                            navController.navigate("otp/${Uri.encode(email)}") {
                                popUpTo(Routes.REGISTRATION_STEP1) { inclusive = true }
                            }
                        } else {
                            authViewModel.register(selectedLang) { _ -> }
                        }
                    }
                )
            }

            // ── OTP verification (after registration) ─────────────────────────
            composable(Routes.OTP_VERIFY) { backStackEntry ->
                val email = Uri.decode(backStackEntry.arguments?.getString("email") ?: "")

                LaunchedEffect(email) {
                    authViewModel.sendOtp(email) {}
                }

                VerificationScreen(
                    selectedLang     = selectedLang,
                    onLanguageChange = onLanguageChange,
                    authState        = authViewModel.authState,
                    onClearError     = { authViewModel.clearError() },
                    onVerifyClick    = { code ->
                        authViewModel.verifyOtpAndLogin(
                            email    = email,
                            code     = code,
                            onSuccess = { _, _ ->
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    },
                    onResendClick = { authViewModel.sendOtp(email) {} },
                    onLogoutClick = { navController.popBackStack(Routes.LOGIN, false) }
                )
            }
            // ── Main shell ────────────────────────────────────────────────────
            composable(Routes.MAIN) {
                //val currentToken  = authViewModel.authToken ?: ""
                //val currentUserId = authViewModel.currentUserId ?: ""
                val currentToken  by remember { derivedStateOf { authViewModel.authToken ?: "" } }
                val currentUserId by remember { derivedStateOf { authViewModel.currentUserId ?: "" } }
                android.util.Log.d("NAV_DEBUG", "MAIN composed — token=$currentToken userId=$currentUserId")
                android.util.Log.d("NAV_DEBUG", "authViewModel instance = ${authViewModel.hashCode()}")

                android.util.Log.d("AUTH_DEBUG", "currentUserId = ${authViewModel.currentUserId}")
                android.util.Log.d("AUTH_DEBUG", "authToken = ${authViewModel.authToken}")
                MainScreen(
                    allTenders = tenders,
                    profileViewModel           = profileViewModel,
                    notificationViewModel    = notificationViewModel,
                    activeFilter               = activeFilter,
                    userId                     = currentUserId,
                    token                      = currentToken,
                    onNavigateToLogin = {
                        authViewModel.clearSession(baseContext)
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                    },
                    selectedLang = selectedLang,
                    onLanguageChange = onLanguageChange,
                    onNavigateToTenderDetail   = { tenderId ->
                        navController.navigate("tender/$tenderId")
                    },
                    onNavigateToFilter         = { navController.navigate(Routes.FILTER) },
                    onNavigateToEditProfile    = { profileId ->
                        navController.navigate(ProfileRoutes.editProfile(profileId))
                    },
                    onNavigateToChangePassword = {
                        navController.navigate(ProfileRoutes.CHANGE_PASSWORD)
                    },
                    onNavigateToDeleteAccount  = { profileId ->
                        navController.navigate(ProfileRoutes.deleteAccount(profileId))
                    }
                )
            }

            // ── Filter ────────────────────────────────────────────────────────
            composable(Routes.FILTER) {
                DetailedFilterScreen(
                    localizedContext = localizedContext,
                    tenders          = tenders,
                    filterState      = activeFilter,
                    onApply          = { newFilter ->
                        activeFilter = newFilter
                        navController.popBackStack()
                    },
                    onDismiss = { navController.popBackStack() }
                )
            }

            // ── Terms & privacy ───────────────────────────────────────────────
            composable(Routes.TERMS)   { }
            composable(Routes.PRIVACY) { }

            // ── Tender detail ─────────────────────────────────────────────────
            composable(
                route     = Routes.TENDER_DETAIL,
                arguments = listOf(navArgument("tenderId") { type = NavType.StringType })
            ) { backStack ->
                val tenderId = backStack.arguments?.getString("tenderId") ?: return@composable
                TenderDetailScreen(
                    tenderId = tenderId,
                    onBack   = { navController.popBackStack() }
                )
            }

            // ── Edit profile ──────────────────────────────────────────────────
            composable(
                ProfileRoutes.EDIT_PROFILE,
                arguments = listOf(navArgument("profileId") { type = NavType.StringType })
            ) { backStack ->
                val currentToken  = authViewModel.authToken ?: ""
                val currentUserId = authViewModel.currentUserId ?: ""
                val profileId = backStack.arguments?.getString("profileId") ?: return@composable
                EditProfileScreen(
                    profileId = profileId,
                    userId    = currentUserId,
                    token     = currentToken,
                    onBack    = { navController.popBackStack() },
                    viewModel = profileViewModel
                )
            }

            // ── Change password ───────────────────────────────────────────────
            composable(ProfileRoutes.CHANGE_PASSWORD) {
                val currentToken  = authViewModel.authToken ?: ""

                ChangePasswordScreen(

                    token  = currentToken,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Delete account ────────────────────────────────────────────────
            composable(
                ProfileRoutes.DELETE_ACCOUNT,
                arguments = listOf(navArgument("profileId") { type = NavType.StringType })
            ) { backStack ->
                val currentToken  = authViewModel.authToken ?: ""
                val currentUserId = authViewModel.currentUserId ?: ""
                val profileId = backStack.arguments?.getString("profileId") ?: return@composable
                DeleteAccountScreen(
                    profileId = profileId,
                    token     = currentToken,
                    onBack    = { navController.popBackStack() },
                    onDeleted = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    viewModel = profileViewModel
                )
            }
        }


        LaunchedEffect(shouldNavigateToMain) {
            if (shouldNavigateToMain) {
                navController.navigate(Routes.MAIN) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
                shouldNavigateToMain = false
            }
        }
    }
}


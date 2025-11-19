package com.example.umaklabtrack.borrowermodule

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
// --- IMPORTS FOR TOAST LOGIC ---
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.umaklabtrack.adminmodule.HomeAdminPage
import com.example.umaklabtrack.adminmodule.RequestsAdminPage
// --------------------------------------------------
import com.example.umaklabtrack.ui.theme.UMakLabTrackTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.umaklabtrack.preferences.SessionPreferences
// --- IMPORT YOUR DIALOGS ---
import com.example.umaklabtrack.borrowermodule.TermsAndConditionsDialog
import com.example.umaklabtrack.borrowermodule.PrivacyPolicyDialog // <--- ADDED THIS IMPORT
import com.example.umaklabtrack.dataClasses.UserSession

// (All your other screen imports go here)
// ...

// -----------------------

//private val crdtuser= CredentialsValidation()


class MainActivity : ComponentActivity() {
    private lateinit var sessionPrefs: SessionPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        sessionPrefs = SessionPreferences(this)

        super.onCreate(savedInstanceState)
        //crdtuser.clearLoginPrefs(this)
        enableEdgeToEdge()

        lifecycleScope.launch {

            val userLoggedIn = sessionPrefs.isLoggedIn()

            if (userLoggedIn) {
                val user = sessionPrefs.loadSession()
                UserSession.USER_ID=user.userId
                println("✅ User is logged in: ${user.name}, ${user.email}")
            } else {
                println("❌ User is not logged in")
            }
            setContent {
                UMakLabTrackTheme {
                    AppEntry(
                        isLoggedIn = userLoggedIn
                    )

                }
            }
        }
    }

    @Composable
    fun AppEntry(isLoggedIn: Boolean) {
        var showSplash by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            delay(3000)
            showSplash = false
        }

        if (showSplash) {
            SplashScreen()
        } else {
            MainNavigation(startDestination = if (isLoggedIn) "home" else "landing")
        }
    }

    @Composable
    fun MainNavigation(startDestination: String) {

        val navController = rememberNavController()
        var isOffline by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val scope = rememberCoroutineScope()
        var isCheckingNetwork by remember { mutableStateOf(false) }

        val checkNetwork: () -> Boolean = {
            val online = isDeviceOnline(context)
            isOffline = !online
            online
        }

        val onRetryClick: () -> Unit = {
            scope.launch {
                if (isCheckingNetwork) return@launch
                isCheckingNetwork = true
                delay(2000)
                checkNetwork()
                isCheckingNetwork = false
            }
        }

        LaunchedEffect(Unit) {
            checkNetwork()
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: "landing"
        val loggedInRoutes = listOf(
            "home", "catalog", "apparatus", "chemicals", "slides",
            "borrow", "reserve", "loan"
        )
        val isUserLoggedIn = loggedInRoutes.any { currentRoute.startsWith(it) }

        var showInitialSplash by remember { mutableStateOf(true) }
        var showPostVerificationSplash by remember { mutableStateOf(false) }
        var showPostLoginSplash by remember { mutableStateOf(false) }
        var showPostResetSplash by remember { mutableStateOf(false) }
        var verificationSuccess by remember { mutableStateOf(false) }
        var phoneVerified by remember { mutableStateOf(false) }

        // --- ADDED STATE FOR DIALOGS ---
        var showTermsDialog by remember { mutableStateOf(false) }
        var showPrivacyDialog by remember { mutableStateOf(false) } // <--- ADDED THIS STATE

        // 1. State for BorrowScreen
        var borrowItems by remember { mutableStateOf(mapOf<String, Int>()) }
        val onToggleSelectBorrow = { itemName: String ->
            borrowItems = if (borrowItems.containsKey(itemName)) {
                borrowItems - itemName
            } else {
                borrowItems + (itemName to 1)
            }
        }
        val onIncreaseQuantityBorrow = { itemName: String ->
            val currentQty = borrowItems[itemName] ?: 1
            borrowItems = borrowItems + (itemName to currentQty + 1)
        }
        val onDecreaseQuantityBorrow = { itemName: String ->
            val currentQty = borrowItems[itemName] ?: 1
            if (currentQty > 1) {
                borrowItems = borrowItems + (itemName to currentQty - 1)
            } else {
                borrowItems = borrowItems - itemName
            }
        }
        val onRemoveItemBorrow = { itemName: String ->
            borrowItems = borrowItems - itemName
        }

        // (State for ReserveScreen - no changes)
        var reservationItems by remember { mutableStateOf(mapOf<String, Int>()) }
        val onToggleSelectReserve = { itemName: String ->
            reservationItems = if (reservationItems.containsKey(itemName)) {
                reservationItems - itemName
            } else {
                reservationItems + (itemName to 1)
            }
        }
        val onIncreaseQuantityReserve = { itemName: String ->
            val currentQty = reservationItems[itemName] ?: 1
            reservationItems = reservationItems + (itemName to currentQty + 1)
        }
        val onDecreaseQuantityReserve = { itemName: String ->
            val currentQty = reservationItems[itemName] ?: 1
            if (currentQty > 1) {
                reservationItems = reservationItems + (itemName to currentQty - 1)
            } else {
                reservationItems = reservationItems - itemName
            }
        }
        val onRemoveItemReserve = { itemName: String ->
            reservationItems = reservationItems - itemName
        }

        // (State for LoanScreen - no changes)
        var loanItems by remember { mutableStateOf(mapOf<String, Int>()) }
        val onToggleSelectLoan = { itemName: String ->
            loanItems = if (loanItems.containsKey(itemName)) {
                loanItems - itemName
            } else {
                loanItems + (itemName to 1)
            }
        }
        val onIncreaseQuantityLoan = { itemName: String ->
            val currentQty = loanItems[itemName] ?: 1
            loanItems = loanItems + (itemName to currentQty + 1)
        }
        val onDecreaseQuantityLoan = { itemName: String ->
            val currentQty = loanItems[itemName] ?: 1
            if (currentQty > 1) {
                loanItems = loanItems + (itemName to currentQty - 1)
            } else {
                loanItems = loanItems - itemName
            }
        }
        val onRemoveItemLoan = { itemName: String ->
            loanItems = loanItems - itemName
        }

        LaunchedEffect(Unit) {
            delay(3000)
            showInitialSplash = false
        }

        if (isOffline) {
            if (isUserLoggedIn) {
                OfflineHomePage(
                    currentRoute = currentRoute.split("/").first(),
                    onRetry = onRetryClick,
                    onNavSelected = { /* Do nothing */ },
                    isChecking = isCheckingNetwork
                )
            } else {
                OfflineLandingPage(
                    onRetry = onRetryClick,
                    isChecking = isCheckingNetwork
                )
            }

        } else {
            // We are ONLINE. Show the app as normal.
            if (verificationSuccess) {
                LaunchedEffect(Unit) {
                    showPostVerificationSplash = true
                    delay(2000)
                    showPostVerificationSplash = false
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                    verificationSuccess = false
                }
            }

            if (phoneVerified) {
                LaunchedEffect(Unit) {
                    showPostVerificationSplash = true
                    delay(2000)
                    showPostVerificationSplash = false
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                    phoneVerified = false
                }
            }

            if (showPostLoginSplash) {
                LaunchedEffect(Unit) {
                    delay(2000)
                    showPostLoginSplash = false
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            if (showPostResetSplash) {
                LaunchedEffect(Unit) {
                    delay(2000)
                    showPostResetSplash = false
                    navController.navigate("landing") {
                        popUpTo("forgot_password") { inclusive = true }
                    }
                }
            }


            if (showInitialSplash || showPostVerificationSplash || showPostLoginSplash || showPostResetSplash) {
                SplashScreen()
            } else {
                NavHost(navController = navController, startDestination = startDestination) {

                    composable("landing") {
                        LandingPage(
                            onJoinClick = {
                                if (checkNetwork()) navController.navigate("signup")
                            },
                            onLoginClick = {
                                if (checkNetwork()) navController.navigate("login")
                            }
                        )
                    }

                    // --- MODIFIED SIGNUP ROUTE ---
                    composable("signup") {
                        SignUpScreen(
                            onBackClick = { navController.popBackStack() },
                            onSignUpSuccess = {
                                if (checkNetwork()) {
                                    // Show the Terms dialog INSTEAD of navigating directly
                                    showTermsDialog = true
                                }
                            },
                            onLoginClick = { navController.navigate("login") }
                        )
                    }

                    composable("signup_verification") {
                        SignUpVerificationScreen(
                            onExitClick = {
                                navController.popBackStack("landing", inclusive = false)
                            },
                            onVerificationSuccess = { verificationSuccess = true },
                            showInitialToast = true
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            onBackClick = { navController.popBackStack() },
                            onLoginSuccess = {
                                if (checkNetwork()) showPostLoginSplash = true
                            },
                            onAdminLoginSuccess = {
                                // Navigate to the admin home page
                                navController.navigate("admin_home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onSignUpClick = { navController.navigate("signup") },
                            onForgotPasswordClick = { navController.navigate("forgot_password_confirmation") }
                        )
                    }



                    composable("admin_home") {

                        val context = LocalContext.current
                        val sessionPrefs = remember { SessionPreferences(context) }

                        var adminName by remember { mutableStateOf("Admin") }

                        LaunchedEffect(Unit) {
                            val user = sessionPrefs.loadSession()
                            adminName = user.name ?: "Admin"
                        }

                        HomeAdminPage(
                            adminName = adminName,
                            onNavSelected = { route ->
                                navController.navigate("admin_$route") {
                                    popUpTo("admin_home") { inclusive = false }
                                }
                            }
                        )

                    }
                    composable("admin_requests") {
                        RequestsAdminPage(onNavSelected = { route ->
                            navController.navigate("admin_$route") {
                                popUpTo("admin_home") { inclusive = false }
                            }
                        }) // Corrected: only passing the valid 'onNavSelected' parameter
                    }

                    composable("home") {
                        val context = LocalContext.current
                        val sessionPrefs = remember { SessionPreferences(context) }
                        var firstName by remember { mutableStateOf("User") }
                        val homeViewModel: HomeViewModel = viewModel()

                        LaunchedEffect(Unit) {
                            val user = sessionPrefs.loadSession()
                            firstName = user.name?.split(" ")?.firstOrNull() ?: "User"
                        }


                        HomePage(
                            userName = firstName,
                            onNavSelected = { route ->
                                if (checkNetwork()) navController.navigate(route)
                            },
                            viewModel = homeViewModel

                        )
                    }


                    composable("catalog") {

                        val homeViewModel: HomeViewModel = viewModel()

                        CatalogScreen(

                            onNavSelected = { route ->
                                if (checkNetwork()) navController.navigate(route)
                            },
                            onApparatusClicked = {
                                if (checkNetwork()) navController.navigate("apparatus")
                            },
                            onChemicalsClicked = {
                                if (checkNetwork()) navController.navigate("chemicals")
                            },
                            onSlidesClicked = {
                                if (checkNetwork()) navController.navigate("slides")
                            },
                            viewModel = homeViewModel
                        )
                    }

                    composable("apparatus") {
                        ApparatusScreen(
                            onNavSelected = { route ->
                                if (checkNetwork()) navController.navigate(
                                    route
                                )
                            },
                            onBackClicked = { navController.popBackStack() }
                        )
                    }

                    composable("chemicals") {
                        ChemicalScreen(
                            onNavSelected = { route ->
                                if (checkNetwork()) navController.navigate(
                                    route
                                )
                            },
                            onBackClicked = { navController.popBackStack() }
                        )
                    }

                    composable("slides") {
                        SlidesScreen(
                            onNavSelected = { route ->
                                if (checkNetwork()) navController.navigate(
                                    route
                                )
                            },
                            onBackClicked = { navController.popBackStack() }
                        )
                    }

                    composable("borrow/{category}") { backStackEntry ->
                        val category = backStackEntry.arguments?.getString("category") ?: "all"

                        BorrowScreen(
                            category = category,
                            selectedItems = borrowItems,
                            onToggleSelect = onToggleSelectBorrow,
                            onIncreaseQuantity = onIncreaseQuantityBorrow,
                            onDecreaseQuantity = onDecreaseQuantityBorrow,
                            onRemoveItem = onRemoveItemBorrow,
                            onNavSelected = { route ->
                                if (checkNetwork()) navController.navigate(route)
                            },
                            onBackClicked = { navController.popBackStack() },
                            onViewSelectedClicked = {
                                if (checkNetwork()) { /* TODO: Navigate to borrow summary screen */
                                }
                            }
                        )
                    }

                    composable("reserve/{category}") { backStackEntry ->
                        val category = backStackEntry.arguments?.getString("category") ?: "all"

                        ReserveScreen(
                            category = category,
                            selectedItems = reservationItems,
                            onToggleSelect = onToggleSelectReserve,
                            onIncreaseQuantity = onIncreaseQuantityReserve,
                            onDecreaseQuantity = onDecreaseQuantityReserve,
                            onRemoveItem = onRemoveItemReserve,
                            onNavSelected = { route ->
                                if (checkNetwork()) navController.navigate(
                                    route
                                )
                            },
                            onBackClicked = { navController.popBackStack() },
                            onViewSelectedClicked = {
                                if (checkNetwork()) { /* TODO: navigate to reserve summary */
                                }
                            }
                        )
                    }

                    composable("loan/{category}") { backStackEntry ->
                        val category = backStackEntry.arguments?.getString("category") ?: "all"

                        LoanScreen(
                            category = category,
                            selectedItems = loanItems,
                            onToggleSelect = onToggleSelectLoan,
                            onIncreaseQuantity = onIncreaseQuantityLoan,
                            onDecreaseQuantity = onDecreaseQuantityLoan,
                            onRemoveItem = onRemoveItemLoan,
                            onNavSelected = { route ->
                                if (checkNetwork()) navController.navigate(
                                    route
                                )
                            },
                            onBackClicked = { navController.popBackStack() },
                            onViewSelectedClicked = {
                                if (checkNetwork()) { /* TODO: navigate to loan summary */
                                }
                            }
                        )
                    }

                    // --- LOGS ROUTE ---
                    composable(
                        route = "logs?showToast={showToast}",
                        arguments = listOf(
                            navArgument("showToast") {
                                defaultValue = false
                                type = NavType.BoolType
                            }
                        )
                    ) { backStackEntry ->
                        val showToastArg = backStackEntry.arguments?.getBoolean("showToast") ?: false
                        ActivityLogsScreen(
                            showToast = showToastArg,
                            onNavSelected = { route -> if (checkNetwork()) navController.navigate(route) }
                        )
                    }

                    // ... inside NavHost, after the "logs" route ...

                    composable("profile") {
                        // 1. Get the context and session
                        val context = LocalContext.current
                        val sessionPrefs = remember { SessionPreferences(context) }

                        // 2. Create a CoroutineScope to handle the suspend function
                        val scope = rememberCoroutineScope()

                        ProfileScreen(
                            onNavSelected = { route ->
                                if (checkNetwork()) navController.navigate(route)
                            },
                            onLogout = {
                                // 3. Launch a coroutine to clear the session
                                scope.launch {
                                    sessionPrefs.clearSession() // Use the correct function name

                                    // 4. Navigate to Landing and wipe back stack
                                    navController.navigate("landing") {
                                        popUpTo(0) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        )
                    }

                    composable("forgot_password_confirmation") {
                        ForgotPasswordConfirmationScreen(
                            onBackClick = { navController.popBackStack() },
                            onCancelClick = { navController.popBackStack() },
                            onYesClick = {
                                if (checkNetwork()) navController.navigate("forgot_password_verification")
                            }
                        )
                    }

                    composable("forgot_password_verification") {
                        ForgotPasswordVerificationScreen(
                            onExitClick = {
                                navController.popBackStack("login", inclusive = false)
                            },
                            onVerificationSuccess = {
                                if (checkNetwork()) navController.navigate("forgot_password")
                            },
                            showInitialToast = true
                        )
                    }

                    composable("forgot_password") {
                        ForgotPasswordScreen(
                            onBackClick = {
                                navController.popBackStack("login", inclusive = false)
                            },
                            onResetSuccess = {
                                if (checkNetwork()) showPostResetSplash = true
                            }
                        )

                    }
                }

                // --- DIALOG LOGIC (Terms -> Privacy -> Verification) ---

                // 1. TERMS AND CONDITIONS
                if (showTermsDialog) {
                    TermsAndConditionsDialog(
                        onNextClicked = {
                            showTermsDialog = false
                            // Navigate to Privacy Policy next
                            showPrivacyDialog = true
                        },
                        onDismiss = {
                            showTermsDialog = false
                        }
                    )
                }

                // 2. PRIVACY POLICY
                if (showPrivacyDialog) {
                    PrivacyPolicyDialog(
                        onAgreeClicked = {
                            showPrivacyDialog = false
                            // User agreed to both, NOW go to verification
                            navController.navigate("signup_verification")
                        },
                        onDismiss = {
                            // If back/dismiss is allowed, go back to Terms
                            showPrivacyDialog = false
                            showTermsDialog = true
                        }
                    )
                }
            }
        }
    }


    private fun isDeviceOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
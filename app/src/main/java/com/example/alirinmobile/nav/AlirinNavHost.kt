package com.example.alirinmobile.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alirinmobile.feature.WeatherViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.alirinmobile.data.auth.Role
import com.example.alirinmobile.feature.AlirinViewModelFactory
import com.example.alirinmobile.feature.AuthViewModel
import com.example.alirinmobile.feature.LaporViewModel
import com.example.alirinmobile.feature.ReportsViewModel
import com.example.alirinmobile.feature.StaffViewModel
import com.example.alirinmobile.feature.auth.AdminWallScreen
import com.example.alirinmobile.feature.auth.LoginScreen
import com.example.alirinmobile.feature.auth.OnboardingScreen
import com.example.alirinmobile.feature.auth.SplashScreen
import com.example.alirinmobile.feature.auth.WelcomeScreen
import com.example.alirinmobile.feature.beranda.BerandaScreen
import com.example.alirinmobile.feature.lapor.LaporFlowScreen
import com.example.alirinmobile.feature.peta.PetaScreen
import com.example.alirinmobile.feature.staff.PersetujuanDetailScreen
import com.example.alirinmobile.feature.staff.PersetujuanListScreen
import com.example.alirinmobile.feature.staff.StaffProfilScreen
import com.example.alirinmobile.feature.staff.StaffStatistikScreen
import com.example.alirinmobile.feature.staff.StaffTindakLanjutScreen
import com.example.alirinmobile.feature.status.StatusDetailScreen
import com.example.alirinmobile.feature.status.StatusListScreen
import com.example.alirinmobile.feature.tentang.TentangScreen
import com.example.alirinmobile.feature.web.WebViewScreen
import com.example.alirinmobile.ui.components.AlirinIcons
import com.example.alirinmobile.ui.theme.Bg
import com.example.alirinmobile.ui.theme.Radius
import com.example.alirinmobile.ui.theme.Surface

object Routes {

    const val Beranda = "beranda"
    const val Peta = "peta"
    const val Status = "status"
    const val StatusDetail = "status_detail/{reportId}"
    const val Tentang = "tentang"

    const val StaffInbox = "staff_inbox"
    const val StaffInboxDetail = "staff_inbox/{reportId}"
    const val StaffLanjut = "staff_lanjut"
    const val StaffStats = "staff_stats"
    const val StaffProfil = "staff_profil"

    const val Web = "web"

    fun statusDetail(id: String) = "status_detail/$id"
    fun staffInboxDetail(id: String) = "staff_inbox/$id"
}

@Composable
fun AlirinNavHost() {
    val authVm: AuthViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
    val session by authVm.session.collectAsStateWithLifecycle()
    val anonChosen by authVm.anonChosen.collectAsStateWithLifecycle()
    val onboardingDone by authVm.onboardingDone.collectAsStateWithLifecycle()

    var splashDone by remember { mutableStateOf(false) }
    var showStaffLogin by remember { mutableStateOf(false) }

    when {
        !splashDone -> SplashScreen(onDone = { splashDone = true })
        !onboardingDone -> OnboardingScreen(onDone = { authVm.finishOnboarding() })
        // Admin hanya ada di web. Akun admin yang login di sini diarahkan ke
        // halaman penjelasan, bukan diberi antarmuka petugas beserta kewenangan
        // menutup pekerjaan lapangan.
        session?.role == Role.Admin -> AdminWallScreen(
            onLogout = { authVm.logout(); showStaffLogin = false },
        )
        session != null -> MainShell(
            role = Role.Staff,
            onLogout = { authVm.logout(); showStaffLogin = false },
        )
        anonChosen -> MainShell(
            role = Role.Citizen,
            onLogout = { authVm.logout(); showStaffLogin = false },
        )
        showStaffLogin -> LoginScreen(viewModel = authVm, onBack = { showStaffLogin = false })
        else -> WelcomeScreen(
            onWarga = { authVm.chooseAnonymous() },
            onStaff = { showStaffLogin = true },
        )
    }
}

private const val SLIDE_MS = 280
private fun AnimatedContentTransitionScope<*>.iosEnter() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(SLIDE_MS))
private fun AnimatedContentTransitionScope<*>.iosExit() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(SLIDE_MS), targetOffset = { full -> -full / 4 })
private fun AnimatedContentTransitionScope<*>.iosPopEnter() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(SLIDE_MS), initialOffset = { full -> -full / 4 })
private fun AnimatedContentTransitionScope<*>.iosPopExit() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(SLIDE_MS))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(role: Role, onLogout: () -> Unit) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    var showLaporSheet by remember { mutableStateOf(false) }

    val isStaff = role == Role.Staff
    val tabsForRole: List<NavItem> = remember(role) {
        if (isStaff) listOf(
            NavItem("staff_inbox",  AlirinIcons.document, "Inbox"),
            NavItem("staff_lanjut", AlirinIcons.bolt,     "Lanjut"),
            NavItem("peta",         AlirinIcons.map,      "Peta"),
            NavItem("staff_stats",  AlirinIcons.sparkles, "Stats"),
            NavItem("staff_profil", AlirinIcons.users,    "Profil"),
        ) else DefaultNavItems
    }
    val startDestination = if (isStaff) Routes.StaffInbox else Routes.Beranda

    val rootRoutes = if (isStaff) listOf(
        Routes.StaffInbox, Routes.StaffLanjut, Routes.Peta, Routes.StaffStats, Routes.StaffProfil,
    ) else listOf(Routes.Beranda, Routes.Peta, Routes.Status, Routes.Tentang)

    val showBottomBar = current in rootRoutes
    val currentTab = when (current) {
        Routes.Beranda -> "beranda"
        Routes.Peta -> "peta"
        Routes.Status -> "status"
        Routes.Tentang -> "tentang"
        Routes.StaffInbox -> "staff_inbox"
        Routes.StaffLanjut -> "staff_lanjut"
        Routes.StaffStats -> "staff_stats"
        Routes.StaffProfil -> "staff_profil"
        else -> ""
    }

    val reportsVm: ReportsViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
    val reports by reportsVm.reports.collectAsStateWithLifecycle()
    val authVm: AuthViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
    val session by authVm.session.collectAsStateWithLifecycle()

    val weatherVm: WeatherViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) weatherVm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Bg),
        containerColor = Bg,
        bottomBar = {

            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(tween(SLIDE_MS)) { it } + fadeIn(tween(SLIDE_MS)),
                exit = slideOutVertically(tween(SLIDE_MS)) { it } + fadeOut(tween(SLIDE_MS)),
            ) {
                BottomNav(
                    current = currentTab,
                    onSelect = { tabId ->
                        when (tabId) {
                            "lapor" -> showLaporSheet = true
                            else -> {
                                val route = when (tabId) {
                                    "beranda" -> Routes.Beranda
                                    "peta" -> Routes.Peta
                                    "status" -> Routes.Status
                                    "tentang" -> Routes.Tentang
                                    "staff_inbox" -> Routes.StaffInbox
                                    "staff_lanjut" -> Routes.StaffLanjut
                                    "staff_stats" -> Routes.StaffStats
                                    "staff_profil" -> Routes.StaffProfil
                                    else -> startDestination
                                }
                                nav.navigate(route) {
                                    popUpTo(nav.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    },
                    items = tabsForRole,
                )
            }
        },
    ) { padding ->

        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                navController = nav,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { iosEnter() },
                exitTransition = { iosExit() },
                popEnterTransition = { iosPopEnter() },
                popExitTransition = { iosPopExit() },
            ) {

                composable(Routes.Beranda) {
                    val myReports by reportsVm.myReports.collectAsStateWithLifecycle()
                    BerandaScreen(
                        reports = reports,
                        myReports = myReports,
                        session = session,
                        onLaporClick = { showLaporSheet = true },
                        onPetaClick = { nav.navigate(Routes.Peta) },
                        onStatusClick = { nav.navigate(Routes.Status) },
                        onStatusItemClick = { nav.navigate(Routes.statusDetail(it.id)) },
                        onTentangClick = { nav.navigate(Routes.Tentang) },
                        onLogout = onLogout,
                    )
                }
                composable(Routes.Peta) {
                    PetaScreen(
                        onBack = { nav.popBackStack() },
                        onLaporInPlace = { showLaporSheet = true },
                    )
                }
                composable(Routes.Status) {
                    val myReports by reportsVm.myReports.collectAsStateWithLifecycle()
                    StatusListScreen(
                        reports = myReports,
                        onTrackToken = { token -> reportsVm.trackByToken(token) },
                        onBack = { nav.popBackStack() },
                        onReportClick = { nav.navigate(Routes.statusDetail(it.id)) },
                    )
                }
                composable(
                    Routes.StatusDetail,
                    arguments = listOf(navArgument("reportId") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("reportId").orEmpty()
                    val report = reports.find { it.id == id } ?: reports.firstOrNull()
                    if (report != null) StatusDetailScreen(report = report, onBack = { nav.popBackStack() })
                }
                composable(Routes.Tentang) {
                    TentangScreen(
                        onBack = { nav.popBackStack() },
                        onOpenWeb = { nav.navigate(Routes.Web) },
                    )
                }
                composable(Routes.Web) {
                    WebViewScreen(onBack = { nav.popBackStack() })
                }

                composable(Routes.StaffInbox) {
                    val staffVm: StaffViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
                    val queue by staffVm.queue.collectAsStateWithLifecycle()
                    PersetujuanListScreen(
                        reports = queue,
                        actor = session,
                        onReportClick = { nav.navigate(Routes.staffInboxDetail(it.id)) },
                        onLogout = onLogout,
                    )
                }
                composable(
                    Routes.StaffInboxDetail,
                    arguments = listOf(navArgument("reportId") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("reportId").orEmpty()
                    val staffVm: StaffViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
                    val report = reports.find { it.id == id }
                    if (report != null) {
                        PersetujuanDetailScreen(
                            report = report,
                            onBack = { nav.popBackStack() },
                            onTransition = { newStatus, note ->
                                staffVm.transition(report.id, newStatus, note)
                            },
                            onVerifyPhoto = { photoPath, note ->
                                staffVm.verifyWithPhoto(report.id, photoPath, note)
                            },
                        )
                    }
                }
                composable(Routes.StaffLanjut) {
                    val staffVm: StaffViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
                    val myTasks by staffVm.myTasks.collectAsStateWithLifecycle()
                    StaffTindakLanjutScreen(
                        reports = myTasks,
                        onClose = { report ->
                            staffVm.transition(report.id, com.example.alirinmobile.data.ReportStatus.Completed, "Pekerjaan ditutup oleh staff.")
                        },
                    )
                }
                composable(Routes.StaffStats) {
                    StaffStatistikScreen(reports = reports)
                }
                composable(Routes.StaffProfil) {
                    StaffProfilScreen(actor = session, onLogout = onLogout)
                }
            }

            if (showLaporSheet) {
                val laporVm: LaporViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { laporVm.reset(); showLaporSheet = false },
                    sheetState = sheetState,
                    shape = Radius.sheet,
                    containerColor = Surface,
                    dragHandle = null,
                ) {
                    Box(Modifier.fillMaxHeight(0.96f)) {
                        LaporFlowScreen(
                            viewModel = laporVm,
                            onClose = { laporVm.reset(); showLaporSheet = false },
                            onGoToStatus = {
                                laporVm.reset()
                                showLaporSheet = false
                                nav.navigate(Routes.Status) { popUpTo(startDestination) }
                            },
                        )
                    }
                }
            }
        }
    }
}

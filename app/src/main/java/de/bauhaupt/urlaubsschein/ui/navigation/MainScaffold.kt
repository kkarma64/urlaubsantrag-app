package de.bauhaupt.urlaubsschein.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.bauhaupt.urlaubsschein.ui.components.ScreenContainer
import de.bauhaupt.urlaubsschein.ui.screens.AntragDetailScreen
import de.bauhaupt.urlaubsschein.ui.screens.HelpScreen
import de.bauhaupt.urlaubsschein.ui.screens.HomeScreen
import de.bauhaupt.urlaubsschein.ui.screens.KontoKalenderScreen
import de.bauhaupt.urlaubsschein.ui.screens.more.BackupScreen
import de.bauhaupt.urlaubsschein.ui.screens.more.DatenschutzScreen
import de.bauhaupt.urlaubsschein.ui.screens.more.DesignScreen
import de.bauhaupt.urlaubsschein.ui.screens.more.EmailSettingsScreen
import de.bauhaupt.urlaubsschein.ui.screens.more.MoreMenuScreen
import de.bauhaupt.urlaubsschein.ui.screens.more.ProfilScreen
import de.bauhaupt.urlaubsschein.ui.screens.more.SprachauswahlScreen
import de.bauhaupt.urlaubsschein.ui.screens.more.StammdatenScreen
import de.bauhaupt.urlaubsschein.ui.screens.more.UnterschriftScreen
import de.bauhaupt.urlaubsschein.ui.wizard.WizardStep1Screen
import de.bauhaupt.urlaubsschein.ui.wizard.WizardStep2Screen
import de.bauhaupt.urlaubsschein.ui.wizard.WizardStep3Screen
import de.bauhaupt.urlaubsschein.ui.wizard.WizardViewModel

sealed class BottomNavRoute(val route: String, val title: String, val icon: ImageVector) {
    object Antraege : BottomNavRoute("tab_antraege", "Anträge", Icons.Default.CheckCircle)
    object KontoKalender : BottomNavRoute("tab_konto", "Konto & Kalender", Icons.Default.DateRange)
    object Profil : BottomNavRoute("tab_profil", "Profil", Icons.Default.Person)
    object Mehr : BottomNavRoute("tab_mehr", "Mehr", Icons.Default.Settings)
}

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val wizardViewModel: WizardViewModel = viewModel()

    val items = listOf(
        BottomNavRoute.Antraege,
        BottomNavRoute.KontoKalender,
        BottomNavRoute.Profil,
        BottomNavRoute.Mehr
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isProfileDrawingMode by remember { mutableStateOf(false) }
    val showBottomBar = !isProfileDrawingMode

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    items.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                val isWizard = currentRoute?.startsWith("wizard") == true
                                if (isWizard) {
                                    wizardViewModel.resetWizard()
                                }
                                if (!selected || isWizard) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.background,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar && currentRoute == BottomNavRoute.Antraege.route) {
                FloatingActionButton(
                    onClick = { navController.navigate("wizard_step1") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .size(64.dp)
                ) {
                    Text("+", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { _ ->
        ScreenContainer {
            NavHost(
                navController = navController,
                startDestination = BottomNavRoute.Antraege.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(BottomNavRoute.Antraege.route) {
                    HomeScreen(onAntragClick = { id -> navController.navigate("antrag_detail/$id") })
                }
                composable("antrag_detail/{antragId}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("antragId")?.toLongOrNull() ?: 0L
                    AntragDetailScreen(antragId = id, onBack = { navController.popBackStack() })
                }
                composable(BottomNavRoute.KontoKalender.route) {
                    KontoKalenderScreen()
                }
                composable(BottomNavRoute.Profil.route) {
                    val profilNavController = rememberNavController()
                    NavHost(
                        navController = profilNavController,
                        startDestination = "profil_menu"
                    ) {
                        composable("profil_menu") {
                            ProfilScreen(onNavigate = { route -> profilNavController.navigate(route) })
                        }
                        composable("more_stammdaten") {
                            StammdatenScreen(onBack = { profilNavController.popBackStack() })
                        }
                        composable("more_unterschrift") {
                            UnterschriftScreen(onBack = { profilNavController.popBackStack() })
                        }
                        composable("more_email") {
                            EmailSettingsScreen(onBack = { profilNavController.popBackStack() })
                        }
                        composable("more_design") {
                            DesignScreen(onBack = { profilNavController.popBackStack() })
                        }
                    }
                }
                composable(BottomNavRoute.Mehr.route) {
                    val moreNavController = rememberNavController()
                    NavHost(
                        navController = moreNavController,
                        startDestination = "more_menu"
                    ) {
                        composable("more_menu") {
                            MoreMenuScreen(onNavigate = { route -> moreNavController.navigate(route) })
                        }
                        composable("more_backup") {
                            BackupScreen(onBack = { moreNavController.popBackStack() })
                        }
                        composable("more_hilfe") {
                            HelpScreen(onBack = { moreNavController.popBackStack() })
                        }
                        composable("more_datenschutz") {
                            DatenschutzScreen(onBack = { moreNavController.popBackStack() })
                        }
                        composable("more_sprachauswahl") {
                            SprachauswahlScreen(onBack = { moreNavController.popBackStack() })
                        }
                    }
                }
                composable("help_screen") {
                    HelpScreen(onBack = { navController.popBackStack() })
                }

                // Wizard Routes
                composable("wizard_step1") {
                    WizardStep1Screen(
                        viewModel = wizardViewModel,
                        onNextClick = { navController.navigate("wizard_step2") }
                    )
                }
                composable("wizard_step2") {
                    WizardStep2Screen(
                        viewModel = wizardViewModel,
                        onNextClick = { navController.navigate("wizard_step3") },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable("wizard_step3") {
                    WizardStep3Screen(
                        viewModel = wizardViewModel,
                        onSendClick = { signatureFile ->
                            wizardViewModel.submitAntrag(context = navController.context, signatureFile = signatureFile) {
                                wizardViewModel.resetWizard()
                                navController.navigate(BottomNavRoute.Antraege.route) {
                                    popUpTo(BottomNavRoute.Antraege.route) { inclusive = true }
                                }
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

package com.renatizzi.photovideomanager.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.ui.acquire.AcquireScreen
import com.renatizzi.photovideomanager.ui.acquire.AcquireViewModel
import com.renatizzi.photovideomanager.ui.clean.CleanScreen
import com.renatizzi.photovideomanager.ui.clean.CleanViewModel
import com.renatizzi.photovideomanager.ui.config.ArchiveSourcesScreen
import com.renatizzi.photovideomanager.ui.config.ArchiveSourcesViewModel
import com.renatizzi.photovideomanager.ui.config.ConfigScreen
import com.renatizzi.photovideomanager.ui.home.FeatureStubScreen
import com.renatizzi.photovideomanager.ui.home.HomeScreen
import com.renatizzi.photovideomanager.ui.home.HomeViewModel
import com.renatizzi.photovideomanager.ui.home.MacroHubScreen
import com.renatizzi.photovideomanager.ui.home.MacroNavigation
import com.renatizzi.photovideomanager.ui.shell.PvmScaffold
import com.renatizzi.photovideomanager.ui.shell.ShellTab
import com.renatizzi.photovideomanager.ui.theme.PvmTheme
import com.renatizzi.photovideomanager.ui.theme.ThemePreferences

@Composable
fun PvmApp(
    catalogFacade: CatalogFacade,
    themePreferences: ThemePreferences,
    darkThemeOverride: Boolean? = null,
) {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by remember {
        mutableStateOf(darkThemeOverride ?: themePreferences.storedDarkMode() ?: systemDark)
    }
    var helpOpen by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val selectedTab = tabForRoute(currentRoute)
    val userLabel = stringResource(R.string.user_placeholder)

    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(catalogFacade))
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(homeState.errorMessage) {
        homeState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    fun navigateTab(tab: ShellTab) {
        val route = when (tab) {
            ShellTab.HOME -> PvmDestination.Home.route
            ShellTab.ORGANIZZA -> PvmDestination.Organizza.route
            ShellTab.COMPONI -> PvmDestination.Componi.route
            ShellTab.PUBBLICA -> PvmDestination.Pubblica.route
            ShellTab.GESTISCI -> PvmDestination.Gestisci.route
        }
        val current = navController.currentDestination?.route
        // Già sulla radice del tab: niente da fare.
        if (current == route) return

        // Sempre sulla radice del tab (hub/dashboard), senza ripristinare
        // schermate annidate (es. Acquisisci / Pulisci) — altrimenti Home/Organizza
        // sembrano "non funzionare" dopo essere entrati in una sotto-funzione.
        navController.navigate(route) {
            popUpTo(PvmDestination.Home.route) {
                inclusive = tab == ShellTab.HOME
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun openFeature(featureId: String) {
        when (featureId) {
            "acquisisci" -> navController.navigate(PvmDestination.Acquire.route)
            "pulisci" -> navController.navigate(PvmDestination.Clean.route)
            else -> navController.navigate(PvmDestination.FeatureStub.create(featureId))
        }
    }

    PvmTheme(darkTheme = darkTheme) {
        PvmScaffold(
            selectedTab = selectedTab,
            darkTheme = darkTheme,
            userLabel = userLabel,
            snackbarHostState = snackbarHostState,
            onSelectTab = ::navigateTab,
            onOpenConfig = {
                navController.navigate(PvmDestination.Config.route) {
                    launchSingleTop = true
                }
            },
            onToggleTheme = {
                darkTheme = !darkTheme
                themePreferences.setDarkMode(darkTheme)
            },
            onHelp = { helpOpen = true },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = PvmDestination.Home.route,
                modifier = Modifier.padding(padding),
            ) {
                composable(PvmDestination.Home.route) {
                    LaunchedEffect(Unit) { homeViewModel.refresh() }
                    HomeScreen(
                        snapshot = homeState.snapshot,
                        onOpenTab = ::navigateTab,
                    )
                }
                composable(PvmDestination.Organizza.route) {
                    MacroHubScreen(
                        area = MacroNavigation.areaFor(ShellTab.ORGANIZZA)!!,
                        onFeatureClick = { openFeature(it.id) },
                    )
                }
                composable(PvmDestination.Componi.route) {
                    MacroHubScreen(
                        area = MacroNavigation.areaFor(ShellTab.COMPONI)!!,
                        onFeatureClick = { openFeature(it.id) },
                    )
                }
                composable(PvmDestination.Pubblica.route) {
                    MacroHubScreen(
                        area = MacroNavigation.areaFor(ShellTab.PUBBLICA)!!,
                        onFeatureClick = { openFeature(it.id) },
                    )
                }
                composable(PvmDestination.Gestisci.route) {
                    MacroHubScreen(
                        area = MacroNavigation.areaFor(ShellTab.GESTISCI)!!,
                        onFeatureClick = { openFeature(it.id) },
                    )
                }
                composable(PvmDestination.Config.route) {
                    ConfigScreen(
                        onOpenArchive = {
                            navController.navigate(PvmDestination.ArchiveSources.route)
                        },
                    )
                }
                composable(PvmDestination.ArchiveSources.route) {
                    val archiveVm: ArchiveSourcesViewModel = viewModel(
                        factory = ArchiveSourcesViewModel.factory(catalogFacade),
                    )
                    val archiveState by archiveVm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(archiveState.message) {
                        archiveState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            archiveVm.consumeMessage()
                        }
                    }
                    ArchiveSourcesScreen(
                        state = archiveState,
                        onAddFolder = archiveVm::addSafFolder,
                        onRemove = archiveVm::removeSource,
                        onCensus = archiveVm::censusSource,
                        onRefresh = archiveVm::refresh,
                    )
                }
                composable(PvmDestination.Acquire.route) {
                    val acquireVm: AcquireViewModel = viewModel(
                        factory = AcquireViewModel.factory(catalogFacade),
                    )
                    val acquireState by acquireVm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(acquireState.message) {
                        acquireState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            acquireVm.consumeMessage()
                        }
                    }
                    AcquireScreen(
                        state = acquireState,
                        onToggle = acquireVm::toggleSelection,
                        onSelectPending = acquireVm::selectPendingOnly,
                        onClearSelection = acquireVm::clearSelection,
                        onAcquire = acquireVm::acquireSelected,
                        onRefresh = acquireVm::refresh,
                    )
                }
                composable(PvmDestination.Clean.route) {
                    val cleanVm: CleanViewModel = viewModel(
                        factory = CleanViewModel.factory(catalogFacade),
                    )
                    val cleanState by cleanVm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(cleanState.message) {
                        cleanState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            cleanVm.consumeMessage()
                        }
                    }
                    CleanScreen(
                        state = cleanState,
                        onAnalyze = cleanVm::analyze,
                    )
                }
                composable(
                    route = PvmDestination.FeatureStub.route,
                    arguments = listOf(navArgument("featureId") { type = NavType.StringType }),
                ) { entry ->
                    FeatureStubScreen(featureId = entry.arguments?.getString("featureId").orEmpty())
                }
            }
        }

        if (helpOpen) {
            AlertDialog(
                onDismissRequest = { helpOpen = false },
                title = { Text(stringResource(R.string.help)) },
                text = { Text(stringResource(R.string.help_body)) },
                confirmButton = {
                    TextButton(onClick = { helpOpen = false }) {
                        Text(stringResource(R.string.ok))
                    }
                },
            )
        }
    }
}


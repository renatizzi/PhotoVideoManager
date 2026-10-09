package com.renatizzi.photovideomanager.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
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
import com.renatizzi.photovideomanager.ui.acquire.AcquireFlowStep
import com.renatizzi.photovideomanager.ui.acquire.AcquireViewModel
import com.renatizzi.photovideomanager.ui.census.CensusViewModel
import com.renatizzi.photovideomanager.ui.clean.CleanScreen
import com.renatizzi.photovideomanager.ui.clean.CleanViewModel
import com.renatizzi.photovideomanager.ui.config.ConfigScreen
import com.renatizzi.photovideomanager.ui.home.FeatureStubScreen
import com.renatizzi.photovideomanager.ui.home.HomeScreen
import com.renatizzi.photovideomanager.ui.home.HomeViewModel
import com.renatizzi.photovideomanager.ui.home.MacroHubScreen
import com.renatizzi.photovideomanager.ui.home.MacroNavigation
import com.renatizzi.photovideomanager.ui.preview.AcquisisciStaticScreen
import com.renatizzi.photovideomanager.ui.preview.AggiornaStaticScreen
import com.renatizzi.photovideomanager.ui.preview.ComponiLandingStaticScreen
import com.renatizzi.photovideomanager.ui.preview.ImportaStaticScreen
import com.renatizzi.photovideomanager.ui.search.SearchScreen
import com.renatizzi.photovideomanager.ui.search.SearchViewModel
import com.renatizzi.photovideomanager.ui.shell.PvmScaffold
import com.renatizzi.photovideomanager.ui.shell.ShellTab
import com.renatizzi.photovideomanager.ui.theme.PvmTheme
import com.renatizzi.photovideomanager.ui.theme.ThemePreferences
import com.renatizzi.photovideomanager.ui.trash.TrashScreen
import com.renatizzi.photovideomanager.ui.trash.TrashViewModel

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
    var acquireStep by remember { mutableStateOf(AcquireFlowStep.CENSUS) }
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
            ShellTab.UTILITY -> PvmDestination.Utility.route
            ShellTab.CONFIGURA -> PvmDestination.Config.route
        }
        val current = navController.currentDestination?.route
        if (current == route) return

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
            "acquisisci" -> {
                acquireStep = AcquireFlowStep.CENSUS
                navController.navigate(PvmDestination.Acquire.route)
            }
            "aggiorna" -> navController.navigate(PvmDestination.Aggiorna.route)
            "ricerca" -> navController.navigate(PvmDestination.Search.route)
            "pulisci" -> navController.navigate(PvmDestination.Clean.route)
            "ripristina", "cestino" -> navController.navigate(PvmDestination.Trash.route)
            "salva", "condividi", "raggruppa", "edita", "crea", "pubblica", "spazio",
            "backup", "revisione",
            -> navController.navigate(PvmDestination.FeatureStub.create(featureId))
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
            onToggleTheme = {
                darkTheme = !darkTheme
                themePreferences.setDarkMode(darkTheme)
            },
            onHelp = { helpOpen = true },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = PvmDestination.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                composable(PvmDestination.Home.route) {
                    LaunchedEffect(Unit) { homeViewModel.refresh() }
                    HomeScreen(
                        snapshot = homeState.snapshot,
                        onOpenTab = ::navigateTab,
                        onOpenFeature = ::openFeature,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable(PvmDestination.Organizza.route) {
                    MacroHubScreen(
                        area = MacroNavigation.areaFor(ShellTab.ORGANIZZA)!!,
                        onFeatureClick = { openFeature(it.id) },
                    )
                }
                composable(PvmDestination.Componi.route) {
                    // Anteprima statica landing Componi (template): elenco + Crea/Edita/Pubblica
                    ComponiLandingStaticScreen()
                }
                composable(PvmDestination.Utility.route) {
                    MacroHubScreen(
                        area = MacroNavigation.areaFor(ShellTab.UTILITY)!!,
                        onFeatureClick = { openFeature(it.id) },
                    )
                }
                composable(PvmDestination.Config.route) {
                    ConfigScreen()
                }
                // Acquisisci: UN solo processo. Layout congelato + dominio reale (censimento → acquisizione).
                composable(PvmDestination.Acquire.route) {
                    val censusVm: CensusViewModel = viewModel(
                        factory = CensusViewModel.factory(catalogFacade),
                    )
                    val acquireVm: AcquireViewModel = viewModel(
                        factory = AcquireViewModel.factory(catalogFacade),
                    )
                    val censusState by censusVm.state.collectAsStateWithLifecycle()
                    val acquireState by acquireVm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(censusState.message) {
                        censusState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            censusVm.consumeMessage()
                        }
                    }
                    LaunchedEffect(acquireState.message) {
                        acquireState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            acquireVm.consumeMessage()
                        }
                    }
                    LaunchedEffect(acquireStep) {
                        if (acquireStep == AcquireFlowStep.COPY) {
                            acquireVm.refresh()
                        }
                    }
                    when (acquireStep) {
                        AcquireFlowStep.CENSUS -> AcquisisciStaticScreen(
                            state = censusState,
                            onAddSource = censusVm::addSafSource,
                            onToggleSelection = censusVm::toggleSelection,
                            onSetAllSelected = censusVm::setAllSelected,
                            onRefresh = censusVm::refresh,
                            onConferma = {
                                censusVm.confirmSelected { ok ->
                                    if (ok) acquireStep = AcquireFlowStep.COPY
                                }
                            },
                        )
                        AcquireFlowStep.COPY -> ImportaStaticScreen(
                            state = acquireState,
                            onToggle = acquireVm::toggleSelection,
                            onKindFilter = acquireVm::onKindFilter,
                            onRefresh = acquireVm::refresh,
                            onImporta = {
                                acquireVm.acquireSelected { ok ->
                                    if (ok) {
                                        acquireStep = AcquireFlowStep.CENSUS
                                        homeViewModel.refresh()
                                        navController.navigate(PvmDestination.Home.route) {
                                            popUpTo(PvmDestination.Home.route) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                            onBackToAcquisisci = { acquireStep = AcquireFlowStep.CENSUS },
                        )
                    }
                }
                composable(PvmDestination.Aggiorna.route) {
                    val aggiornaVm: SearchViewModel = viewModel(
                        factory = SearchViewModel.factory(catalogFacade),
                    )
                    val aggiornaState by aggiornaVm.state.collectAsStateWithLifecycle()
                    val menuSoon = stringResource(R.string.aggiorna_menu_soon)
                    var menuPing by remember { mutableStateOf(0) }
                    LaunchedEffect(aggiornaState.message) {
                        aggiornaState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            aggiornaVm.consumeMessage()
                        }
                    }
                    LaunchedEffect(menuPing) {
                        if (menuPing > 0) snackbarHostState.showSnackbar(menuSoon)
                    }
                    AggiornaStaticScreen(
                        state = aggiornaState,
                        onQueryChange = aggiornaVm::onQueryChange,
                        onKindFilter = aggiornaVm::onKindFilter,
                        onToggleSelection = aggiornaVm::toggleSelection,
                        onRefresh = aggiornaVm::refresh,
                        onPulisci = { navController.navigate(PvmDestination.Clean.route) },
                        onRowMenu = { menuPing += 1 },
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
                        onRequestTrash = cleanVm::requestTrashExtras,
                        onConfirmTrash = cleanVm::confirmTrashExtras,
                        onDismissTrash = cleanVm::dismissTrashConfirm,
                        onOpenTrash = { navController.navigate(PvmDestination.Trash.route) },
                    )
                }
                composable(PvmDestination.Trash.route) {
                    val trashVm: TrashViewModel = viewModel(
                        factory = TrashViewModel.factory(catalogFacade),
                    )
                    val trashState by trashVm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(trashState.message) {
                        trashState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            trashVm.consumeMessage()
                        }
                    }
                    TrashScreen(
                        state = trashState,
                        onRefresh = trashVm::refresh,
                        onRestore = trashVm::restore,
                        onPurge = trashVm::purge,
                        onRequestEmpty = trashVm::requestEmptyTrash,
                        onConfirmEmpty = trashVm::confirmEmptyTrash,
                        onDismissEmpty = trashVm::dismissEmptyTrash,
                    )
                }
                composable(PvmDestination.Search.route) {
                    val searchVm: SearchViewModel = viewModel(
                        factory = SearchViewModel.factory(catalogFacade),
                    )
                    val searchState by searchVm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(searchState.message) {
                        searchState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            searchVm.consumeMessage()
                        }
                    }
                    SearchScreen(
                        state = searchState,
                        onQueryChange = searchVm::onQueryChange,
                        onKindFilter = searchVm::onKindFilter,
                        onRefresh = searchVm::refresh,
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

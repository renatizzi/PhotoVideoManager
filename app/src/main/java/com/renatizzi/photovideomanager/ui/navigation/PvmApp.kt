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
import com.renatizzi.photovideomanager.ui.acquire.AcquireFlowScaffold
import com.renatizzi.photovideomanager.ui.acquire.AcquireFlowStep
import com.renatizzi.photovideomanager.ui.acquire.AcquireScreen
import com.renatizzi.photovideomanager.ui.acquire.AcquireViewModel
import com.renatizzi.photovideomanager.ui.archive.ArchiveScreen
import com.renatizzi.photovideomanager.ui.archive.ArchiveViewModel
import com.renatizzi.photovideomanager.ui.census.CensusSourcesScreen
import com.renatizzi.photovideomanager.ui.census.CensusViewModel
import com.renatizzi.photovideomanager.ui.census.SourceBrowseScreen
import com.renatizzi.photovideomanager.ui.census.SourceBrowseViewModel
import com.renatizzi.photovideomanager.ui.clean.CleanScreen
import com.renatizzi.photovideomanager.ui.clean.CleanViewModel
import com.renatizzi.photovideomanager.ui.config.ConfigScreen
import com.renatizzi.photovideomanager.ui.home.FeatureStubScreen
import com.renatizzi.photovideomanager.ui.home.HomeScreen
import com.renatizzi.photovideomanager.ui.home.HomeViewModel
import com.renatizzi.photovideomanager.ui.home.MacroHubScreen
import com.renatizzi.photovideomanager.ui.home.MacroNavigation
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
            ShellTab.PUBBLICA -> PvmDestination.Pubblica.route
            ShellTab.GESTISCI -> PvmDestination.Gestisci.route
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
            "archivia" -> navController.navigate(PvmDestination.ArchiveBrowse.route)
            "ricerca" -> navController.navigate(PvmDestination.Search.route)
            "pulisci" -> navController.navigate(PvmDestination.Clean.route)
            "cestino" -> navController.navigate(PvmDestination.Trash.route)
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
                modifier = Modifier.padding(padding),
            ) {
                composable(PvmDestination.Home.route) {
                    LaunchedEffect(Unit) { homeViewModel.refresh() }
                    HomeScreen(
                        snapshot = homeState.snapshot,
                        onOpenTab = ::navigateTab,
                        onOpenFeature = ::openFeature,
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
                    ConfigScreen()
                }
                composable(PvmDestination.Acquire.route) {
                    val censusVm: CensusViewModel = viewModel(
                        factory = CensusViewModel.factory(catalogFacade),
                    )
                    val censusState by censusVm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(censusState.message) {
                        censusState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            censusVm.consumeMessage()
                        }
                    }
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
                    LaunchedEffect(acquireStep) {
                        if (acquireStep == AcquireFlowStep.COPY) acquireVm.refresh()
                        if (acquireStep == AcquireFlowStep.CENSUS) censusVm.refresh()
                    }
                    AcquireFlowScaffold(
                        step = acquireStep,
                        onStep = { acquireStep = it },
                    ) {
                        when (acquireStep) {
                            AcquireFlowStep.CENSUS -> CensusSourcesScreen(
                                state = censusState,
                                onAddSource = censusVm::addSafSource,
                                onToggleSelection = censusVm::toggleSelection,
                                onRefreshStatus = censusVm::refresh,
                                onCensusSelected = censusVm::censusSelected,
                                onCensusOne = censusVm::censusOne,
                                onRemove = censusVm::removeSource,
                                onBrowse = { locationId ->
                                    navController.navigate(
                                        PvmDestination.SourceBrowse.create(locationId),
                                    )
                                },
                                onSortMode = censusVm::setSortMode,
                            )
                            AcquireFlowStep.COPY -> AcquireScreen(
                                state = acquireState,
                                onToggle = acquireVm::toggleSelection,
                                onKindFilter = acquireVm::onKindFilter,
                                onSelectPending = acquireVm::selectPendingOnly,
                                onClearSelection = acquireVm::clearSelection,
                                onAcquire = acquireVm::acquireSelected,
                                onRefresh = acquireVm::refresh,
                            )
                        }
                    }
                }
                composable(
                    route = PvmDestination.SourceBrowse.route,
                    arguments = listOf(navArgument("locationId") { type = NavType.StringType }),
                ) { entry ->
                    val locationId = entry.arguments?.getString("locationId").orEmpty()
                    val browseVm: SourceBrowseViewModel = viewModel(
                        factory = SourceBrowseViewModel.factory(catalogFacade, locationId),
                    )
                    val browseState by browseVm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(browseState.message) {
                        browseState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            browseVm.consumeMessage()
                        }
                    }
                    SourceBrowseScreen(
                        state = browseState,
                        onSortMode = browseVm::setSortMode,
                        onRefresh = browseVm::refresh,
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
                composable(PvmDestination.ArchiveBrowse.route) {
                    val archiveBrowseVm: ArchiveViewModel = viewModel(
                        factory = ArchiveViewModel.factory(catalogFacade),
                    )
                    val archiveBrowseState by archiveBrowseVm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(archiveBrowseState.message) {
                        archiveBrowseState.message?.let {
                            snackbarHostState.showSnackbar(it)
                            archiveBrowseVm.consumeMessage()
                        }
                    }
                    ArchiveScreen(
                        state = archiveBrowseState,
                        onRefresh = archiveBrowseVm::refresh,
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

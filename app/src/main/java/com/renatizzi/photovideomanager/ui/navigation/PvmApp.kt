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
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.ui.acquire.AcquireScreen
import com.renatizzi.photovideomanager.ui.acquire.AcquireViewModel
import com.renatizzi.photovideomanager.ui.config.ArchiveSourcesScreen
import com.renatizzi.photovideomanager.ui.config.ArchiveSourcesViewModel
import com.renatizzi.photovideomanager.ui.config.ConfigScreen
import com.renatizzi.photovideomanager.ui.home.FeatureStubScreen
import com.renatizzi.photovideomanager.ui.home.HomeScreen
import com.renatizzi.photovideomanager.ui.home.HomeViewModel
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
    val selectedTab = when {
        currentRoute?.startsWith("config") == true -> ShellTab.SETTINGS
        else -> ShellTab.HOME
    }
    val userLabel = stringResource(R.string.user_placeholder)

    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(catalogFacade))
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(homeState.errorMessage) {
        homeState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    PvmTheme(darkTheme = darkTheme) {
        PvmScaffold(
            selectedTab = selectedTab,
            darkTheme = darkTheme,
            userLabel = userLabel,
            snackbarHostState = snackbarHostState,
            onSelectTab = { tab ->
                when (tab) {
                    ShellTab.HOME -> navController.navigate(PvmDestination.Home.route) {
                        popUpTo(PvmDestination.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    ShellTab.SETTINGS -> navController.navigate(PvmDestination.Config.route) {
                        launchSingleTop = true
                    }
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
                    val storageStatus = when (homeState.localAvailability) {
                        Availability.AVAILABLE -> stringResource(R.string.storage_local)
                        Availability.UNAVAILABLE -> stringResource(R.string.storage_local_unavailable)
                        Availability.UNKNOWN -> stringResource(R.string.storage_local_unknown)
                    }
                    HomeScreen(
                        catalogCount = homeState.catalogCount,
                        storageStatus = storageStatus,
                        onFeatureClick = { featureId ->
                            when (featureId) {
                                "acquisisci" -> navController.navigate(PvmDestination.Acquire.route)
                                else -> navController.navigate(PvmDestination.FeatureStub.create(featureId))
                            }
                        },
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

package com.renatizzi.photovideomanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.renatizzi.photovideomanager.ui.config.ArchiveSourcesScreen
import com.renatizzi.photovideomanager.ui.config.ArchiveSourcesViewModel
import com.renatizzi.photovideomanager.ui.config.ConfigScreen
import com.renatizzi.photovideomanager.ui.home.FeatureStubScreen
import com.renatizzi.photovideomanager.ui.home.HomeScreen
import com.renatizzi.photovideomanager.ui.home.HomeViewModel
import com.renatizzi.photovideomanager.ui.shell.BottomTab
import com.renatizzi.photovideomanager.ui.shell.PvmBottomBar
import com.renatizzi.photovideomanager.ui.shell.PvmTopBar
import com.renatizzi.photovideomanager.ui.theme.PvmTheme

@Composable
fun PvmApp(
    catalogFacade: CatalogFacade,
    darkThemeOverride: Boolean? = null,
) {
    var darkTheme by remember {
        mutableStateOf(darkThemeOverride ?: false)
    }
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val selectedTab = when {
        currentRoute?.startsWith("config") == true -> BottomTab.Settings
        else -> BottomTab.Home
    }

    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(catalogFacade))
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(homeState.errorMessage) {
        homeState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    PvmTheme(darkTheme = darkTheme) {
        Scaffold(
            topBar = {
                PvmTopBar(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    onHelp = { },
                )
            },
            bottomBar = {
                PvmBottomBar(
                    selected = selectedTab,
                    onSelect = { tab ->
                        when (tab) {
                            BottomTab.Home -> navController.navigate(PvmDestination.Home.route) {
                                popUpTo(PvmDestination.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                            BottomTab.Settings -> navController.navigate(PvmDestination.Config.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = PvmDestination.Home.route,
                modifier = Modifier.padding(padding),
            ) {
                composable(PvmDestination.Home.route) {
                    val storageStatus = when (homeState.localAvailability) {
                        Availability.AVAILABLE -> stringResource(R.string.storage_local)
                        Availability.UNAVAILABLE -> stringResource(R.string.storage_local_unavailable)
                        Availability.UNKNOWN -> stringResource(R.string.storage_local_unknown)
                    }
                    HomeScreen(
                        catalogCount = homeState.catalogCount,
                        storageStatus = storageStatus,
                        onFeatureClick = { featureId ->
                            navController.navigate(PvmDestination.FeatureStub.create(featureId))
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
                        onRefresh = archiveVm::refresh,
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
    }
}

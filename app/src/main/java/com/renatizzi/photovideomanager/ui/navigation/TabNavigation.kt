package com.renatizzi.photovideomanager.ui.navigation

import com.renatizzi.photovideomanager.ui.shell.ShellTab

fun tabForRoute(route: String?): ShellTab? = when {
    route == null -> ShellTab.HOME
    route == PvmDestination.Home.route -> ShellTab.HOME
    route.startsWith("organizza") -> ShellTab.ORGANIZZA
    route.startsWith("componi") -> ShellTab.COMPONI
    route.startsWith("pubblica") -> ShellTab.PUBBLICA
    route.startsWith("gestisci") -> ShellTab.GESTISCI
    route.startsWith("config") -> ShellTab.CONFIGURA
    route.startsWith("feature/") -> null
    else -> ShellTab.HOME
}

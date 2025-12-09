package com.example.smartledger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class LedgerDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : LedgerDestination("home", "首页", Icons.Outlined.Home)
    data object Add : LedgerDestination("add", "记一笔", Icons.Outlined.Add)
    data object Stats : LedgerDestination("stats", "统计", Icons.Outlined.BarChart)
    data object Methods : LedgerDestination("methods", "方式/卡片", Icons.Outlined.CreditCard)
    data object Settings : LedgerDestination("settings", "设置", Icons.Outlined.Settings)
}

val bottomDestinations = listOf(
    LedgerDestination.Home,
    LedgerDestination.Add,
    LedgerDestination.Stats,
    LedgerDestination.Methods,
    LedgerDestination.Settings
)



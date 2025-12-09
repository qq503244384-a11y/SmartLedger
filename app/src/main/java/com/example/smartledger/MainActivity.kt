package com.example.smartledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartledger.SmartLedgerApp
import com.example.smartledger.data.SettingsRepository
import com.example.smartledger.ui.navigation.LedgerDestination
import com.example.smartledger.ui.navigation.bottomDestinations
import com.example.smartledger.ui.screens.AddTransactionScreen
import com.example.smartledger.ui.screens.HomeScreen
import com.example.smartledger.ui.screens.InboxScreen
import com.example.smartledger.ui.screens.MethodsScreen
import com.example.smartledger.ui.screens.SettingsScreen
import com.example.smartledger.ui.screens.StatsScreen
import com.example.smartledger.ui.theme.SmartLedgerTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartLedgerTheme {
                val container = (application as SmartLedgerApp).container
                val settingsRepository = remember { container.settingsRepository }
                val navController = rememberNavController()
                Scaffold(
                    topBar = { TopBar() },
                    bottomBar = { BottomNavBar(navController) }
                ) { padding ->
                    LedgerNavHost(
                        navController = navController,
                        settingsRepository = settingsRepository,
                        repository = container.repository,
                        messageProcessor = container.messageProcessor,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    TopAppBar(
        title = { Text(text = "Smart Ledger") }
    )
}

@Composable
private fun LedgerNavHost(
    navController: NavHostController,
    settingsRepository: SettingsRepository,
    repository: com.example.smartledger.data.LedgerRepository,
    messageProcessor: com.example.smartledger.domain.MessageProcessor,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = LedgerDestination.Home.route, modifier = modifier) {
        composable(LedgerDestination.Home.route) {
            HomeScreen(
                repository = repository,
                processor = messageProcessor,
                onNavigateInbox = { navController.navigate("inbox") }
            )
        }
        composable(LedgerDestination.Add.route) { AddTransactionScreen(repository) }
        composable(LedgerDestination.Stats.route) { StatsScreen(repository) }
        composable(LedgerDestination.Methods.route) { MethodsScreen(repository) }
        composable(LedgerDestination.Settings.route) { SettingsScreen(settingsRepository) }
        composable("inbox") {
            InboxScreen(
                processor = messageProcessor,
                repository = repository
            )
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar {
        bottomDestinations.forEach { dest ->
            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label) }
            )
        }
    }
}


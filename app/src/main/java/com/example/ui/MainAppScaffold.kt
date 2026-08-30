package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionType
import com.example.ui.components.AddTransactionSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

enum class AppNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    TRANSACTIONS("History", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_transactions"),
    ANALYTICS("Insights", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_analytics"),
    BUDGET("Budget", Icons.Filled.Savings, Icons.Outlined.Savings, "nav_budget"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: FinanceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var currentDestination by remember { mutableStateOf(AppNavDestination.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Category Creation Dialog State
    var newCatName by remember { mutableStateOf("") }
    var newCatType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var newCatIcon by remember { mutableStateOf("shopping_bag") }

    // Theme Mode
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (uiState.darkModePreference) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
    }

    // Snackbar Trigger
    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            coroutineScope.launch {
                val actionLabel = if (uiState.lastDeletedTransaction != null) "Undo" else null
                val result = snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = actionLabel,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDelete()
                }
                viewModel.clearSnackbar()
            }
        }
    }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("main_bottom_nav_bar")
                    ) {
                        AppNavDestination.entries.forEach { destination ->
                            val isSelected = currentDestination == destination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmeraldPrimary,
                                    selectedTextColor = EmeraldPrimary,
                                    indicatorColor = EmeraldPrimary.copy(alpha = 0.12f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(destination.testTag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentDestination) {
                        AppNavDestination.HOME -> HomeScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigateToTransactions = { currentDestination = AppNavDestination.TRANSACTIONS },
                            onNavigateToSettings = { currentDestination = AppNavDestination.SETTINGS }
                        )
                        AppNavDestination.TRANSACTIONS -> TransactionsScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                        AppNavDestination.ANALYTICS -> AnalyticsScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                        AppNavDestination.BUDGET -> BudgetScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                        AppNavDestination.SETTINGS -> SettingsScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Global Add / Edit Transaction Sheet
            if (uiState.isAddTransactionSheetOpen) {
                AddTransactionSheet(
                    uiState = uiState,
                    viewModel = viewModel,
                    sheetState = addSheetState
                )
            }

            // Add Category Dialog
            if (uiState.isCategoryDialogOpen) {
                AlertDialog(
                    onDismissRequest = { viewModel.setCategoryDialogOpen(false) },
                    title = { Text("Add Custom Category") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Category Type Selector (Expense vs Income)
                            TabRow(
                                selectedTabIndex = if (newCatType == TransactionType.EXPENSE) 0 else 1,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Tab(
                                    selected = newCatType == TransactionType.EXPENSE,
                                    onClick = { newCatType = TransactionType.EXPENSE },
                                    text = { Text("Expense", color = if (newCatType == TransactionType.EXPENSE) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant) }
                                )
                                Tab(
                                    selected = newCatType == TransactionType.INCOME,
                                    onClick = { newCatType = TransactionType.INCOME },
                                    text = { Text("Income", color = if (newCatType == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant) }
                                )
                            }

                            OutlinedTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                label = { Text("Category Name (e.g. Gym, Pets, Subscriptions)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newCatName.isNotBlank()) {
                                    val color = if (newCatType == TransactionType.INCOME) 0xFF10B981 else 0xFFF59E0B
                                    viewModel.addNewCategory(
                                        name = newCatName,
                                        type = newCatType,
                                        icon = "category",
                                        color = color
                                    )
                                    newCatName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setCategoryDialogOpen(false) }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

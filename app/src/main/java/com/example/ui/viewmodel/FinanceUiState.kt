package com.example.ui.viewmodel

import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CurrencyConfig
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.util.InsightCard

enum class TimeRange(val label: String, val days: Int) {
    SEVEN_DAYS("7 Days", 7),
    THIRTY_DAYS("30 Days", 30),
    SIX_MONTHS("6 Months", 180),
    ONE_YEAR("1 Year", 365)
}

enum class TransactionFilter(val label: String) {
    ALL("All"),
    INCOME("Income"),
    EXPENSE("Expense"),
    TRANSFER("Transfers")
}

enum class DateFilter(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

data class CashFlowPoint(
    val label: String,
    val fullDateLabel: String,
    val income: Double,
    val expense: Double,
    val dateMillis: Long
)

data class CategorySpendItem(
    val categoryId: String,
    val categoryName: String,
    val icon: String,
    val color: Long,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class BudgetProgressItem(
    val budgetId: String,
    val categoryId: String?,
    val name: String,
    val limit: Double,
    val spent: Double,
    val remaining: Double,
    val percentage: Float,
    val isOverBudget: Boolean,
    val isWarning: Boolean, // >= 80%
    val statusText: String
)

data class FinanceUiState(
    val isLoading: Boolean = true,
    val currency: CurrencyConfig = CurrencyConfig.NPR,
    val darkModePreference: String = "LIGHT", // SYSTEM, LIGHT, DARK
    val isAppLockEnabled: Boolean = false,
    val isUnlocked: Boolean = true,

    // Core Entities
    val transactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),

    // Computed Dashboard Metrics
    val totalAvailableBalance: Double = 0.0,
    val thisMonthIncome: Double = 0.0,
    val thisMonthExpense: Double = 0.0,
    val thisMonthSavings: Double = 0.0,
    val savingsRate: Double = 0.0,
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,

    // Chart & Analytics
    val selectedTimeRange: TimeRange = TimeRange.SEVEN_DAYS,
    val cashFlowPoints: List<CashFlowPoint> = emptyList(),
    val categoryBreakdown: List<CategorySpendItem> = emptyList(),
    val topSpendingCategories: List<CategorySpendItem> = emptyList(),
    val dailySpendPoints: List<CashFlowPoint> = emptyList(),
    val balanceTrendPoints: List<Pair<String, Double>> = emptyList(),
    val insights: List<InsightCard> = emptyList(),

    // Budget Status
    val overallBudget: BudgetProgressItem? = null,
    val categoryBudgets: List<BudgetProgressItem> = emptyList(),

    // Filter & Search in Transaction History
    val searchQuery: String = "",
    val typeFilter: TransactionFilter = TransactionFilter.ALL,
    val dateFilter: DateFilter = DateFilter.ALL,
    val selectedCategoryFilter: String? = null,
    val selectedAccountFilter: String? = null,
    val filteredTransactionsGrouped: Map<String, List<TransactionEntity>> = emptyMap(),

    // Modals / Sheets
    val isAddTransactionSheetOpen: Boolean = false,
    val editingTransaction: TransactionEntity? = null,
    val prefillType: TransactionType = TransactionType.EXPENSE,
    val isBudgetDialogOpen: Boolean = false,
    val isAccountDialogOpen: Boolean = false,
    val isCategoryDialogOpen: Boolean = false,
    val isExportDialogOpen: Boolean = false,
    val exportContent: String = "",
    val exportType: String = "JSON",

    // Undo & Feedback
    val snackbarMessage: String? = null,
    val lastDeletedTransaction: TransactionEntity? = null
)

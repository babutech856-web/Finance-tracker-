package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CurrencyConfig
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.util.CurrencyFormatter
import com.example.util.DateTimeUtils
import com.example.util.FinancialInsightsEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class FilterState(
    val currency: CurrencyConfig = CurrencyConfig.NPR,
    val timeRange: TimeRange = TimeRange.SEVEN_DAYS,
    val searchQuery: String = "",
    val typeFilter: TransactionFilter = TransactionFilter.ALL,
    val dateFilter: DateFilter = DateFilter.ALL,
    val categoryFilter: String? = null,
    val accountFilter: String? = null
)

private data class DialogState(
    val isAddTransactionSheetOpen: Boolean = false,
    val editingTransaction: TransactionEntity? = null,
    val prefillType: TransactionType = TransactionType.EXPENSE,
    val isBudgetDialogOpen: Boolean = false,
    val isAccountDialogOpen: Boolean = false,
    val isCategoryDialogOpen: Boolean = false,
    val isExportDialogOpen: Boolean = false,
    val exportContent: String = "",
    val exportType: String = "JSON",
    val snackbarMessage: String? = null,
    val lastDeletedTransaction: TransactionEntity? = null,
    val darkModePreference: String = "LIGHT",
    val isAppLockEnabled: Boolean = false,
    val isUnlocked: Boolean = true
)

private data class DataState(
    val transactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList()
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    private val _currency = MutableStateFlow(CurrencyConfig.NPR)
    private val _darkModePreference = MutableStateFlow("LIGHT")
    private val _isAppLockEnabled = MutableStateFlow(false)
    private val _isUnlocked = MutableStateFlow(true)
    private val _selectedTimeRange = MutableStateFlow(TimeRange.SEVEN_DAYS)
    private val _searchQuery = MutableStateFlow("")
    private val _typeFilter = MutableStateFlow(TransactionFilter.ALL)
    private val _dateFilter = MutableStateFlow(DateFilter.ALL)
    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    private val _selectedAccountFilter = MutableStateFlow<String?>(null)

    private val _isAddTransactionSheetOpen = MutableStateFlow(false)
    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    private val _prefillType = MutableStateFlow(TransactionType.EXPENSE)
    private val _isBudgetDialogOpen = MutableStateFlow(false)
    private val _isAccountDialogOpen = MutableStateFlow(false)
    private val _isCategoryDialogOpen = MutableStateFlow(false)
    private val _isExportDialogOpen = MutableStateFlow(false)
    private val _exportContent = MutableStateFlow("")
    private val _exportType = MutableStateFlow("JSON")
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _lastDeletedTransaction = MutableStateFlow<TransactionEntity?>(null)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(
            transactionDao = db.transactionDao(),
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao(),
            budgetDao = db.budgetDao()
        )

        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    private val dataFlow = combine(
        repository.allTransactions,
        repository.allAccounts,
        repository.allCategories,
        repository.allBudgets
    ) { tx, acc, cat, bud ->
        DataState(tx, acc, cat, bud)
    }

    private val filterFlow = combine(
        _currency,
        _selectedTimeRange,
        _searchQuery,
        _typeFilter,
        _dateFilter,
        _selectedCategoryFilter,
        _selectedAccountFilter
    ) { params ->
        FilterState(
            currency = params[0] as CurrencyConfig,
            timeRange = params[1] as TimeRange,
            searchQuery = params[2] as String,
            typeFilter = params[3] as TransactionFilter,
            dateFilter = params[4] as DateFilter,
            categoryFilter = params[5] as String?,
            accountFilter = params[6] as String?
        )
    }

    private val dialogFlow = combine(
        combine(
            _isAddTransactionSheetOpen,
            _editingTransaction,
            _prefillType,
            _isBudgetDialogOpen,
            _isAccountDialogOpen
        ) { a, b, c, d, e -> listOf(a, b, c, d, e) },
        combine(
            _isCategoryDialogOpen,
            _isExportDialogOpen,
            _exportContent,
            _exportType,
            _snackbarMessage
        ) { a, b, c, d, e -> listOf(a, b, c, d, e) },
        combine(
            _lastDeletedTransaction,
            _darkModePreference,
            _isAppLockEnabled,
            _isUnlocked
        ) { a, b, c, d -> listOf(a, b, c, d) }
    ) { group1, group2, group3 ->
        DialogState(
            isAddTransactionSheetOpen = group1[0] as Boolean,
            editingTransaction = group1[1] as TransactionEntity?,
            prefillType = group1[2] as TransactionType,
            isBudgetDialogOpen = group1[3] as Boolean,
            isAccountDialogOpen = group1[4] as Boolean,
            isCategoryDialogOpen = group2[0] as Boolean,
            isExportDialogOpen = group2[1] as Boolean,
            exportContent = group2[2] as String,
            exportType = group2[3] as String,
            snackbarMessage = group2[4] as String?,
            lastDeletedTransaction = group3[0] as TransactionEntity?,
            darkModePreference = group3[1] as String,
            isAppLockEnabled = group3[2] as Boolean,
            isUnlocked = group3[3] as Boolean
        )
    }

    val uiState: StateFlow<FinanceUiState> = combine(
        dataFlow,
        filterFlow,
        dialogFlow
    ) { data, filter, dialog ->
        buildUiState(data, filter, dialog)
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceUiState()
    )

    private fun buildUiState(
        data: DataState,
        filter: FilterState,
        dialog: DialogState
    ): FinanceUiState {
        val transactions = data.transactions
        val accounts = data.accounts
        val categories = data.categories
        val budgets = data.budgets
        val currency = filter.currency
        val timeRange = filter.timeRange
        val searchQuery = filter.searchQuery
        val typeFilter = filter.typeFilter
        val dateFilter = filter.dateFilter
        val categoryFilter = filter.categoryFilter
        val accountFilter = filter.accountFilter

        // 1. Balance Calculations
        val totalOpeningBalance = accounts.sumOf { it.openingBalance }
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalAvailableBalance = totalOpeningBalance + totalIncome - totalExpense

        // 2. This Month & Today Metrics
        val startOfMonth = DateTimeUtils.getStartOfMonth()
        val startOfToday = DateTimeUtils.getStartOfToday()

        val thisMonthIncome = transactions
            .filter { it.type == TransactionType.INCOME && it.dateMillis >= startOfMonth }
            .sumOf { it.amount }
        val thisMonthExpense = transactions
            .filter { it.type == TransactionType.EXPENSE && it.dateMillis >= startOfMonth }
            .sumOf { it.amount }
        val thisMonthSavings = thisMonthIncome - thisMonthExpense
        val savingsRate = if (thisMonthIncome > 0) {
            ((thisMonthSavings / thisMonthIncome) * 100).coerceAtLeast(0.0)
        } else {
            0.0
        }

        val todayIncome = transactions
            .filter { it.type == TransactionType.INCOME && it.dateMillis >= startOfToday }
            .sumOf { it.amount }
        val todayExpense = transactions
            .filter { it.type == TransactionType.EXPENSE && it.dateMillis >= startOfToday }
            .sumOf { it.amount }

        // 3. Cash Flow Series for Selected Range
        val cashFlowPoints = computeCashFlowSeries(transactions, timeRange)
        val dailySpendPoints = computeDailySpendSeries(transactions, timeRange)
        val balanceTrendPoints = computeBalanceTrendSeries(transactions, totalOpeningBalance, timeRange)

        // 4. Category Breakdown (Expenses for this month / selected range)
        val rangeStart = when (timeRange) {
            TimeRange.SEVEN_DAYS -> DateTimeUtils.getDaysAgo(7)
            TimeRange.THIRTY_DAYS -> DateTimeUtils.getDaysAgo(30)
            TimeRange.SIX_MONTHS -> DateTimeUtils.getMonthsAgo(6)
            TimeRange.ONE_YEAR -> DateTimeUtils.getMonthsAgo(12)
        }
        val rangeExpenses = transactions.filter { it.type == TransactionType.EXPENSE && it.dateMillis >= rangeStart }
        val totalRangeExpense = rangeExpenses.sumOf { it.amount }

        val categoryBreakdown = rangeExpenses
            .groupBy { it.categoryId }
            .map { (catId, items) ->
                val sum = items.sumOf { it.amount }
                val firstItem = items.first()
                val pct = if (totalRangeExpense > 0) (sum / totalRangeExpense).toFloat() * 100f else 0f
                CategorySpendItem(
                    categoryId = catId,
                    categoryName = firstItem.categoryName,
                    icon = firstItem.categoryIcon,
                    color = firstItem.categoryColor,
                    amount = sum,
                    percentage = pct,
                    transactionCount = items.size
                )
            }
            .sortedByDescending { it.amount }

        val topSpendingCategories = categoryBreakdown.take(5)

        // 5. Budgets Progress Calculation
        val thisMonthExpenses = transactions.filter { it.type == TransactionType.EXPENSE && it.dateMillis >= startOfMonth }
        val overallBudgetEntity = budgets.find { it.categoryId == null }
        val overallBudgetProgress = overallBudgetEntity?.let { b ->
            val spent = thisMonthExpenses.sumOf { it.amount }
            val remaining = b.monthlyLimit - spent
            val pct = if (b.monthlyLimit > 0) (spent / b.monthlyLimit).toFloat() else 0f
            val isOver = spent > b.monthlyLimit
            val isWarn = !isOver && pct >= 0.8f
            val status = when {
                isOver -> "Over budget by ${CurrencyFormatter.format(spent - b.monthlyLimit, currency)}"
                isWarn -> "Approaching limit (${(pct * 100).toInt()}%)"
                else -> "${CurrencyFormatter.format(remaining, currency)} remaining"
            }
            BudgetProgressItem(
                budgetId = b.id,
                categoryId = null,
                name = "Monthly Budget",
                limit = b.monthlyLimit,
                spent = spent,
                remaining = remaining,
                percentage = pct,
                isOverBudget = isOver,
                isWarning = isWarn,
                statusText = status
            )
        }

        val categoryBudgetProgress = budgets.filter { it.categoryId != null }.map { b ->
            val spent = thisMonthExpenses.filter { it.categoryId == b.categoryId }.sumOf { it.amount }
            val remaining = b.monthlyLimit - spent
            val pct = if (b.monthlyLimit > 0) (spent / b.monthlyLimit).toFloat() else 0f
            val isOver = spent > b.monthlyLimit
            val isWarn = !isOver && pct >= 0.8f
            val status = when {
                isOver -> "Over budget by ${CurrencyFormatter.format(spent - b.monthlyLimit, currency)}"
                isWarn -> "Approaching limit (${(pct * 100).toInt()}%)"
                else -> "${CurrencyFormatter.format(remaining, currency)} left"
            }
            BudgetProgressItem(
                budgetId = b.id,
                categoryId = b.categoryId,
                name = b.categoryName,
                limit = b.monthlyLimit,
                spent = spent,
                remaining = remaining,
                percentage = pct,
                isOverBudget = isOver,
                isWarning = isWarn,
                statusText = status
            )
        }

        // 6. Insights
        val insights = FinancialInsightsEngine.generateInsights(transactions, currency)

        // 7. Filtered Transactions
        val filtered = transactions.filter { t ->
            val matchesQuery = searchQuery.isBlank() ||
                    t.description.contains(searchQuery, ignoreCase = true) ||
                    t.categoryName.contains(searchQuery, ignoreCase = true) ||
                    t.fromAccount.contains(searchQuery, ignoreCase = true) ||
                    t.notes.contains(searchQuery, ignoreCase = true)

            val matchesType = when (typeFilter) {
                TransactionFilter.ALL -> true
                TransactionFilter.INCOME -> t.type == TransactionType.INCOME
                TransactionFilter.EXPENSE -> t.type == TransactionType.EXPENSE
                TransactionFilter.TRANSFER -> t.type == TransactionType.TRANSFER
            }

            val matchesDate = when (dateFilter) {
                DateFilter.ALL -> true
                DateFilter.TODAY -> t.dateMillis >= DateTimeUtils.getStartOfToday()
                DateFilter.THIS_WEEK -> t.dateMillis >= DateTimeUtils.getStartOfWeek()
                DateFilter.THIS_MONTH -> t.dateMillis >= DateTimeUtils.getStartOfMonth()
            }

            val matchesCategory = categoryFilter == null || t.categoryId == categoryFilter
            val matchesAccount = accountFilter == null || t.fromAccount == accountFilter

            matchesQuery && matchesType && matchesDate && matchesCategory && matchesAccount
        }

        val groupedTransactions = filtered.groupBy { t ->
            DateTimeUtils.getDateGroupHeader(t.dateMillis)
        }

        return FinanceUiState(
            isLoading = false,
            currency = currency,
            darkModePreference = dialog.darkModePreference,
            isAppLockEnabled = dialog.isAppLockEnabled,
            isUnlocked = dialog.isUnlocked,
            transactions = transactions,
            accounts = accounts,
            categories = categories,
            budgets = budgets,
            totalAvailableBalance = totalAvailableBalance,
            thisMonthIncome = thisMonthIncome,
            thisMonthExpense = thisMonthExpense,
            thisMonthSavings = thisMonthSavings,
            savingsRate = savingsRate,
            todayIncome = todayIncome,
            todayExpense = todayExpense,
            selectedTimeRange = timeRange,
            cashFlowPoints = cashFlowPoints,
            categoryBreakdown = categoryBreakdown,
            topSpendingCategories = topSpendingCategories,
            dailySpendPoints = dailySpendPoints,
            balanceTrendPoints = balanceTrendPoints,
            insights = insights,
            overallBudget = overallBudgetProgress,
            categoryBudgets = categoryBudgetProgress,
            searchQuery = searchQuery,
            typeFilter = typeFilter,
            dateFilter = dateFilter,
            selectedCategoryFilter = categoryFilter,
            selectedAccountFilter = accountFilter,
            filteredTransactionsGrouped = groupedTransactions,
            isAddTransactionSheetOpen = dialog.isAddTransactionSheetOpen,
            editingTransaction = dialog.editingTransaction,
            prefillType = dialog.prefillType,
            isBudgetDialogOpen = dialog.isBudgetDialogOpen,
            isAccountDialogOpen = dialog.isAccountDialogOpen,
            isCategoryDialogOpen = dialog.isCategoryDialogOpen,
            isExportDialogOpen = dialog.isExportDialogOpen,
            exportContent = dialog.exportContent,
            exportType = dialog.exportType,
            snackbarMessage = dialog.snackbarMessage,
            lastDeletedTransaction = dialog.lastDeletedTransaction
        )
    }

    private fun computeCashFlowSeries(
        transactions: List<TransactionEntity>,
        timeRange: TimeRange
    ): List<CashFlowPoint> {
        val points = mutableListOf<CashFlowPoint>()
        val cal = Calendar.getInstance()

        when (timeRange) {
            TimeRange.SEVEN_DAYS -> {
                for (i in 6 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_YEAR, -i)
                    c.set(Calendar.HOUR_OF_DAY, 0)
                    c.set(Calendar.MINUTE, 0)
                    c.set(Calendar.SECOND, 0)
                    c.set(Calendar.MILLISECOND, 0)
                    val start = c.timeInMillis
                    c.set(Calendar.HOUR_OF_DAY, 23)
                    c.set(Calendar.MINUTE, 59)
                    c.set(Calendar.SECOND, 59)
                    val end = c.timeInMillis

                    val dayTransactions = transactions.filter { it.dateMillis in start..end }
                    val inc = dayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val exp = dayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                    val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(start))
                    val fullLabel = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(start))
                    points.add(CashFlowPoint(dayLabel, fullLabel, inc, exp, start))
                }
            }
            TimeRange.THIRTY_DAYS -> {
                // Group by 5-day intervals or weekly buckets
                for (i in 29 downTo 0 step 5) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_YEAR, -i)
                    c.set(Calendar.HOUR_OF_DAY, 0)
                    c.set(Calendar.MINUTE, 0)
                    c.set(Calendar.SECOND, 0)
                    val start = c.timeInMillis
                    val end = start + (5 * 86400000L) - 1000L

                    val rangeTx = transactions.filter { it.dateMillis in start..end }
                    val inc = rangeTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val exp = rangeTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                    val label = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(start))
                    points.add(CashFlowPoint(label, label, inc, exp, start))
                }
            }
            TimeRange.SIX_MONTHS -> {
                for (i in 5 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.MONTH, -i)
                    c.set(Calendar.DAY_OF_MONTH, 1)
                    c.set(Calendar.HOUR_OF_DAY, 0)
                    c.set(Calendar.MINUTE, 0)
                    c.set(Calendar.SECOND, 0)
                    val start = c.timeInMillis
                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
                    c.set(Calendar.HOUR_OF_DAY, 23)
                    c.set(Calendar.MINUTE, 59)
                    val end = c.timeInMillis

                    val monthTx = transactions.filter { it.dateMillis in start..end }
                    val inc = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val exp = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                    val label = SimpleDateFormat("MMM", Locale.getDefault()).format(Date(start))
                    val fullLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(start))
                    points.add(CashFlowPoint(label, fullLabel, inc, exp, start))
                }
            }
            TimeRange.ONE_YEAR -> {
                for (i in 11 downTo 0 step 2) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.MONTH, -i)
                    c.set(Calendar.DAY_OF_MONTH, 1)
                    c.set(Calendar.HOUR_OF_DAY, 0)
                    c.set(Calendar.MINUTE, 0)
                    val start = c.timeInMillis
                    val end = start + (60 * 86400000L)

                    val rangeTx = transactions.filter { it.dateMillis in start..end }
                    val inc = rangeTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val exp = rangeTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                    val label = SimpleDateFormat("MMM", Locale.getDefault()).format(Date(start))
                    points.add(CashFlowPoint(label, label, inc, exp, start))
                }
            }
        }
        return points
    }

    private fun computeDailySpendSeries(
        transactions: List<TransactionEntity>,
        timeRange: TimeRange
    ): List<CashFlowPoint> {
        val days = when (timeRange) {
            TimeRange.SEVEN_DAYS -> 7
            TimeRange.THIRTY_DAYS -> 14
            TimeRange.SIX_MONTHS -> 18
            TimeRange.ONE_YEAR -> 24
        }
        val points = mutableListOf<CashFlowPoint>()
        for (i in (days - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            val start = c.timeInMillis
            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            val end = c.timeInMillis

            val dayTx = transactions.filter { it.dateMillis in start..end }
            val exp = dayTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val inc = dayTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

            val label = SimpleDateFormat("d", Locale.getDefault()).format(Date(start))
            val fullLabel = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(start))
            points.add(CashFlowPoint(label, fullLabel, inc, exp, start))
        }
        return points
    }

    private fun computeBalanceTrendSeries(
        transactions: List<TransactionEntity>,
        openingBalance: Double,
        timeRange: TimeRange
    ): List<Pair<String, Double>> {
        val points = mutableListOf<Pair<String, Double>>()
        val count = 7
        val sortedAsc = transactions.sortedBy { it.dateMillis }

        for (i in (count - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i * (if (timeRange == TimeRange.SEVEN_DAYS) 1 else 4))
            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            val timestamp = c.timeInMillis

            val txUpToPoint = sortedAsc.filter { it.dateMillis <= timestamp }
            val inc = txUpToPoint.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val exp = txUpToPoint.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val bal = openingBalance + inc - exp

            val label = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
            points.add(label to bal)
        }
        return points
    }

    // User Actions
    fun setCurrency(currency: CurrencyConfig) {
        _currency.value = currency
    }

    fun setDarkModePreference(mode: String) {
        _darkModePreference.value = mode
    }

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(filter: TransactionFilter) {
        _typeFilter.value = filter
    }

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun setAccountFilter(accountName: String?) {
        _selectedAccountFilter.value = accountName
    }

    fun openAddTransactionSheet(type: TransactionType = TransactionType.EXPENSE) {
        _prefillType.value = type
        _editingTransaction.value = null
        _isAddTransactionSheetOpen.value = true
    }

    fun openEditTransaction(transaction: TransactionEntity) {
        _editingTransaction.value = transaction
        _prefillType.value = transaction.type
        _isAddTransactionSheetOpen.value = true
    }

    fun closeAddTransactionSheet() {
        _isAddTransactionSheetOpen.value = false
        _editingTransaction.value = null
    }

    fun saveTransaction(
        type: TransactionType,
        amount: Double,
        categoryId: String,
        categoryName: String,
        categoryIcon: String,
        categoryColor: Long,
        description: String,
        fromAccount: String,
        toAccount: String?,
        dateMillis: Long,
        notes: String,
        keepOpenForNext: Boolean = false
    ) {
        viewModelScope.launch {
            val current = _editingTransaction.value
            if (current != null) {
                repository.updateTransaction(
                    current.copy(
                        type = type,
                        amount = amount,
                        categoryId = categoryId,
                        categoryName = categoryName,
                        categoryIcon = categoryIcon,
                        categoryColor = categoryColor,
                        description = description,
                        fromAccount = fromAccount,
                        toAccount = toAccount,
                        dateMillis = dateMillis,
                        notes = notes
                    )
                )
                _snackbarMessage.value = "Transaction updated"
            } else {
                repository.addTransaction(
                    TransactionEntity(
                        type = type,
                        amount = amount,
                        categoryId = categoryId,
                        categoryName = categoryName,
                        categoryIcon = categoryIcon,
                        categoryColor = categoryColor,
                        description = description,
                        fromAccount = fromAccount,
                        toAccount = toAccount,
                        dateMillis = dateMillis,
                        notes = notes
                    )
                )
                _snackbarMessage.value = "Transaction recorded successfully"
            }

            if (!keepOpenForNext) {
                closeAddTransactionSheet()
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            _lastDeletedTransaction.value = transaction
            repository.deleteTransaction(transaction)
            _snackbarMessage.value = "Transaction deleted"
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            val deleted = _lastDeletedTransaction.value
            if (deleted != null) {
                repository.addTransaction(deleted)
                _lastDeletedTransaction.value = null
                _snackbarMessage.value = "Transaction restored"
            }
        }
    }

    fun duplicateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.duplicateTransaction(transaction)
            _snackbarMessage.value = "Transaction duplicated"
        }
    }

    fun saveBudget(budgetId: String, categoryId: String?, name: String, limit: Double) {
        viewModelScope.launch {
            repository.saveBudget(
                BudgetEntity(
                    id = budgetId,
                    categoryId = categoryId,
                    categoryName = name,
                    monthlyLimit = limit
                )
            )
            _snackbarMessage.value = "Budget updated"
            _isBudgetDialogOpen.value = false
        }
    }

    fun addNewCategory(name: String, type: TransactionType, icon: String, color: Long) {
        viewModelScope.launch {
            val id = name.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis()
            repository.addCategory(
                CategoryEntity(
                    id = id,
                    name = name,
                    type = type,
                    icon = icon,
                    color = color,
                    isCustom = true
                )
            )
            _snackbarMessage.value = "Category added"
            _isCategoryDialogOpen.value = false
        }
    }

    fun addNewAccount(name: String, type: String, openingBalance: Double, icon: String, color: Long) {
        viewModelScope.launch {
            val id = name.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis()
            repository.addAccount(
                AccountEntity(
                    id = id,
                    name = name,
                    type = type,
                    openingBalance = openingBalance,
                    icon = icon,
                    color = color
                )
            )
            _snackbarMessage.value = "Account created"
            _isAccountDialogOpen.value = false
        }
    }

    fun resetDataToDefaults() {
        viewModelScope.launch {
            repository.resetToSampleData()
            _snackbarMessage.value = "Reset to default sample data"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllTransactions()
            _snackbarMessage.value = "All transactions cleared"
        }
    }

    fun triggerExport(format: String) {
        viewModelScope.launch {
            val txs = uiState.value.transactions
            val content = if (format == "CSV") {
                repository.exportTransactionsCsv(txs)
            } else {
                repository.exportTransactionsJson(txs)
            }
            _exportContent.value = content
            _exportType.value = format
            _isExportDialogOpen.value = true
        }
    }

    fun closeExportDialog() {
        _isExportDialogOpen.value = false
    }

    fun setBudgetDialogOpen(open: Boolean) {
        _isBudgetDialogOpen.value = open
    }

    fun setAccountDialogOpen(open: Boolean) {
        _isAccountDialogOpen.value = open
    }

    fun setCategoryDialogOpen(open: Boolean) {
        _isCategoryDialogOpen.value = open
    }

    fun toggleAppLock(enable: Boolean) {
        _isAppLockEnabled.value = enable
        _snackbarMessage.value = if (enable) "App PIN Security Enabled" else "App Security Disabled"
    }

    fun unlockApp() {
        _isUnlocked.value = true
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

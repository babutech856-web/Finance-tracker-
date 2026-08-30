package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.local.BudgetDao
import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.util.SampleDataSeeder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val existingCats = categoryDao.getAllCategories().firstOrNull()
        if (existingCats.isNullOrEmpty()) {
            categoryDao.insertAll(SampleDataSeeder.DEFAULT_EXPENSE_CATEGORIES)
            categoryDao.insertAll(SampleDataSeeder.DEFAULT_INCOME_CATEGORIES)
        }

        val existingAccounts = accountDao.getAllAccounts().firstOrNull()
        if (existingAccounts.isNullOrEmpty()) {
            accountDao.insertAll(SampleDataSeeder.DEFAULT_ACCOUNTS)
        }

        val existingBudgets = budgetDao.getAllBudgets().firstOrNull()
        if (existingBudgets.isNullOrEmpty()) {
            budgetDao.insertAll(SampleDataSeeder.DEFAULT_BUDGETS)
        }

        val existingTransactions = transactionDao.getAllTransactions().firstOrNull()
        if (existingTransactions.isNullOrEmpty()) {
            transactionDao.insertAll(SampleDataSeeder.createInitialTransactions())
        }
    }

    suspend fun addTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteById(id)
    }

    suspend fun duplicateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val copy = transaction.copy(
            id = 0,
            dateMillis = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(copy)
    }

    suspend fun saveBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
    }

    suspend fun addCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    suspend fun addAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        accountDao.insertAccount(account)
    }

    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
        accountDao.clearAll()
        categoryDao.clearAll()
        budgetDao.clearAll()

        categoryDao.insertAll(SampleDataSeeder.DEFAULT_EXPENSE_CATEGORIES)
        categoryDao.insertAll(SampleDataSeeder.DEFAULT_INCOME_CATEGORIES)
        accountDao.insertAll(SampleDataSeeder.DEFAULT_ACCOUNTS)
        budgetDao.insertAll(SampleDataSeeder.DEFAULT_BUDGETS)
        transactionDao.insertAll(SampleDataSeeder.createInitialTransactions())
    }

    suspend fun clearAllTransactions() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
    }

    suspend fun exportTransactionsJson(transactions: List<TransactionEntity>): String = withContext(Dispatchers.Default) {
        val array = JSONArray()
        for (t in transactions) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("type", t.type.name)
            obj.put("amount", t.amount)
            obj.put("category", t.categoryName)
            obj.put("description", t.description)
            obj.put("account", t.fromAccount)
            obj.put("toAccount", t.toAccount ?: "")
            obj.put("dateMillis", t.dateMillis)
            obj.put("notes", t.notes)
            array.put(obj)
        }
        array.toString(2)
    }

    suspend fun exportTransactionsCsv(transactions: List<TransactionEntity>): String = withContext(Dispatchers.Default) {
        val sb = StringBuilder()
        sb.append("ID,Type,Amount,Category,Description,Account,ToAccount,DateMillis,Notes\n")
        for (t in transactions) {
            sb.append("${t.id},")
            sb.append("${t.type.name},")
            sb.append("${t.amount},")
            sb.append("\"${t.categoryName.replace("\"", "\"\"")}\",")
            sb.append("\"${t.description.replace("\"", "\"\"")}\",")
            sb.append("\"${t.fromAccount.replace("\"", "\"\"")}\",")
            sb.append("\"${(t.toAccount ?: "").replace("\"", "\"\"")}\",")
            sb.append("${t.dateMillis},")
            sb.append("\"${t.notes.replace("\"", "\"\"")}\"\n")
        }
        sb.toString()
    }
}

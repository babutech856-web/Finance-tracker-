package com.example.util

import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.util.Calendar

object SampleDataSeeder {

    val DEFAULT_ACCOUNTS = listOf(
        AccountEntity("cash", "Cash in Hand", "CASH", 10000.0, "payments", 0xFF1B8755),
        AccountEntity("bank", "Nabil Bank", "BANK", 50000.0, "account_balance", 0xFF006D5B),
        AccountEntity("esewa", "eSewa Wallet", "WALLET", 15000.0, "account_balance_wallet", 0xFF4CAF50),
        AccountEntity("khalti", "Khalti Wallet", "WALLET", 10450.0, "wallet", 0xFF5C2D91)
    )

    val DEFAULT_EXPENSE_CATEGORIES = listOf(
        CategoryEntity("food", "Food & Dining", TransactionType.EXPENSE, "restaurant", 0xFFF59E0B),
        CategoryEntity("transport", "Transport & Fuel", TransactionType.EXPENSE, "directions_car", 0xFF3B82F6),
        CategoryEntity("shopping", "Shopping", TransactionType.EXPENSE, "shopping_bag", 0xFFEC4899),
        CategoryEntity("bills", "Bills & Utilities", TransactionType.EXPENSE, "receipt_long", 0xFF8B5CF6),
        CategoryEntity("education", "Education", TransactionType.EXPENSE, "school", 0xFF10B981),
        CategoryEntity("health", "Health & Medical", TransactionType.EXPENSE, "local_hospital", 0xFFEF4444),
        CategoryEntity("entertainment", "Entertainment", TransactionType.EXPENSE, "movie", 0xFF06B6D4),
        CategoryEntity("rent", "Rent & Housing", TransactionType.EXPENSE, "home", 0xFF6366F1),
        CategoryEntity("other_expense", "Other Expense", TransactionType.EXPENSE, "more_horiz", 0xFF6B7280)
    )

    val DEFAULT_INCOME_CATEGORIES = listOf(
        CategoryEntity("salary", "Salary", TransactionType.INCOME, "work", 0xFF10B981),
        CategoryEntity("freelance", "Freelance", TransactionType.INCOME, "laptop_mac", 0xFF14B8A6),
        CategoryEntity("business", "Business", TransactionType.INCOME, "storefront", 0xFF8B5CF6),
        CategoryEntity("allowance", "Allowance", TransactionType.INCOME, "attach_money", 0xFFF97316),
        CategoryEntity("interest", "Interest / Dividends", TransactionType.INCOME, "trending_up", 0xFF0284C7),
        CategoryEntity("gift", "Gift", TransactionType.INCOME, "card_giftcard", 0xFFEC4899),
        CategoryEntity("other_income", "Other Income", TransactionType.INCOME, "savings", 0xFF6B7280)
    )

    val DEFAULT_BUDGETS = listOf(
        BudgetEntity("overall", null, "Monthly Budget", 50000.0),
        BudgetEntity("food", "food", "Food & Dining", 10000.0),
        BudgetEntity("transport", "transport", "Transport & Fuel", 6000.0),
        BudgetEntity("shopping", "shopping", "Shopping", 8000.0),
        BudgetEntity("entertainment", "entertainment", "Entertainment", 3000.0)
    )

    fun createInitialTransactions(): List<TransactionEntity> {
        val now = Calendar.getInstance()
        val list = mutableListOf<TransactionEntity>()

        fun timeOffset(daysAgo: Int, hour: Int, minute: Int): Long {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -daysAgo)
            c.set(Calendar.HOUR_OF_DAY, hour)
            c.set(Calendar.MINUTE, minute)
            c.set(Calendar.SECOND, 0)
            return c.timeInMillis
        }

        // Today's transactions
        list.add(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 450.0,
                categoryId = "food",
                categoryName = "Food & Dining",
                categoryIcon = "restaurant",
                categoryColor = 0xFFF59E0B,
                description = "Lunch at cafe",
                fromAccount = "eSewa Wallet",
                dateMillis = timeOffset(0, 13, 20),
                notes = "Sandwich & Cold Coffee"
            )
        )
        list.add(
            TransactionEntity(
                type = TransactionType.INCOME,
                amount = 5000.0,
                categoryId = "freelance",
                categoryName = "Freelance",
                categoryIcon = "laptop_mac",
                categoryColor = 0xFF14B8A6,
                description = "Freelance payment",
                fromAccount = "Bank",
                dateMillis = timeOffset(0, 10, 0),
                notes = "UI Design milestone payout"
            )
        )

        // Yesterday's transactions
        list.add(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 2000.0,
                categoryId = "transport",
                categoryName = "Transport & Fuel",
                categoryIcon = "directions_car",
                categoryColor = 0xFF3B82F6,
                description = "Fuel - Bike refill",
                fromAccount = "Cash in Hand",
                dateMillis = timeOffset(1, 17, 45),
                notes = "Octane fuel"
            )
        )
        list.add(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 1200.0,
                categoryId = "food",
                categoryName = "Food & Dining",
                categoryIcon = "restaurant",
                categoryColor = 0xFFF59E0B,
                description = "Dinner with friends",
                fromAccount = "eSewa Wallet",
                dateMillis = timeOffset(1, 20, 15),
                notes = "Momo & Chowmein"
            )
        )

        // 2 days ago
        list.add(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 5000.0,
                categoryId = "shopping",
                categoryName = "Shopping",
                categoryIcon = "shopping_bag",
                categoryColor = 0xFFEC4899,
                description = "Sneakers & Clothing",
                fromAccount = "Bank",
                dateMillis = timeOffset(2, 16, 30),
                notes = "New Road shopping"
            )
        )
        list.add(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 4200.0,
                categoryId = "transport",
                categoryName = "Transport & Fuel",
                categoryIcon = "directions_car",
                categoryColor = 0xFF3B82F6,
                description = "Taxi & Bus fare passes",
                fromAccount = "Cash in Hand",
                dateMillis = timeOffset(2, 9, 15)
            )
        )

        // 3 days ago
        list.add(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 4500.0,
                categoryId = "entertainment",
                categoryName = "Entertainment",
                categoryIcon = "movie",
                categoryColor = 0xFF06B6D4,
                description = "Concert Tickets & Snacks",
                fromAccount = "Khalti Wallet",
                dateMillis = timeOffset(3, 19, 0)
            )
        )
        list.add(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 3500.0,
                categoryId = "bills",
                categoryName = "Bills & Utilities",
                categoryIcon = "receipt_long",
                categoryColor = 0xFF8B5CF6,
                description = "Internet & Electricity Bill",
                fromAccount = "eSewa Wallet",
                dateMillis = timeOffset(3, 11, 20)
            )
        )

        // 4 days ago
        list.add(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 4800.0,
                categoryId = "food",
                categoryName = "Food & Dining",
                categoryIcon = "restaurant",
                categoryColor = 0xFFF59E0B,
                description = "Weekly Supermarket Groceries",
                fromAccount = "Nabil Bank",
                dateMillis = timeOffset(4, 15, 10)
            )
        )

        // 5 days ago
        list.add(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 6050.0,
                categoryId = "food",
                categoryName = "Food & Dining",
                categoryIcon = "restaurant",
                categoryColor = 0xFFF59E0B,
                description = "Family Dining & Bakery",
                fromAccount = "Bank",
                dateMillis = timeOffset(5, 18, 45)
            )
        )
        list.add(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 2850.0,
                categoryId = "health",
                categoryName = "Health & Medical",
                categoryIcon = "local_hospital",
                categoryColor = 0xFFEF4444,
                description = "Pharmacy & Vitamins",
                fromAccount = "Cash in Hand",
                dateMillis = timeOffset(5, 12, 0)
            )
        )

        // Earlier this month
        list.add(
            TransactionEntity(
                type = TransactionType.INCOME,
                amount = 115000.0,
                categoryId = "salary",
                categoryName = "Salary",
                categoryIcon = "work",
                categoryColor = 0xFF10B981,
                description = "Monthly Salary Direct Deposit",
                fromAccount = "Nabil Bank",
                dateMillis = timeOffset(12, 9, 30),
                notes = "Tech Corp Monthly Payroll"
            )
        )

        return list
    }
}

package com.example.util

import com.example.data.model.CurrencyConfig
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType

data class InsightCard(
    val title: String,
    val description: String,
    val icon: String, // icon key
    val isPositive: Boolean
)

object FinancialInsightsEngine {

    fun generateInsights(
        transactions: List<TransactionEntity>,
        currency: CurrencyConfig
    ): List<InsightCard> {
        if (transactions.isEmpty()) {
            return listOf(
                InsightCard(
                    title = "Getting Started",
                    description = "No insights yet. Keep tracking your expenses to see smart financial patterns.",
                    icon = "info",
                    isPositive = true
                )
            )
        }

        val insights = mutableListOf<InsightCard>()
        val startOfMonth = DateTimeUtils.getStartOfMonth()
        val startOfLastMonth = DateTimeUtils.getMonthsAgo(1)

        val thisMonthExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE && it.dateMillis >= startOfMonth
        }
        val thisMonthIncome = transactions.filter {
            it.type == TransactionType.INCOME && it.dateMillis >= startOfMonth
        }

        val lastMonthExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE && it.dateMillis >= startOfLastMonth && it.dateMillis < startOfMonth
        }

        val totalIncome = thisMonthIncome.sumOf { it.amount }
        val totalExpense = thisMonthExpenses.sumOf { it.amount }
        val savings = totalIncome - totalExpense

        // 1. Highest Spending Category
        val expenseByCategory = thisMonthExpenses.groupBy { it.categoryName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        if (expenseByCategory.isNotEmpty()) {
            val topCategory = expenseByCategory.first()
            val pct = if (totalExpense > 0) (topCategory.second / totalExpense * 100).toInt() else 0
            insights.add(
                InsightCard(
                    title = "Top Spending Category",
                    description = "Your highest spending category is ${topCategory.first} (${CurrencyFormatter.format(topCategory.second, currency)} • $pct% of expenses).",
                    icon = "pie_chart",
                    isPositive = false
                )
            )
        }

        // 2. Savings Rate Insight
        if (totalIncome > 0) {
            val rate = (savings / totalIncome * 100).coerceIn(0.0, 100.0)
            if (savings > 0) {
                insights.add(
                    InsightCard(
                        title = "Healthy Savings Rate",
                        description = "You saved ${CurrencyFormatter.format(savings, currency)} this month with a savings rate of ${String.format(java.util.Locale.US, "%.1f", rate)}%.",
                        icon = "savings",
                        isPositive = true
                    )
                )
            } else {
                insights.add(
                    InsightCard(
                        title = "Expenses Exceeded Income",
                        description = "Your spending is currently higher than your income for this period by ${CurrencyFormatter.format(-savings, currency)}.",
                        icon = "warning",
                        isPositive = false
                    )
                )
            }
        }

        // 3. Category Comparison Insight
        if (lastMonthExpenses.isNotEmpty() && expenseByCategory.isNotEmpty()) {
            val topCatName = expenseByCategory.first().first
            val lastMonthCatExpense = lastMonthExpenses.filter { it.categoryName == topCatName }.sumOf { it.amount }
            if (lastMonthCatExpense > 0) {
                val diff = expenseByCategory.first().second - lastMonthCatExpense
                val diffPct = (diff / lastMonthCatExpense * 100).toInt()
                if (diffPct < 0) {
                    insights.add(
                        InsightCard(
                            title = "Reduced Spending",
                            description = "You spent ${Math.abs(diffPct)}% less on $topCatName compared to the previous period.",
                            icon = "trending_down",
                            isPositive = true
                        )
                    )
                } else if (diffPct > 0) {
                    insights.add(
                        InsightCard(
                            title = "Increased Spending",
                            description = "$topCatName expenses increased by $diffPct% compared to the previous period.",
                            icon = "trending_up",
                            isPositive = false
                        )
                    )
                }
            }
        } else {
            // General activity insight
            val transactionCount = transactions.size
            insights.add(
                InsightCard(
                    title = "Consistent Tracking",
                    description = "You have recorded $transactionCount transactions. Clear records lead to disciplined wealth growth.",
                    icon = "verified",
                    isPositive = true
                )
            )
        }

        return insights
    }
}

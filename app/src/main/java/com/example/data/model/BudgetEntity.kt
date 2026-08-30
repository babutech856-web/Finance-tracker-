package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String, // "overall" or categoryId e.g. "food"
    val categoryId: String? = null, // null for overall monthly budget
    val categoryName: String = "Overall Monthly Budget",
    val monthlyLimit: Double,
    val monthYear: String = "" // e.g. "2026-08" or empty for persistent monthly limit
)

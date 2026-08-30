package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType, // EXPENSE, INCOME, TRANSFER
    val amount: Double,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: Long,
    val description: String = "",
    val fromAccount: String = "Cash", // Cash, Bank, eSewa, Khalti, etc.
    val toAccount: String? = null,    // For transfers
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: TransactionType, // EXPENSE or INCOME
    val icon: String,
    val color: Long,
    val isCustom: Boolean = false
)

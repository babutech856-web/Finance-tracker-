package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // CASH, BANK, WALLET, OTHER
    val openingBalance: Double = 0.0,
    val icon: String = "account_balance",
    val color: Long = 0xFF006D5B
)

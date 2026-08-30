package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {

    fun getIcon(name: String): ImageVector {
        return when (name.lowercase().trim()) {
            "restaurant", "food", "dining" -> Icons.Default.Restaurant
            "directions_car", "transport", "fuel", "car" -> Icons.Default.DirectionsCar
            "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
            "receipt_long", "bills", "utilities" -> Icons.Default.ReceiptLong
            "school", "education" -> Icons.Default.School
            "local_hospital", "health", "medical" -> Icons.Default.LocalHospital
            "movie", "entertainment" -> Icons.Default.Movie
            "home", "rent", "housing" -> Icons.Default.Home
            "work", "salary" -> Icons.Default.Work
            "laptop_mac", "freelance" -> Icons.Default.LaptopMac
            "storefront", "business" -> Icons.Default.Storefront
            "attach_money", "allowance" -> Icons.Default.AttachMoney
            "trending_up", "interest", "investment" -> Icons.Default.TrendingUp
            "card_giftcard", "gift" -> Icons.Default.CardGiftcard
            "savings" -> Icons.Default.Savings
            "payments", "cash" -> Icons.Default.Payments
            "account_balance", "bank" -> Icons.Default.AccountBalance
            "account_balance_wallet", "wallet", "esewa", "khalti" -> Icons.Default.AccountBalanceWallet
            "swap_horiz", "transfer" -> Icons.Default.SwapHoriz
            else -> Icons.Default.Category
        }
    }
}

package com.example.data.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class NumberFormatStyle {
    NEPALI_INDIAN, // e.g. 10,00,000
    WESTERN        // e.g. 1,000,000
}

data class CurrencyConfig(
    val code: String = "NPR",
    val symbol: String = "Rs. ",
    val name: String = "Nepali Rupee",
    val formatStyle: NumberFormatStyle = NumberFormatStyle.NEPALI_INDIAN
) {
    companion object {
        val NPR = CurrencyConfig("NPR", "Rs. ", "Nepali Rupee (NPR)", NumberFormatStyle.NEPALI_INDIAN)
        val USD = CurrencyConfig("USD", "$", "US Dollar (USD)", NumberFormatStyle.WESTERN)
        val EUR = CurrencyConfig("EUR", "€", "Euro (EUR)", NumberFormatStyle.WESTERN)
        val GBP = CurrencyConfig("GBP", "£", "British Pound (GBP)", NumberFormatStyle.WESTERN)
        val INR = CurrencyConfig("INR", "₹", "Indian Rupee (INR)", NumberFormatStyle.NEPALI_INDIAN)
        val AUD = CurrencyConfig("AUD", "A$", "Australian Dollar (AUD)", NumberFormatStyle.WESTERN)
        val CAD = CurrencyConfig("CAD", "C$", "Canadian Dollar (CAD)", NumberFormatStyle.WESTERN)
        val JPY = CurrencyConfig("JPY", "¥", "Japanese Yen (JPY)", NumberFormatStyle.WESTERN)

        val SUPPORTED_CURRENCIES = listOf(NPR, USD, EUR, GBP, INR, AUD, CAD, JPY)
    }
}

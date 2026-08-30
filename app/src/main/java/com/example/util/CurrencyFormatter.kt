package com.example.util

import com.example.data.model.CurrencyConfig
import com.example.data.model.NumberFormatStyle
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

object CurrencyFormatter {

    fun format(
        amount: Double,
        currency: CurrencyConfig = CurrencyConfig.NPR,
        showDecimalsIfWhole: Boolean = false,
        includeSign: Boolean = false
    ): String {
        val isNegative = amount < 0
        val absAmount = abs(amount)
        val signStr = when {
            includeSign && amount > 0 -> "+"
            isNegative -> "−"
            else -> ""
        }

        val formattedNumber = when (currency.formatStyle) {
            NumberFormatStyle.NEPALI_INDIAN -> formatNepaliIndian(absAmount, showDecimalsIfWhole)
            NumberFormatStyle.WESTERN -> formatWestern(absAmount, showDecimalsIfWhole)
        }

        return if (includeSign && isNegative) {
            "− ${currency.symbol}$formattedNumber"
        } else if (includeSign && amount > 0) {
            "+ ${currency.symbol}$formattedNumber"
        } else if (isNegative) {
            "− ${currency.symbol}$formattedNumber"
        } else {
            "${currency.symbol}$formattedNumber"
        }
    }

    fun formatNepaliIndian(amount: Double, showDecimalsIfWhole: Boolean): String {
        val longVal = amount.toLong()
        val decimalPart = amount - longVal
        val decimalStr = if (decimalPart > 0.009 || showDecimalsIfWhole) {
            val roundedDec = (decimalPart * 100).roundToLong()
            "." + String.format(Locale.US, "%02d", roundedDec)
        } else {
            ""
        }

        val numStr = longVal.toString()
        if (numStr.length <= 3) {
            return numStr + decimalStr
        }

        val lastThree = numStr.substring(numStr.length - 3)
        val remaining = numStr.substring(0, numStr.length - 3)

        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count % 2 == 0 && i != 0) {
                sb.append(",")
            }
        }
        val prefix = sb.reverse().toString()
        return "$prefix,$lastThree$decimalStr"
    }

    private fun formatWestern(amount: Double, showDecimalsIfWhole: Boolean): String {
        val pattern = if (showDecimalsIfWhole || (amount % 1.0 != 0.0)) {
            "#,##0.00"
        } else {
            "#,##0"
        }
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat(pattern, symbols)
        return df.format(amount)
    }

    fun formatCompact(amount: Double, currency: CurrencyConfig = CurrencyConfig.NPR): String {
        val absVal = abs(amount)
        val sign = if (amount < 0) "− " else ""
        return when {
            currency.formatStyle == NumberFormatStyle.NEPALI_INDIAN -> {
                when {
                    absVal >= 10_000_000 -> "${sign}${currency.symbol}${String.format(Locale.US, "%.1f", absVal / 10_000_000)} Cr"
                    absVal >= 100_000 -> "${sign}${currency.symbol}${String.format(Locale.US, "%.1f", absVal / 100_000)} L"
                    absVal >= 1_000 -> "${sign}${currency.symbol}${String.format(Locale.US, "%.1f", absVal / 1_000)}k"
                    else -> format(amount, currency)
                }
            }
            else -> {
                when {
                    absVal >= 1_000_000_000 -> "${sign}${currency.symbol}${String.format(Locale.US, "%.1f", absVal / 1_000_000_000)}B"
                    absVal >= 1_000_000 -> "${sign}${currency.symbol}${String.format(Locale.US, "%.1f", absVal / 1_000_000)}M"
                    absVal >= 1_000 -> "${sign}${currency.symbol}${String.format(Locale.US, "%.1f", absVal / 1_000)}k"
                    else -> format(amount, currency)
                }
            }
        }
    }
}

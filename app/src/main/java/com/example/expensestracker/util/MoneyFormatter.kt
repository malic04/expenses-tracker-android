package com.example.expensestracker.util

import com.example.expensestracker.data.Currency
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MoneyFormatter {

    private val df = DecimalFormat(
        "#,##0.00",
        DecimalFormatSymbols(Locale.GERMANY)
    )

    fun format(amount: Double, currency: Currency): String {
        val formatted = df.format(amount)
        return when (currency) {
            Currency.BAM -> "$formatted KM"
            Currency.EUR -> "$formatted €"
        }
    }
}

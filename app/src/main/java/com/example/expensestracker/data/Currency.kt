package com.example.expensestracker.data

enum class Currency {
    BAM,
    EUR;

    fun convert(amount: Double, to: Currency): Double {
        if (this == to) return amount

        return when (this) {
            BAM -> amount / 1.96   // BAM → EUR
            EUR -> amount * 1.96   // EUR → BAM
        }
    }
}

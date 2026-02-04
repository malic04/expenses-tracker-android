package com.example.expensestracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.expensestracker.data.AppDatabase
import com.example.expensestracker.data.Currency
import com.example.expensestracker.data.Expense
import com.example.expensestracker.data.SettingsManager
import com.example.expensestracker.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val settings = SettingsManager(application)

    // FILTERI (STATE FLOW)
    private val filterFrom = MutableStateFlow(0L)
    private val filterTo = MutableStateFlow(Long.MAX_VALUE)
    private val filterCategory = MutableStateFlow<String?>(null)

    // VALUTA
    val currency = MutableStateFlow(settings.getCurrency())

    // LISTA TROŠKOVA – AUTOMATSKI SE OSVJEŽAVA
    val expenses: LiveData<List<Expense>> =
        combine(filterFrom, filterTo, filterCategory) { from, to, category ->
            Triple(from, to, category)
        }.flatMapLatest { (from, to, category) ->
            repository.getFilteredExpenses(from, to, category)
        }.asLiveData()

    val totalAmount: LiveData<Double> =
        expenses.map { list -> list.sumOf { it.amount } }

    val totalByCategory: LiveData<Map<String, Double>> =
        expenses.map { list ->
            list.groupBy { it.category }
                .mapValues { it.value.sumOf { e -> e.amount } }
        }

    init {
        val dao = AppDatabase
            .getInstance(application)
            .expenseDao()

        repository = ExpenseRepository(dao)

        // default: ovaj mjesec
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        filterFrom.value = cal.timeInMillis
        filterTo.value = System.currentTimeMillis()
    }

    // ---------------- FILTER ----------------

    fun setFilter(from: Long, to: Long, category: String?) {
        filterFrom.value = from
        filterTo.value = to
        filterCategory.value = category
    }

    // ---------------- CURRENCY ----------------

    fun setCurrency(newCurrency: Currency) {
        val oldCurrency = currency.value
        if (oldCurrency == newCurrency) return

        currency.value = newCurrency
        settings.setCurrency(newCurrency)

        viewModelScope.launch {
            expenses.value?.forEach { expense ->
                val converted =
                    oldCurrency.convert(expense.amount, newCurrency)

                repository.update(expense.copy(amount = converted))
            }
        }
    }

    // ---------------- CRUD ----------------

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        note: String?,
        date: Long
    ) {
        viewModelScope.launch {
            repository.insert(
                Expense(
                    title = title,
                    amount = amount,
                    category = category,
                    note = note,
                    date = date
                )
            )
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.update(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }
}

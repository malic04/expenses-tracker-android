package com.example.expensestracker.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.expensestracker.data.AppDatabase
import com.example.expensestracker.data.Currency
import com.example.expensestracker.data.Expense
import com.example.expensestracker.data.SettingsManager
import com.example.expensestracker.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val settings = SettingsManager(application)

    private val filterFrom = MutableLiveData<Long>()
    private val filterTo = MutableLiveData<Long>()
    private val filterCategory = MutableLiveData<String?>(null)

    val currency: MutableLiveData<Currency> =
        MutableLiveData(settings.getCurrency())

    val expenses: LiveData<List<Expense>>
    val totalAmount: LiveData<Double>
    val totalByCategory: LiveData<Map<String, Double>>

    init {
        val dao = AppDatabase
            .getInstance(application)
            .expenseDao()

        repository = ExpenseRepository(dao)

        // Default: ovaj mjesec
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        filterFrom.value = cal.timeInMillis
        filterTo.value = System.currentTimeMillis()

        expenses = MediatorLiveData<List<Expense>>().apply {
            fun reload() {
                val source = repository.getFilteredExpenses(
                    filterFrom.value ?: 0L,
                    filterTo.value ?: Long.MAX_VALUE,
                    filterCategory.value
                )
                addSource(source) { value = it }
            }

            addSource(filterFrom) { reload() }
            addSource(filterTo) { reload() }
            addSource(filterCategory) { reload() }
        }

        totalAmount = expenses.map { list ->
            list.sumOf { it.amount }
        }

        totalByCategory = expenses.map { list ->
            list.groupBy { it.category }
                .mapValues { entry ->
                    entry.value.sumOf { it.amount }
                }
        }
    }

    // ---------------- FILTER ----------------

    fun setFilter(from: Long, to: Long, category: String?) {
        filterFrom.value = from
        filterTo.value = to
        filterCategory.value = category
    }

    // ---------------- CURRENCY ----------------

    fun setCurrency(newCurrency: Currency) {
        val oldCurrency = currency.value ?: Currency.BAM
        if (oldCurrency == newCurrency) return

        currency.value = newCurrency
        settings.setCurrency(newCurrency)

        val currentExpenses = expenses.value ?: return

        viewModelScope.launch {
            currentExpenses.forEach { expense ->
                val convertedAmount =
                    oldCurrency.convert(expense.amount, newCurrency)

                repository.update(
                    expense.copy(amount = convertedAmount)
                )
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

package com.example.expensestracker.repository

import androidx.lifecycle.LiveData
import com.example.expensestracker.data.Expense
import com.example.expensestracker.data.ExpenseDao

class ExpenseRepository(
    private val expenseDao: ExpenseDao
) {

    fun getFilteredExpenses(
        from: Long,
        to: Long,
        category: String?
    ): LiveData<List<Expense>> {
        return expenseDao.getFilteredExpenses(from, to, category)
    }

    suspend fun insert(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun update(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
}

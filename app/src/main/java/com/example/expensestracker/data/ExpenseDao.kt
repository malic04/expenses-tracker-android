package com.example.expensestracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseDao {

    @Query("""
        SELECT * FROM expenses
        WHERE date BETWEEN :from AND :to
        AND (:category IS NULL OR category = :category)
        ORDER BY date DESC
    """)
    fun getFilteredExpenses(
        from: Long,
        to: Long,
        category: String?
    ): LiveData<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}

package com.example.expensestracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensestracker.R
import com.example.expensestracker.data.Currency
import com.example.expensestracker.data.Expense
import com.example.expensestracker.util.MoneyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter(
    private val onClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val items = mutableListOf<Expense>()
    private var currency: Currency = Currency.BAM

    private val dateFormat =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun submitList(list: List<Expense>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): Expense =
        items[position]

    fun setCurrency(currency: Currency) {
        this.currency = currency
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ExpenseViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ExpenseViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView =
            itemView.findViewById(R.id.tvTitle)
        private val tvAmount: TextView =
            itemView.findViewById(R.id.tvAmount)
        private val tvMeta: TextView =
            itemView.findViewById(R.id.tvMeta)

        fun bind(expense: Expense) {
            tvTitle.text = expense.title
            tvAmount.text =
                MoneyFormatter.format(expense.amount, currency)

            val dateStr =
                dateFormat.format(Date(expense.date))
            tvMeta.text =
                "${expense.category} • $dateStr"

            itemView.setOnClickListener {
                onClick(expense)
            }
        }
    }
}

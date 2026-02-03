package com.example.expensestracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensestracker.R
import com.example.expensestracker.data.Currency
import com.example.expensestracker.data.Expense
import com.example.expensestracker.util.MoneyFormatter
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private val onClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DIFF) {

    private var currency: Currency = Currency.BAM

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(
                oldItem: Expense,
                newItem: Expense
            ) = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: Expense,
                newItem: Expense
            ) = oldItem == newItem
        }
    }

    fun setCurrency(currency: Currency) {
        this.currency = currency
        notifyItemRangeChanged(0, itemCount)
    }

    fun getItemAt(position: Int): Expense =
        currentList[position]

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
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)

        private val df = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        fun bind(expense: Expense) {
            tvTitle.text = expense.title
            tvAmount.text = MoneyFormatter.format(expense.amount, currency)
            tvMeta.text = "${expense.category} • ${df.format(Date(expense.date))}"

            itemView.setOnClickListener { onClick(expense) }
        }
    }
}

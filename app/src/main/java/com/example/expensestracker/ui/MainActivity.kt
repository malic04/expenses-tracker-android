package com.example.expensestracker.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensestracker.R
import com.example.expensestracker.data.Currency
import com.example.expensestracker.data.Expense
import com.example.expensestracker.data.SettingsManager
import com.example.expensestracker.util.MoneyFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(application)
    }

    private lateinit var settings: SettingsManager

    private lateinit var rvExpenses: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var tvCategorySummary: TextView
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCurrency: Spinner
    private lateinit var switchDarkMode: Switch
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: ExpenseAdapter

    private var lastCategory: String? = null


    private val categories =
        listOf("Sve", "Hrana", "Prijevoz", "Stan", "Zabava", "Ostalo")
    private val currencies =
        listOf("BAM (KM)", "EUR (€)")

    private val dateFormat =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {

        settings = SettingsManager(this)

        AppCompatDelegate.setDefaultNightMode(
            if (settings.isDarkModeEnabled())
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupDarkModeSwitch()
        setupRecyclerView()
        setupCategorySpinner()
        setupCurrencySpinner()
        observeViewModel()
        setupFab()
        setupSwipeToDelete()
    }

    private fun initViews() {
        rvExpenses = findViewById(R.id.rvExpenses)
        tvTotal = findViewById(R.id.tvTotal)
        tvCategorySummary = findViewById(R.id.tvCategorySummary)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCurrency = findViewById(R.id.spinnerCurrency)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        fabAdd = findViewById(R.id.fabAdd)
    }

    private fun setupDarkModeSwitch() {
        switchDarkMode.isChecked = settings.isDarkModeEnabled()

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            settings.setDarkMode(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter { showAddEditDialog(it) }
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = adapter
    }

    private fun setupCategorySpinner() {
        val a = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = a
        spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p: AdapterView<*>?, v: View?, pos: Int, id: Long
                ) = applyFilter()
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupCurrencySpinner() {
        val a = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = a
        spinnerCurrency.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p: AdapterView<*>?, v: View?, pos: Int, id: Long
                ) {
                    viewModel.setCurrency(if (pos == 0) Currency.BAM else Currency.EUR)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun observeViewModel() {
        viewModel.expenses.observe(this) { adapter.submitList(it) }
        viewModel.currency.observe(this) { adapter.setCurrency(it) }
        viewModel.totalAmount.observe(this) {
            tvTotal.text = "Ukupno: ${MoneyFormatter.format(it, viewModel.currency.value!!)}"
        }
        viewModel.totalByCategory.observe(this) { map ->
            tvCategorySummary.text =
                if (map.isEmpty()) "Nema troškova"
                else map.entries.joinToString("\n") {
                    "${it.key}: ${MoneyFormatter.format(it.value, viewModel.currency.value!!)}"
                }
        }
    }

    private fun applyFilter() {
        val selected = spinnerCategory.selectedItem as String
        if (selected == lastCategory) return

        lastCategory = selected

        viewModel.setFilter(
            0L,
            Long.MAX_VALUE,
            if (selected == "Sve") null else selected
        )
    }


    private fun setupFab() {
        fabAdd.setOnClickListener { showAddEditDialog(null) }
    }

    private fun setupSwipeToDelete() {
        ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                    confirmDelete(adapter.getItemAt(vh.adapterPosition))
                }
            }
        ).attachToRecyclerView(rvExpenses)
    }

    private fun confirmDelete(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Brisanje")
            .setMessage("Obrisati '${expense.title}'?")
            .setPositiveButton("Obriši") { _, _ ->
                viewModel.deleteExpense(expense)
            }
            .setNegativeButton("Odustani") { d, _ ->
                d.dismiss()
                adapter.notifyDataSetChanged()
            }
            .show()
    }




private fun showAddEditDialog(expense: Expense?) {
        val view = layoutInflater.inflate(R.layout.dialog_add_expense, null)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val etNote = view.findViewById<EditText>(R.id.etNote)
        val spinner = view.findViewById<Spinner>(R.id.spinnerDialogCategory)
        val btnDate = view.findViewById<Button>(R.id.btnDate)

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories.drop(1)
        )

        val cal = Calendar.getInstance()

        expense?.let {
            etTitle.setText(it.title)
            etAmount.setText(it.amount.toString())
            etNote.setText(it.note ?: "")
            cal.timeInMillis = it.date
            spinner.setSelection(categories.drop(1).indexOf(it.category))
        }

        btnDate.text = dateFormat.format(cal.time)

        btnDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    cal.set(y, m, d)
                    btnDate.text = dateFormat.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle(if (expense == null) "Novi trošak" else "Uredi trošak")
            .setView(view)
            .setPositiveButton("Spremi") { _, _ ->
                val title = etTitle.text.toString().trim()
                val amount = etAmount.text.toString().toDoubleOrNull()
                val category = spinner.selectedItem as String
                val note = etNote.text.toString().trim().ifEmpty { null }

                if (title.isBlank() || amount == null || amount <= 0) return@setPositiveButton

                if (expense == null) {
                    viewModel.addExpense(title, amount, category, note, cal.timeInMillis)
                } else {
                    viewModel.updateExpense(
                        expense.copy(
                            title = title,
                            amount = amount,
                            category = category,
                            note = note,
                            date = cal.timeInMillis
                        )
                    )
                }
            }
            .setNegativeButton("Odustani", null)
            .show()
    }
}

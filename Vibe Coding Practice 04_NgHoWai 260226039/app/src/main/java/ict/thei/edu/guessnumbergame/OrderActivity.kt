package ict.thei.edu.guessnumbergame

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OrderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        val cbCustomer1 = findViewById<CheckBox>(R.id.cbCustomer1)
        val cbCustomer2 = findViewById<CheckBox>(R.id.cbCustomer2)
        val rgItems = findViewById<RadioGroup>(R.id.rgItems)
        val btnConfirmOrder = findViewById<Button>(R.id.btnConfirmOrder)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val btnReturn = findViewById<Button>(R.id.btnReturn)

        // Confirm
        btnConfirmOrder.setOnClickListener {
            val selectedCustomers = mutableListOf<String>()
            if (cbCustomer1.isChecked) selectedCustomers.add("Customer 1")
            if (cbCustomer2.isChecked) selectedCustomers.add("Customer 2")

            val customerStr = selectedCustomers.joinToString(" & ")

            val itemStr = when (rgItems.checkedRadioButtonId) {
                R.id.rbBreakfast -> "Breakfast"
                R.id.rbLunch -> "Lunch"
                R.id.rbDinner -> "Dinner"
                else -> ""
            }

            if (customerStr.isNotEmpty() && itemStr.isNotEmpty()) {
                tvResult.text = "$customerStr select $itemStr."
            } else {
                tvResult.text = "Please select item:"
            }
        }

        //  Return Game UI
        btnReturn.setOnClickListener {
            finish()
        }
    }
}
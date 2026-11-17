package ict.thei.edu.guessnumbergame

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val etConfigUser = findViewById<EditText>(R.id.etConfigUser)
        val etConfigPass = findViewById<EditText>(R.id.etConfigPass)
        val rgMaxRange = findViewById<RadioGroup>(R.id.rgMaxRange)
        val btnSaveConfig = findViewById<Button>(R.id.btnSaveConfig)
        val btnBackToGame = findViewById<Button>(R.id.btnBackToGame)

        val sharedPref = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)

        etConfigUser.setText(sharedPref.getString("USERNAME", "admin"))
        etConfigPass.setText(sharedPref.getString("PASSWORD", "1234"))

        when (sharedPref.getInt("MAX_RANGE", 100)) {
            50 -> rgMaxRange.check(R.id.rbEasy)
            500 -> rgMaxRange.check(R.id.rbHard)
            else -> rgMaxRange.check(R.id.rbNormal)
        }

        //  SharedPreferences
        btnSaveConfig.setOnClickListener {
            val newUser = etConfigUser.text.toString().trim()
            val newPass = etConfigPass.text.toString().trim()

            if (newUser.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Username and password cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedMaxRange = when (rgMaxRange.checkedRadioButtonId) {
                R.id.rbEasy -> 50
                R.id.rbHard -> 500
                else -> 100
            }

            sharedPref.edit()
                .putString("USERNAME", newUser)
                .putString("PASSWORD", newPass)
                .putString("CURRENT_PLAYER", newUser)
                .putInt("MAX_RANGE", selectedMaxRange)
                .apply()

            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
            finish() // MainActivity
        }

        btnBackToGame.setOnClickListener {
            finish()
        }
    }
}
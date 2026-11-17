package ict.thei.edu.guessnumbergame

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val cbRemember = findViewById<CheckBox>(R.id.cbRemember)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // SharedPreferences
        val sharedPref = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        val savedUser = sharedPref.getString("USERNAME", "admin") ?: "admin"
        val savedPass = sharedPref.getString("PASSWORD", "1234") ?: "1234"
        val isRemembered = sharedPref.getBoolean("REMEMBER", false)

        if (isRemembered) {
            etUsername.setText(savedUser)
            etPassword.setText(savedPass)
            cbRemember.isChecked = true
        }

        btnLogin.setOnClickListener {
            val inputUser = etUsername.text.toString().trim()
            val inputPass = etPassword.text.toString().trim()

            val validUser = sharedPref.getString("USERNAME", "admin")
            val validPass = sharedPref.getString("PASSWORD", "1234")

            if (inputUser == validUser && inputPass == validPass) {
                // SharedPreferences
                val editor = sharedPref.edit()
                editor.putBoolean("REMEMBER", cbRemember.isChecked)
                editor.putString("CURRENT_PLAYER", inputUser)
                editor.apply()

                Toast.makeText(this, "Welcome, $inputUser!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid Username or Password!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
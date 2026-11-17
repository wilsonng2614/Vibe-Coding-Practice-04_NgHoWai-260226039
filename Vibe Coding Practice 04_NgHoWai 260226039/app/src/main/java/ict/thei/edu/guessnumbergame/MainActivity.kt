package ict.thei.edu.guessnumbergame

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

/**
 * MainActivity class: Manages the core logic for the "Number Master" (Guess Number) game,
 * while also serving as the central navigation hub for the entire Game Center
 * (providing access to Bingo, Tic-Tac-Toe, the Meal Order system, and the Configuration screen).
 */
class MainActivity : AppCompatActivity() {

    // Core game state variables: target secret number, current min/max bounds, configured max bound, and guess count
    private var targetNumber: Int = 0
    private var minRange: Int = 0
    private var maxRange: Int = 100
    private var configuredMaxRange: Int = 100
    private var attempts: Int = 0

    // Declare UI view components
    private lateinit var tvPlayerInfo: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvRange: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind UI components from the layout
        tvPlayerInfo = findViewById(R.id.tvPlayerInfo)
        tvMessage = findViewById(R.id.tvMessage)
        tvRange = findViewById(R.id.tvRange)
        val etInput = findViewById<EditText>(R.id.etInput)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)
        val btnRestart = findViewById<Button>(R.id.btnRestart)
        val btnPlayBingo = findViewById<Button>(R.id.btnPlayBingo)
        val btnPlayTicTacToe = findViewById<Button>(R.id.btnPlayTicTacToe)
        val btnConfig = findViewById<Button>(R.id.btnConfig)
        val btnGoToClasswork3 = findViewById<Button>(R.id.btnGoToClasswork3)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Load player settings from SharedPreferences and initialize a new game round
        loadPreferencesAndResetGame()

        // Click listener for the "GUESS" button: validates player input and narrows down the number range
        btnConfirm.setOnClickListener {
            val inputStr = etInput.text.toString().trim()
            val guess = inputStr.toIntOrNull()

            if (guess == null) {
                tvMessage.text = "⚠️ Please enter a valid number!"
            } else if (guess !in minRange..maxRange) {
                tvMessage.text = "🚫 Out of bounds! Stay within range."
            } else {
                attempts++
                when {
                    guess == targetNumber -> {
                        tvMessage.text = "🎉 Bingo! Solved in $attempts tries!"
                        tvRange.text = "Answer: $targetNumber"
                    }
                    guess < targetNumber -> {
                        // If the guessed number is too low, raise the minimum bound to guess + 1
                        minRange = guess + 1
                        tvMessage.text = "🔼 Too low! Try higher (Tries: $attempts)"
                        tvRange.text = "$minRange  ~  $maxRange"
                    }
                    else -> {
                        // If the guessed number is too high, lower the maximum bound to guess - 1
                        maxRange = guess - 1
                        tvMessage.text = "🔽 Too high! Try lower (Tries: $attempts)"
                        tvRange.text = "$minRange  ~  $maxRange"
                    }
                }
            }
            etInput.text.clear()
        }

        // Click listener for the "Reset" button: restarts the number guessing game
        btnRestart.setOnClickListener {
            resetGame()
            etInput.text.clear()
        }

        // Navigate to the 5x5 Bingo game (integrated with SQLiteDatabase)
        btnPlayBingo.setOnClickListener {
            startActivity(Intent(this, BingoActivity::class.java))
        }

        // Navigate to the Tic-Tac-Toe mini-game
        btnPlayTicTacToe.setOnClickListener {
            startActivity(Intent(this, TicTacToeActivity::class.java))
        }

        // Navigate to the Configuration settings screen
        btnConfig.setOnClickListener {
            startActivity(Intent(this, ConfigActivity::class.java))
        }

        // Navigate to the Classwork 3 Meal Order screen
        btnGoToClasswork3.setOnClickListener {
            startActivity(Intent(this, OrderActivity::class.java))
        }

        // Log out and return to LoginActivity
        btnLogout.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Automatically checks and applies the latest player username and difficulty range
     * when returning to the main screen from the Configuration activity.
     */
    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        val player = sharedPref.getString("CURRENT_PLAYER", "admin")
        val newMax = sharedPref.getInt("MAX_RANGE", 100)

        tvPlayerInfo.text = "👤 Player: $player"

        if (newMax != configuredMaxRange) {
            configuredMaxRange = newMax
            resetGame()
        }
    }

    /**
     * Reads player profile information and difficulty settings from SharedPreferences.
     */
    private fun loadPreferencesAndResetGame() {
        val sharedPref = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        val player = sharedPref.getString("CURRENT_PLAYER", "admin")
        configuredMaxRange = sharedPref.getInt("MAX_RANGE", 100)
        tvPlayerInfo.text = "👤 Player: $player"
        resetGame()
    }

    /**
     * Resets the secret target number, range bounds, and attempt counter for a new round.
     */
    private fun resetGame() {
        minRange = 0
        maxRange = configuredMaxRange
        attempts = 0
        targetNumber = Random.nextInt(minRange, maxRange + 1)
        tvMessage.text = "Guess the secret number!"
        tvRange.text = "$minRange  ~  $maxRange"
    }
}
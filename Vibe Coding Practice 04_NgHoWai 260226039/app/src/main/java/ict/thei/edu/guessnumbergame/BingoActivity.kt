package ict.thei.edu.guessnumbergame

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BingoActivity class: Implements the "Authentic Lucky Draw Machine + Dual 5x5 Boards Live Battle" mode,
 * fully integrated with SQLiteDatabase.
 *
 * Game Rules and Mechanics:
 * 1. The screen simultaneously displays the "Player's 5x5 Bingo Card" and the "CPU's 5x5 Bingo Card",
 *    each independently shuffled with numbers from 1 to 25.
 * 2. Board cells cannot be manually clicked to prevent cheating; players must click "Draw Lucky Ball"
 *    to randomly draw a number.
 * 3. Each time a ball is drawn, the corresponding number is highlighted on both the player's board (purple)
 *    and the CPU's board (coral orange).
 * 4. The first board to complete any line of 5 numbers (horizontal, vertical, or diagonal) turns emerald green
 *    and wins the round!
 * 5. Upon winning, the Username, Round Number, the 5 winning Bingo numbers, and the System Time
 *    are automatically saved into the SQLiteDatabase.
 */
class BingoActivity : AppCompatActivity() {

    // Declare SQLite database helper instance
    private lateinit var dbHelper: GameDatabaseHelper

    // Store the 25 buttons and number arrays for both the player's and CPU's 5x5 boards
    private val playerButtons = mutableListOf<AppCompatButton>()
    private val cpuButtons = mutableListOf<AppCompatButton>()
    private var playerBoard = IntArray(25)
    private var cpuBoard = IntArray(25)

    // Track the list of drawn numbers and their drawn status (indices 1 to 25)
    private val drawnNumbersList = mutableListOf<Int>()
    private val isNumberDrawn = BooleanArray(26)

    // Define all 12 possible winning line index combinations on a 5x5 board (5 rows, 5 columns, 2 diagonals)
    private val bingoLines: List<IntArray> by lazy {
        val list = mutableListOf<IntArray>()
        for (i in 0 until 5) {
            list.add(intArrayOf(i * 5, i * 5 + 1, i * 5 + 2, i * 5 + 3, i * 5 + 4)) // Horizontal rows
            list.add(intArrayOf(i, i + 5, i + 10, i + 15, i + 20))                 // Vertical columns
        }
        list.add(intArrayOf(0, 6, 12, 18, 24)) // Main diagonal
        list.add(intArrayOf(4, 8, 12, 16, 20)) // Anti-diagonal
        list
    }

    // Track the current player's username, round number, and active game state
    private var currentUsername: String = "admin"
    private var currentRound: Int = 1
    private var isGameActive: Boolean = true

    // Declare UI view components
    private lateinit var tvBingoHeaderInfo: TextView
    private lateinit var tvLastBall: TextView
    private lateinit var tvBingoStatus: TextView
    private lateinit var tvDrawHistory: TextView
    private lateinit var tvPlayerLines: TextView
    private lateinit var tvCpuLines: TextView
    private lateinit var tvSqliteRecords: TextView
    private lateinit var glPlayerBoard: GridLayout
    private lateinit var glCpuBoard: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bingo)

        // Initialize the SQLiteDatabase helper
        dbHelper = GameDatabaseHelper(this)

        // Retrieve the currently logged-in Username from SharedPreferences
        val sharedPref = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        currentUsername = sharedPref.getString("CURRENT_PLAYER", "admin") ?: "admin"

        // Bind UI components from the layout
        tvBingoHeaderInfo = findViewById(R.id.tvBingoHeaderInfo)
        tvLastBall = findViewById(R.id.tvLastBall)
        tvBingoStatus = findViewById(R.id.tvBingoStatus)
        tvDrawHistory = findViewById(R.id.tvDrawHistory)
        tvPlayerLines = findViewById(R.id.tvPlayerLines)
        tvCpuLines = findViewById(R.id.tvCpuLines)
        tvSqliteRecords = findViewById(R.id.tvSqliteRecords)
        glPlayerBoard = findViewById(R.id.glPlayerBoard)
        glCpuBoard = findViewById(R.id.glCpuBoard)
        val btnDrawNumber = findViewById<Button>(R.id.btnDrawNumber)
        val btnNewBingoRound = findViewById<Button>(R.id.btnNewBingoRound)
        val btnClearDb = findViewById<Button>(R.id.btnClearDb)
        val btnBackFromBingo = findViewById<Button>(R.id.btnBackFromBingo)

        // Dynamically generate the 5x5 grid buttons for both the player and the CPU boards
        createGridButtons(glPlayerBoard, playerButtons, cellDp = 46)
        createGridButtons(glCpuBoard, cpuButtons, cellDp = 42)

        // Retrieve the next round number, initialize both boards, and load SQLite history records
        currentRound = dbHelper.getNextRoundNumber()
        startNewBingoRound()
        refreshDatabaseView()

        // Click listener for "Draw Lucky Ball": randomly draws an unselected number from the pool
        btnDrawNumber.setOnClickListener {
            if (!isGameActive) {
                Toast.makeText(this, "Round finished! Click 'New Cards' to start next round.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            drawNextLuckyBall()
        }

        // Click listener for "New Cards": reshuffles both cards to start a fresh match
        btnNewBingoRound.setOnClickListener {
            startNewBingoRound()
        }

        // Click listener for "Clear DB": deletes all historical records from the SQLite database
        btnClearDb.setOnClickListener {
            dbHelper.clearAllRecords()
            currentRound = 1
            updateHeaderUI()
            refreshDatabaseView()
            Toast.makeText(this, "SQLite Database records cleared!", Toast.LENGTH_SHORT).show()
        }

        // Return to the main menu activity
        btnBackFromBingo.setOnClickListener {
            finish()
        }
    }

    /**
     * Dynamically creates 25 buttons for a 5x5 board and adds them to the specified GridLayout.
     * Cells are set to non-clickable so that numbers can only be marked via the Lucky Draw machine.
     */
    private fun createGridButtons(
        gridLayout: GridLayout,
        buttonList: MutableList<AppCompatButton>,
        cellDp: Int
    ) {
        val density = resources.displayMetrics.density
        val cellSize = (cellDp * density).toInt()
        val marginSize = (2.5f * density).toInt()

        for (i in 0 until 25) {
            val btn = AppCompatButton(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    setMargins(marginSize, marginSize, marginSize, marginSize)
                }
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, 0)
                setBackgroundResource(R.drawable.bg_ttt_cell)
                stateListAnimator = null
                isClickable = false
            }
            buttonList.add(btn)
            gridLayout.addView(btn)
        }
    }

    /**
     * Initializes a new round: independently shuffles numbers 1 to 25 for both the player and CPU cards,
     * and resets all drawn ball states.
     */
    private fun startNewBingoRound() {
        playerBoard = (1..25).shuffled().toIntArray()
        cpuBoard = (1..25).shuffled().toIntArray()
        drawnNumbersList.clear()
        for (n in 1..25) isNumberDrawn[n] = false

        for (i in 0 until 25) {
            playerButtons[i].apply {
                text = playerBoard[i].toString()
                setTextColor(Color.parseColor("#1E1E2C"))
                setBackgroundResource(R.drawable.bg_ttt_cell)
            }
            cpuButtons[i].apply {
                text = cpuBoard[i].toString()
                setTextColor(Color.parseColor("#1E1E2C"))
                setBackgroundResource(R.drawable.bg_ttt_cell)
            }
        }

        isGameActive = true
        tvLastBall.text = "?"
        tvBingoStatus.text = "Click 'Draw Lucky Ball' to draw a number!"
        tvBingoStatus.setTextColor(Color.parseColor("#1E1E2C"))
        tvDrawHistory.text = "Drawn (0/25): None"
        updateHeaderUI()
        updateBestLineProgress()
    }

    /**
     * Executes a random lucky ball draw: picks an unselected number between 1 and 25,
     * and simultaneously highlights the matching cell on both the player's and CPU's boards.
     */
    private fun drawNextLuckyBall() {
        val availableBalls = (1..25).filter { !isNumberDrawn[it] }
        if (availableBalls.isEmpty()) return

        // Randomly pick a lucky number ball from the remaining pool
        val luckyNumber = availableBalls.random()
        isNumberDrawn[luckyNumber] = true
        drawnNumbersList.add(luckyNumber)

        // Update the top lucky ball display and drawn number history
        tvLastBall.text = luckyNumber.toString()
        tvBingoStatus.text = "🎱 Ball #$luckyNumber drawn! Both cards updated."
        tvBingoStatus.setTextColor(Color.parseColor("#5E4AE3"))
        tvDrawHistory.text = "Drawn (${drawnNumbersList.size}/25): ${drawnNumbersList.joinToString(", ")}"

        // 1. Highlight the matching number on the player's board (purple gradient)
        val pIdx = playerBoard.indexOf(luckyNumber)
        if (pIdx != -1) {
            playerButtons[pIdx].apply {
                setBackgroundResource(R.drawable.btn_gradient_primary)
                setTextColor(Color.WHITE)
            }
        }

        // 2. Highlight the matching number on the CPU's board (coral orange gradient)
        val cIdx = cpuBoard.indexOf(luckyNumber)
        if (cIdx != -1) {
            cpuButtons[cIdx].apply {
                setBackgroundResource(R.drawable.btn_gradient_coral)
                setTextColor(Color.WHITE)
            }
        }

        // Update the best line progress indicator (e.g., 4 / 5) for both boards
        updateBestLineProgress()

        // Check if either the player or the CPU has completed a 5-number Bingo line
        checkBingoWinner()
    }

    /**
     * Checks whether the player or CPU has completed a 5-number line.
     * If a winner is found, highlights the winning line in emerald green and saves the record to SQLiteDatabase.
     */
    private fun checkBingoWinner() {
        val playerWinLine = findWinningLineIndices(playerBoard)
        val cpuWinLine = findWinningLineIndices(cpuBoard)

        if (playerWinLine == null && cpuWinLine == null) return

        isGameActive = false
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Case 1: Player completes a Bingo line (player takes priority if both complete on the same ball)
        if (playerWinLine != null) {
            for (pos in playerWinLine) {
                playerButtons[pos].setBackgroundResource(R.drawable.btn_gradient_teal)
            }
            val winningNumbersStr = playerWinLine.map { playerBoard[it] }.joinToString(", ")

            // Insert the 4 required attributes into SQLiteDatabase
            dbHelper.insertBingoRecord(
                username = currentUsername,
                roundNumber = currentRound,
                bingoNumbers = winningNumbersStr,
                systemTime = currentTime
            )

            tvBingoStatus.text = "🏆 YOU WIN BINGO! Line: [$winningNumbersStr]"
            tvBingoStatus.setTextColor(Color.parseColor("#00A896"))
            Toast.makeText(this, "You Won! Record saved to SQLite (Round #$currentRound)", Toast.LENGTH_SHORT).show()
        }
        // Case 2: CPU completes a Bingo line first
        else if (cpuWinLine != null) {
            for (pos in cpuWinLine) {
                cpuButtons[pos].setBackgroundResource(R.drawable.btn_gradient_teal)
            }
            val winningNumbersStr = cpuWinLine.map { cpuBoard[it] }.joinToString(", ")

            // Insert the 4 required attributes into SQLiteDatabase
            dbHelper.insertBingoRecord(
                username = "CPU_AI (vs $currentUsername)",
                roundNumber = currentRound,
                bingoNumbers = winningNumbersStr,
                systemTime = currentTime
            )

            tvBingoStatus.text = "🤖 CPU WINS BINGO! Line: [$winningNumbersStr]"
            tvBingoStatus.setTextColor(Color.parseColor("#D9383A"))
            Toast.makeText(this, "CPU Won Round #$currentRound! Record saved to SQLite.", Toast.LENGTH_SHORT).show()
        }

        // Increment the round number for the next match and refresh the SQLite records display
        currentRound = dbHelper.getNextRoundNumber()
        refreshDatabaseView()
    }

    /**
     * Checks if any of the 12 lines on the specified board has all 5 numbers drawn.
     */
    private fun findWinningLineIndices(board: IntArray): IntArray? {
        for (line in bingoLines) {
            if (line.all { idx -> isNumberDrawn[board[idx]] }) {
                return line
            }
        }
        return null
    }

    /**
     * Calculates and displays the closest line progress (Best Line: X / 5) for both the player and the CPU.
     */
    private fun updateBestLineProgress() {
        val playerMax = bingoLines.maxOf { line ->
            line.count { idx -> isNumberDrawn[playerBoard[idx]] }
        }
        val cpuMax = bingoLines.maxOf { line ->
            line.count { idx -> isNumberDrawn[cpuBoard[idx]] }
        }
        tvPlayerLines.text = "🟣 $currentUsername's Card (Best Line: $playerMax / 5)"
        tvCpuLines.text = "🟠 CPU's Card (Best Line: $cpuMax / 5)"
    }

    /**
     * Updates the top header badge with the current player's username and round number.
     */
    private fun updateHeaderUI() {
        tvBingoHeaderInfo.text = "👤 Player: $currentUsername  |  Round #$currentRound"
    }

    /**
     * Queries all saved Bingo records from SQLiteDatabase and displays them in the bottom TextView.
     */
    private fun refreshDatabaseView() {
        tvSqliteRecords.text = dbHelper.getAllBingoRecordsFormatted()
    }
}
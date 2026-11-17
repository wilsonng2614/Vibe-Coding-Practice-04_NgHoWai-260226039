package ict.thei.edu.guessnumbergame

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TicTacToeActivity : AppCompatActivity() {

    private val board = Array(9) { "" }
    private lateinit var cells: List<Button>
    private var isXTurn = true
    private var gameActive = true

    private var xWins = 0
    private var oWins = 0
    private var draws = 0

    private lateinit var tvTurnStatus: TextView
    private lateinit var tvScoreBoard: TextView
    private lateinit var cbVsCpu: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tic_tac_toe)

        tvTurnStatus = findViewById(R.id.tvTurnStatus)
        tvScoreBoard = findViewById(R.id.tvScoreBoard)
        cbVsCpu = findViewById(R.id.cbVsCpu)
        val btnResetBoard = findViewById<Button>(R.id.btnResetBoard)
        val btnBackFromTtt = findViewById<Button>(R.id.btnBackFromTtt)

        cells = listOf(
            findViewById(R.id.btnCell0), findViewById(R.id.btnCell1), findViewById(R.id.btnCell2),
            findViewById(R.id.btnCell3), findViewById(R.id.btnCell4), findViewById(R.id.btnCell5),
            findViewById(R.id.btnCell6), findViewById(R.id.btnCell7), findViewById(R.id.btnCell8)
        )

        val prefs = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        xWins = prefs.getInt("TTT_X_WINS", 0)
        oWins = prefs.getInt("TTT_O_WINS", 0)
        draws = prefs.getInt("TTT_DRAWS", 0)
        updateScoreUI()

        cells.forEachIndexed { index, button ->
            button.setOnClickListener { onCellClicked(index) }
        }

        btnResetBoard.setOnClickListener { resetBoard() }
        btnBackFromTtt.setOnClickListener { finish() }
    }

    private fun onCellClicked(index: Int) {
        if (!gameActive || board[index].isNotEmpty()) return

        makeMove(index, if (isXTurn) "X" else "O")

        if (checkGameResult()) return

        isXTurn = !isXTurn
        updateTurnUI()

        if (gameActive && !isXTurn && cbVsCpu.isChecked) {
            val cpuMove = findBestCpuMove()
            if (cpuMove != -1) {
                makeMove(cpuMove, "O")
                if (!checkGameResult()) {
                    isXTurn = true
                    updateTurnUI()
                }
            }
        }
    }

    private fun makeMove(index: Int, symbol: String) {
        board[index] = symbol
        cells[index].text = symbol
        if (symbol == "X") {
            cells[index].setTextColor(Color.parseColor("#FF6B6B")) // 珊瑚紅
        } else {
            cells[index].setTextColor(Color.parseColor("#00A896")) // 湖水綠
        }
    }

    private fun findBestCpuMove(): Int {
        val winPatterns = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )

        for (p in winPatterns) {
            val move = checkTwoInRow(p, "O")
            if (move != -1) return move
        }

        for (p in winPatterns) {
            val move = checkTwoInRow(p, "X")
            if (move != -1) return move
        }

        if (board[4].isEmpty()) return 4

        val emptyIndices = board.indices.filter { board[it].isEmpty() }
        return if (emptyIndices.isNotEmpty()) emptyIndices.random() else -1
    }

    private fun checkTwoInRow(pattern: IntArray, symbol: String): Int {
        val a = pattern[0]; val b = pattern[1]; val c = pattern[2]
        if (board[a] == symbol && board[b] == symbol && board[c].isEmpty()) return c
        if (board[a] == symbol && board[c] == symbol && board[b].isEmpty()) return b
        if (board[b] == symbol && board[c] == symbol && board[a].isEmpty()) return a
        return -1
    }

    private fun checkGameResult(): Boolean {
        val winPatterns = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )

        for (p in winPatterns) {
            val first = board[p[0]]
            if (first.isNotEmpty() && first == board[p[1]] && first == board[p[2]]) {
                gameActive = false
                if (first == "X") {
                    xWins++
                    tvTurnStatus.text = "🎉 Player X Wins!"
                    tvTurnStatus.setTextColor(Color.parseColor("#FF6B6B"))
                } else {
                    oWins++
                    tvTurnStatus.text = if (cbVsCpu.isChecked) "🤖 CPU (O) Wins!" else "🎉 Player O Wins!"
                    tvTurnStatus.setTextColor(Color.parseColor("#00A896"))
                }
                saveScores()
                return true
            }
        }

        if (board.none { it.isEmpty() }) {
            gameActive = false
            draws++
            tvTurnStatus.text = "🤝 It's a Draw!"
            tvTurnStatus.setTextColor(Color.parseColor("#5E4AE3"))
            saveScores()
            return true
        }
        return false
    }

    private fun updateTurnUI() {
        if (isXTurn) {
            tvTurnStatus.text = "❌ Player X's Turn"
            tvTurnStatus.setTextColor(Color.parseColor("#FF6B6B"))
        } else {
            tvTurnStatus.text = "⭕ Player O's Turn"
            tvTurnStatus.setTextColor(Color.parseColor("#00A896"))
        }
    }

    private fun saveScores() {
        updateScoreUI()
        getSharedPreferences("GamePrefs", Context.MODE_PRIVATE).edit()
            .putInt("TTT_X_WINS", xWins)
            .putInt("TTT_O_WINS", oWins)
            .putInt("TTT_DRAWS", draws)
            .apply()
    }

    private fun updateScoreUI() {
        tvScoreBoard.text = "❌ Wins: $xWins   |   🤝 Draws: $draws   |   ⭕ Wins: $oWins"
    }

    private fun resetBoard() {
        for (i in board.indices) {
            board[i] = ""
            cells[i].text = ""
        }
        isXTurn = true
        gameActive = true
        updateTurnUI()
    }
}
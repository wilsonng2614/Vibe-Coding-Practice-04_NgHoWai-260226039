package ict.thei.edu.guessnumbergame

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * GameDatabaseHelper class: Manages the creation, version upgrades, and CRUD data operations for the SQLiteDatabase.
 * According to the assignment requirements, the database table includes the following attributes:
 * 1. Username (Player's account name)
 * 2. Round Number (Round number of the game)
 * 3. Five Numbers of Bingo (The 5 numbers forming the winning Bingo line)
 * 4. System Time (System timestamp when the record was achieved)
 */
class GameDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Define database name and version number
        private const val DATABASE_NAME = "BingoGameRecords.db"
        private const val DATABASE_VERSION = 1

        // Define table name and column constants
        const val TABLE_NAME = "bingo_history"
        const val COL_ID = "id"
        const val COL_USERNAME = "username"           // i. Username
        const val COL_ROUND = "round_number"          // ii. Round number of the game
        const val COL_BINGO_NUMBERS = "bingo_numbers" // iii. Those five numbers of Bingo
        const val COL_SYSTEM_TIME = "system_time"     // iv. System Time
    }

    /**
     * Called when the database is created for the first time; executes the CREATE TABLE SQL statement.
     */
    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT NOT NULL,
                $COL_ROUND INTEGER NOT NULL,
                $COL_BINGO_NUMBERS TEXT NOT NULL,
                $COL_SYSTEM_TIME TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    /**
     * Called when the database needs to be upgraded; drops the existing table and recreates it.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /**
     * Inserts a new Bingo winning record into the SQLiteDatabase.
     * @param username Player's username
     * @param roundNumber Current round number of the game
     * @param bingoNumbers String containing the 5 winning Bingo numbers (e.g., "5, 12, 18, 21, 25")
     * @param systemTime Current system time string
     * @return The row ID of the newly inserted record, or -1 if an error occurred
     */
    fun insertBingoRecord(
        username: String,
        roundNumber: Int,
        bingoNumbers: String,
        systemTime: String
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_USERNAME, username)
            put(COL_ROUND, roundNumber)
            put(COL_BINGO_NUMBERS, bingoNumbers)
            put(COL_SYSTEM_TIME, systemTime)
        }
        val resultId = db.insert(TABLE_NAME, null, values)
        db.close()
        return resultId
    }

    /**
     * Retrieves the highest round number from the database to calculate the next Round Number.
     */
    fun getNextRoundNumber(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT MAX($COL_ROUND) FROM $TABLE_NAME", null)
        var maxRound = 0
        if (cursor.moveToFirst()) {
            maxRound = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return maxRound + 1
    }

    /**
     * Queries all Bingo historical records from the SQLiteDatabase (ordered from newest to oldest).
     * @return Formatted historical records string for UI display
     */
    fun getAllBingoRecordsFormatted(): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME ORDER BY $COL_ID DESC",
            null
        )

        val sb = StringBuilder()
        if (cursor.moveToFirst()) {
            do {
                val user = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME))
                val round = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ROUND))
                val nums = cursor.getString(cursor.getColumnIndexOrThrow(COL_BINGO_NUMBERS))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COL_SYSTEM_TIME))

                sb.append("🏆 Round #$round | 👤 $user\n")
                sb.append("🔢 Bingo 5 Numbers: [$nums]\n")
                sb.append("🕒 Time: $time\n")
                sb.append("────────────────────────\n")
            } while (cursor.moveToNext())
        } else {
            sb.append("No Bingo records in SQLite database yet.\nWin a round to save your first record!")
        }
        cursor.close()
        db.close()
        return sb.toString()
    }

    /**
     * Deletes all records from the database table.
     */
    fun clearAllRecords() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }
}
package connectfour

const val Title = "Connect Four"
const val DefaultRows = 6
const val DefaultColumns = 7
const val MinSize = 5
const val MaxSize = 9
const val AskFirstPlayer = "First player's name:"
const val AskSecondPlayer = "Second player's name:"
const val AskBoardSize = "Set the board dimensions (Rows x Columns)\nPress Enter for default ($DefaultRows x $DefaultColumns)"
const val BoardRowsError = "Board rows should be from $MinSize to $MaxSize"
const val BoardColumnsError = "Board columns should be from $MinSize to $MaxSize"
const val InvalidInput = "Invalid input"
const val InvalidMove = "Incorrect column number"
const val ColumnOutOfRange = "The column number is out of range"
const val Draw = "It is a draw"
const val AskMove = "\'s turn:"
const val GameOver = "Game over!"
const val Symbols = "o*"
const val AskGames =
"""Do you want to play single or multiple games?
For a single game, input 1 or press Enter
Input a number of games:"""

class Game(val games: Int, val rows: Int, val columns: Int,
    val players : Array<String>) {
    val board = Array(rows) { CharArray(columns) { ' ' } }
    val scores = IntArray(players.size) { 0 }
    var player = 0
    var gameNumber = 0

    private fun clearBoard() {
        for (i in 0 until rows)
            for (j in 0 until columns)
                board[i][j] = ' '
    }

    fun banner() {
        println(players.joinToString(" VS "))
        println("$rows X $columns board")
        if (games == 1)
            println("Single game")
        else {
            println("Total $games games")
        }
    }

    fun start(startingPlayer: Int) {
        player = startingPlayer
        gameNumber++
        if (games != 1)
            println("Game #$gameNumber")
        clearBoard()
    }

    fun showScores() {
        println("Score")
        println("${players[0]}: ${scores[0]} ${players[1]}: ${scores[1]}")
    }

    fun turn(column: Int): Boolean {
        for (i in rows - 1 downTo 0) {
            if (board[i][column - 1] == ' ') {
                board[i][column - 1] = Symbols[player]
                break
            }
        }
        drawBoard()
        if (victory(player)) {
            println("Player " + players[player] + " won")
            scores[player] += 2
            return true
        } else if (boardFull()) {
            println(Draw)
            scores[0]++
            scores[1]++
            return true
        }
        player = 1 - player
        return false
    }

    private fun victory(player: Int): Boolean {
        val symbol = Symbols[player]
        for (i in 0 until rows)
            for (j in 0 until columns - 3)
                if (board[i][j] == symbol &&
                    board[i][j + 1] == symbol &&
                    board[i][j + 2] == symbol &&
                    board[i][j + 3] == symbol)
                    return true
        for (i in 0 until rows - 3)
            for (j in 0 until columns)
                if (board[i][j] == symbol &&
                    board[i + 1][j] == symbol &&
                    board[i + 2][j] == symbol &&
                    board[i + 3][j] == symbol)
                    return true
        for (i in 0 until rows - 3)
            for (j in 0 until columns - 3)
                if (board[i][j] == symbol &&
                    board[i + 1][j + 1] == symbol &&
                    board[i + 2][j + 2] == symbol &&
                    board[i + 3][j + 3] == symbol)
                    return true
        for (i in 3 until rows)
            for (j in 0 until columns - 3)
                if (board[i][j] == symbol &&
                    board[i - 1][j + 1] == symbol &&
                    board[i - 2][j + 2] == symbol &&
                    board[i - 3][j + 3] == symbol)
                    return true
        return false
    }

    private fun boardFull(): Boolean {
        for (i in 0 until rows)
            for (j in 0 until columns)
                if (board[i][j] == ' ')
                    return false
        return true
    }

    fun drawBoard() {
        for (i in 1 .. columns)
            print(" $i")
        println()
        for (i in 0 until rows) {
            print('║')
            for (j in 0 until columns)
                print("${board[i][j]}║")
            println()
        }
        print('╚')
        for (j in 1 until columns)
            print("═╩")
        println("═╝")
    }
}

fun prompt(message: String): String {
    println(message)
    return readln()
}

fun readSize(): Pair<Int, Int> {
    var rows: Int? = null
    var columns: Int? = null
    while (rows == null || columns == null ||
        rows < MinSize || rows > MaxSize ||
        columns < MinSize || columns > MaxSize) {
        val input = prompt(AskBoardSize)
        if (input.isEmpty()) {
            rows = DefaultRows
            columns = DefaultColumns
            break
        }
        val parts = input.split('x', 'X')
        if (parts.size != 2) {
            println(InvalidInput)
            continue
        }
        rows = parts[0].trim().toIntOrNull()
        columns = parts[1].trim().toIntOrNull()
        if (rows == null || columns == null) {
            println(InvalidInput)
            continue
        }
        if (rows < MinSize || rows > MaxSize) {
            println(BoardRowsError)
        }
        if (columns < MinSize || columns > MaxSize) {
            println(BoardColumnsError)
        }
    }
    return Pair(rows!!, columns!!)
}

fun readGames(): Int {
    var number: Int? = null
    while (number == null || number < 1) {
        val input = prompt(AskGames)
        number = if (input == "") 1 else input.toIntOrNull()
        if (number == null || number < 1)
            println(InvalidInput)
    }
    return number
}

fun readMove(game: Game): Int? {
    var move: Int? = null
    while (move == null) {
        val input = prompt(game.players[game.player] + AskMove)
        if (input == "end")
            break
        move = input.toIntOrNull()
        if (move == null) {
            println(InvalidMove)
            continue
        }
        if (move < 1 || move > game.columns) {
            print(ColumnOutOfRange)
            println(" (1 - ${game.columns})")
            move = null
            continue
        }
        if (game.board[0][move - 1] != ' ') {
            println("Column $move is full")
            move = null
            continue
        }
    }
    return move
}

fun main() {
    println(Title)
    val first = prompt(AskFirstPlayer)
    val second = prompt(AskSecondPlayer)
    val (rows, columns) = readSize()
    val games = readGames()
    val game = Game(games, rows, columns, arrayOf(first, second))
    game.banner()
    var startingPlayer = 0
    var terminated: Boolean = false
    while(game.gameNumber < game.games && !terminated) {
        var finished: Boolean = false
        game.start(startingPlayer)
        game.drawBoard()
        while (!finished) {
            val move = readMove(game)
            if (move == null) {
                terminated = true
                break
            }
            if (game.turn(move))
                finished = true
        }
        if (!terminated || game.games != 1)
            game.showScores()
        startingPlayer = 1 - startingPlayer
    }
    println(GameOver)
}
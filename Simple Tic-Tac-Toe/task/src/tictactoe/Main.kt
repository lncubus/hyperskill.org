package tictactoe

import kotlin.math.abs

const val N = 3
val players = listOf('X', 'O')

private enum class Result(val displayName: String) {
    GameNotFinished("Game not finished"),
    Draw("Draw"),
    PlayerWins("Player wins"),
    Impossible("Impossible");
    override fun toString(): String = displayName
}

private class TicTacToe {
    override fun toString() = "${drawBoard()}\n${getResult()}"

    val grid = Array(N) {CharArray(N) {' '} }

    fun drawBoard(): StringBuilder {
        val hr = "---------"
        val answer = StringBuilder()
        answer.appendLine(hr)
        grid.forEach {
            answer.appendLine(it.joinToString(
                separator = " ", prefix = "| ", postfix = " |")) }
        answer.append(hr)
        return answer
    }

    private fun checkVictory(r: Int, c: Int, dr: Int, dc: Int): Char {
        val result = grid[r][c]
        if (!players.contains(result))
            return ' '
        for (i in 1 until N) {
            val mark = grid[r + dr * i][c + dc * i]
            if (mark != result)
                return ' '
        }
        return result
    }

    private fun countVictory(r: Int, c: Int, dr: Int, dc: Int) {
        val winner = checkVictory(r, c, dr, dc)
        if (players.contains(winner)) {
            victories[winner] = victories.getOrDefault(winner, 0) + 1
        }
    }

    private val victories = mutableMapOf <Char, Int>()

    fun getWinner() = victories.keys.singleOrNull()

    fun getResult(): Result
    {
        val count = mutableMapOf<Char, Int>()
        victories.clear()

        grid.forEach { it.forEach {
            if (players.contains(it)) {
                count[it] = count.getOrDefault(it, 0) + 1
            }
        }}

        val minimum = count.values.minOfOrNull { it } ?: 0
        val maximum = count.values.maxOfOrNull { it } ?: 0
        if (abs(minimum - maximum) > 1)
            return Result.Impossible
        countVictory(0, 0, 1, 1)
        countVictory(0, N - 1, 1, -1)
        for(i in 0 until N) {
            countVictory(i, 0, 0, 1)
            countVictory(0, i, 1, 0)
        }
        val totalVictories = victories.keys.size
        return when {
            totalVictories > 1 -> return Result.Impossible
            totalVictories == 1 -> return Result.PlayerWins
            grid.any { it.any { it == ' ' }} -> Result.GameNotFinished
            else -> Result.Draw
        }
    }
}

private fun check(input: String, result: Result, winner: Char = ' ') {
    val ttt = TicTacToe()
    for (r in 0 until N)
        for (c in 0 until N)
            ttt.grid[r][c] = input[r * N + c]
    if (ttt.getResult() != result)
        throw AssertionError()
    if (result == Result.PlayerWins && ttt.getWinner() != winner)
        throw AssertionError()
}

private fun test() {
    check("XXXOO  O ", Result.PlayerWins, 'X')
    check("XOXOXOXXO", Result.PlayerWins, 'X')
    check("XOOOXOXXO", Result.PlayerWins, 'O')
    check("XOXOOXXXO", Result.Draw)
    check("XO OOX X ", Result.GameNotFinished)
    check("XO XO XOX", Result.Impossible)
    check(" O X  X X", Result.Impossible)
    check(" OOOO X X", Result.Impossible)
    check("XOXOXXOOX", Result.PlayerWins, 'X')
}

fun main() {
    // write your code here
    // test()
    val ticTacToe = TicTacToe()
    var player = 0
    var result = Result.GameNotFinished
    while (result == Result.GameNotFinished) {
        println(ticTacToe.drawBoard())
        var okay = false
        while (!okay) {
            val move = readln().split(' ', limit = 2)
            if (move.size != 2 || move.any { it.any { !it.isDigit() } }) {
                println("You should enter numbers!")
                continue
            }
            val coordinates = move.map { it.toInt() - 1 }
            if (coordinates.any { it !in 0 until N }) {
                println("Coordinates should be from 1 to ${N}!")
                continue
            }
            val (row, column) = coordinates
            val c = ticTacToe.grid[row][column]
            if (c != ' ') {
                println("This cell is occupied! Choose another one!")
                continue
            }
            ticTacToe.grid[row][column] = players[player]
            player = (player + 1) % players.size
            okay = true
            result = ticTacToe.getResult()
        }
    }
    println(ticTacToe.drawBoard())
    if (result == Result.PlayerWins)
        println("${ticTacToe.getWinner()} wins")
    else
        println(result)
}


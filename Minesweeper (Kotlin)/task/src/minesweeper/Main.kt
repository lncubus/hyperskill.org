package minesweeper

import java.util.LinkedList

const val N = 9

enum class Marker {
    free, mine
}

const val Mine = -3
const val Mark = -2
const val Fog = -1

data class Position(val row: Int, val col: Int) {
    override fun toString() = "$row $col"

    operator fun plus(other: Position) =
        Position(this.row + other.row, this.col + other.col)
    operator fun minus(other: Position) =
        Position(this.row - other.row, this.col - other.col)
    operator fun times(v: Int) =
        Position(v * this.row, v * this.col)

    companion object {
        fun parse(place: CharSequence): Position? {
            place.split(' ').let {
                if (it.size != 2)
                    return null
                val row = it[1].toIntOrNull() ?: return null
                val col = it[0].toIntOrNull() ?: return null
                return Position(row - 1, col - 1)
            }
        }
    }
}

data class Turn(val position: Position, val marker: Marker) {
    companion object {
        fun parse(place: CharSequence): Turn? {
            place.split(' ').let {
                if (it.size != 3)
                    return null
                val p = Position.parse(it[0] + " " + it[1])
                    ?: return null
                val m = Marker.valueOf(it[2])
                return Turn(p, m)
            }
        }
    }
}

class Minefield(private val n: Int,
    private val width: Int = N, private val height: Int = N) {
    private val mines = mutableSetOf<Position>()
    private val calculated = Array(height) { IntArray(width) { Fog } }
    var lost: Position? = null

    fun victory(): Boolean {
        if (lost != null)
            return false
        for (row in 0 until height)
            for (col in 0 until width) {
                if (calculated[row][col] == Mark && Position(row, col) !in mines)
                    return false
                if (calculated[row][col] == Fog && Position(row, col) in mines)
                    return false
            }
        return true
    }

    private fun create(n: Int, free: Position) {
        val random = java.util.Random()
        while (mines.size < n) {
            val x = random.nextInt(N)
            val y = random.nextInt(N)
            val p = Position(x, y)
            if (p != free)
                mines.add(p)
        }
    }

    private fun countMines(p: Position): Int {
        var count = 0
        for (row in p.row - 1..p.row + 1) {
            for (col in p.col - 1..p.col + 1) {
                if (row == p.row && col == p.col)
                    continue
                if (col in 0 until width &&
                    row in 0 until height &&
                    Position(row, col) in mines
                )
                    count++
            }
        }
        return count
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(" │")
        for (x in 0 until width)
            sb.append(x + 1)
        sb.appendLine("│")
        sb.appendLine("—│—————————│")
        for (y in 0 until height) {
            sb.append(y + 1)
            sb.append('│')
            for (x in 0 until width) {
                val cell = calculated[y][x]
                sb.append(
                    when (cell) {
                        Mine -> 'X'
                        Mark -> '*'
                        Fog -> if (lost == null) '.'
                            else if (Position(y, x) in mines) 'X' else '.'
                        0 -> '/'
                        else -> '0' + calculated[y][x]
                    }
                )
            }
            sb.appendLine('│')
        }
        sb.appendLine("—│—————————│")
        return sb.toString()
    }

    private fun clear(position: Position) {
        val queue = LinkedList<Position>()
        queue.add(position)
        while (queue.isNotEmpty()) {
            val p = queue.remove()
            if (p in mines)
                continue
            val count = countMines(p)
            calculated[p.row][p.col] = count
            if (count == 0) {
                for (row in p.row - 1..p.row + 1) {
                    for (col in p.col - 1..p.col + 1) {
                        if (row == p.row && col == p.col)
                            continue
                        if (col in 0 until width &&
                            row in 0 until height &&
                            calculated[row][col] < 0
                        )
                            queue.add(Position(row, col))
                    }
                }
            }
        }
    }

    fun turn(turn: Turn) {
        if (mines == emptySet<Position>())
            create(n, turn.position)
        when (turn.marker) {
            Marker.free -> {
                if (turn.position in mines)
                    lost = turn.position
                else
                    clear(turn.position)
            }
            Marker.mine -> {
                if (calculated[turn.position.row][turn.position.col] == Mark)
                    calculated[turn.position.row][turn.position.col] = Fog
                else if (calculated[turn.position.row][turn.position.col] == Fog)
                    calculated[turn.position.row][turn.position.col] = Mark
            }
        }
    }
}

fun prompt(message: String): String {
    print(message)
    return readln()
}

fun main() {
    val n = prompt("How many mines do you want on the field? ").toInt()
    val mines = Minefield(n, N, N)
    do {
        var okay = false
        do {
            println(mines)
            val command = prompt("Set/unset mines marks or claim a cell as free: ")
            val turn = Turn.parse(command)
            if (turn != null) {
                mines.turn(turn)
                okay = true
            } else
                println("Invalid command")
        } while (!okay)
    } while (!mines.victory() && mines.lost == null)
    println(mines)
    if (mines.lost != null)
        println("You stepped on a mine and failed!")
    else
        println("Congratulations! You found all the mines!")
}

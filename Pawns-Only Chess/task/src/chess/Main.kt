package chess

import kotlin.math.abs

const val N = 8

const val Title = "Pawns-Only Chess"

enum class Player(val symbol: Char, val direction: Int, val start: Int, val coronation: Int) {
    White('W', 1, 1, 7),
    Black('B', -1, 6, 0);
    fun other() = Player.values().single { it != this }
}

enum class MoveResult {
    Okay,
    Capture,
    InvalidInput,
    NoPiece
}

enum class GameResult(val player: Player?) {
    WhiteWins(Player.White),
    BlackWins(Player.Black),
    Stalemate(null);
    override fun toString(): String =
        if (player == null) "Stalemate!" else "${player.toString()} Wins!"
    companion object {
        fun wins(player: Player?) = GameResult.values().single { it.player == player }
    }
}

data class Position(val row: Int, val col: Int) {
    override fun toString() = String(charArrayOf('a' + col, '1' + row))

    operator fun plus(other: Position) = Position(this.row + other.row, this.col + other.col)
    operator fun minus(other: Position) = Position(this.row - other.row, this.col - other.col)
    // operator fun times(v: Int) = Position(v * this.row, v * this.col)

    fun abs() = Position(abs(row), abs(col))

    companion object {
        fun parse(place: CharSequence): Position? =
            if (place.length != 2 ||
                place[0] !in 'a'..'a' + (N - 1) ||
                place[1] !in '1'..'1' + (N - 1))
                null
            else
                Position(place[1].code - '1'.code, place[0].code - 'a'.code)
    }
}

data class Move(val from: Position, val to: Position) {
    override fun toString() = from.toString() + to.toString()

    companion object {
        fun parse(move: CharSequence): Move? =
            if (move.length != 4)
                null
            else {
                val from = Position.parse(move.substring(0, 2))
                val to = Position.parse(move.substring(2))
                if (from == null || to == null)
                    null
                else
                    Move(from, to)
            }
    }
}

class Chessboard (white: String, black: String) {

    private val players = listOf(white, black)
    private var currentPlayer = Player.White
    private val pieces = mutableMapOf<Position, Player>()
    init {
        for(p in Player.values())
            for(col in 0 until N)
                pieces[Position(p.start, col)] = p
    }

    private val passant = mutableMapOf<Player, Position>()
    private var gameResult: GameResult? = null

    fun player() = players[currentPlayer.ordinal]
    fun color() = currentPlayer.toString()
    fun result() = gameResult
    fun piece(place: String) =
        pieces.getOrDefault(Position.parse(place)
            ?: Position(-1, -1), null
        )

    private fun isFree(position: Position) = isStanding(position, null)

    private fun isStanding(position: Position, player: Player?) =
        position.row in 0 until N && position.col in 0 until N &&
        pieces.getOrDefault(position, null) == player

    private fun forward(from: Position) = sequence {
        val forward = from + Position(currentPlayer.direction, 0)
        // can we step forward
        if (isFree(forward)) {
            yield(Move(from, forward))
            // can we jump forward
            if (from.row == currentPlayer.start) {
                val jump = from + Position(2 * currentPlayer.direction, 0)
                if (isFree(jump))
                    yield(Move(from, jump))
            }
        }
    }

    private fun capture(from: Position) = sequence {
        val other = currentPlayer.other()
        for (d in -1..1 step 2) {
            val capture = from + Position(currentPlayer.direction, d)
            if (isStanding(capture, other))
                yield(Move(from, capture))
            else if (isFree(capture)) {
                val passed = from + Position(0, d)
                if (passant.getOrDefault(other, null) == passed)
                    yield(Move(from, capture))
            }
        }
    }

    private fun available(from: Position): Sequence<Move> = sequence {
        val player = pieces.getOrDefault(from, null)
        if (player != currentPlayer)
            return@sequence
        yieldAll(forward(from))
        yieldAll(capture(from))
    }

    private fun checkVictory(): GameResult? {
        // coronation of the queen
        if (pieces.any { it.value == currentPlayer && it.key.row == currentPlayer.coronation })
            return GameResult.wins(currentPlayer)
        val other = currentPlayer.other()
        // genocide
        if (!pieces.any { it.value == other } )
            return GameResult.wins(currentPlayer)
        return null
    }

    private fun checkStalemate(): GameResult? {
        // possible moves
        val mine = pieces.filter { it.value == currentPlayer }
        // we can move continue the game
        if (mine.any { available(it.key).any() })
            return null
        // no available moves
        return GameResult.Stalemate
    }

    fun move(movement: String): MoveResult {
        val move = Move.parse(movement)
        // invalid input
        if (move == null)
            return MoveResult.InvalidInput
        // not my piece
        if (!isStanding(move.from, currentPlayer))
            return MoveResult.NoPiece
        val capturing = move.from.col != move.to.col
        val moves = (if (capturing) capture(move.from) else forward(move.from)).toList()
        // this is not a legal move
        if (!moves.any { it.to == move.to })
            return MoveResult.InvalidInput
        // let's move
        val other = currentPlayer.other()
        pieces.remove(move.from)
        // capture en-passant
        if (capturing && isFree(move.to)) {
            val passed = passant.getValue(other)
            pieces.remove(passed)
        }
        // stand here
        pieces[move.to] = currentPlayer
        // now we're vulnerable
        if (!capturing && (move.to - move.from).abs() == Position(2, 0))
            passant[currentPlayer] = move.to
        passant.remove(other)
        gameResult = checkVictory()
        if (gameResult == null) {
            currentPlayer = other
            gameResult = checkStalemate()
        }
        return if (capturing) MoveResult.Capture else MoveResult.Okay
    }

    fun draw(small: Boolean = false): String {
        val sb = StringBuilder(672)
        val line = "  +---+---+---+---+---+---+---+---+"
        if (!small)
            sb.appendLine(line)
        for (row in N - 1 downTo 0) {
            sb.append(row + 1)
            if (small)
                sb.append('|')
            for (col in 0 until N) {
                if (!small)
                    sb.append(" | ")
                val piece =
                    pieces.getOrDefault(Position(row, col), null)
                sb.append(piece?.symbol ?: ' ')
            }
            if (!small) {
                sb.appendLine(" |")
                sb.appendLine(line)
            } else {
                sb.appendLine("|")
            }
        }
        sb.append(' ')
        if (small)
            sb.append(' ')
        for (col in 0 until N) {
            if (!small)
                sb.append("   ")
            sb.append('a' + col)
        }
        return sb.toString()
    }

    companion object {
        private fun assertEquals(expected: Any?, result: Any?) {
            if (expected != result) throw AssertionError(
                    "$expected was expected but $result found")
        }

        fun test() {
            val board = Chessboard("John", "Amelia")
            assertEquals(null, Position.parse("a9"))
            assertEquals(Position(0,0), Position.parse("a1"))
            assertEquals(Position(0,7), Position.parse("h1"))
            assertEquals(Position(7,0), Position.parse("a8"))
            assertEquals(Position(7,7), Position.parse("h8"))
            assertEquals(Template, board.draw())
            assertEquals(MoveResult.InvalidInput, board.move("a1z1"))
            assertEquals("White", board.color())
            assertEquals("John", board.player())
            assertEquals(MoveResult.NoPiece, board.move("b3b4"))
            assertEquals(MoveResult.Okay, board.move("a2a3"))
            assertEquals("Black", board.color())
            assertEquals("Amelia", board.player())
            assertEquals(MoveResult.Okay, board.move("b7b5"))
            assertEquals("White", board.color())
            assertEquals("John", board.player())
            assertEquals(MoveResult.Okay, board.move("a3a4"))
            assertEquals(Template1, board.draw(true))
            assertEquals(MoveResult.Capture, board.move("b5a4"))
            assertEquals(Template2, board.draw(true))
            assertEquals("White", board.color())
            assertEquals("John", board.player())
        }

        fun testParty() {
            val board = Chessboard("John", "Amelia")
            assertEquals(MoveResult.Okay, board.move("e2e4"))
            assertEquals(Player.White, board.piece("e4"))
            assertEquals(null, board.piece("e2"))
            assertEquals(MoveResult.Okay, board.move("d7d5"))
            assertEquals(Player.Black, board.piece("d5"))
            assertEquals(null, board.piece("d7"))
            assertEquals(MoveResult.Capture, board.move("e4d5"))
            assertEquals(Player.White, board.piece("d5"))
            assertEquals(null, board.piece("e4"))
            assertEquals(MoveResult.Okay, board.move("c7c6"))
            assertEquals(MoveResult.Okay, board.move("d5d6"))
            assertEquals(MoveResult.Okay, board.move("c6c5"))
            assertEquals(MoveResult.Okay, board.move("d6d7"))
            assertEquals(MoveResult.Okay, board.move("c5c4"))
            assertEquals(MoveResult.Okay, board.move("d7d8"))
            assertEquals(GameResult.WhiteWins, board.result())
        }

        fun testStalemate() {
            val board = Chessboard("John", "Amelia")
            board.pieces.clear()
            board.pieces[Position(6, 3)] = Player.White
            board.pieces[Position(6, 6)] = Player.White
            board.pieces[Position(4, 7)] = Player.Black
            board.pieces[Position(2, 0)] = Player.Black
            board.pieces[Position(1, 1)] = Player.White
            board.pieces[Position(1, 2)] = Player.White
            board.pieces[Position(1, 4)] = Player.White
            board.pieces[Position(1, 5)] = Player.White
            board.pieces[Position(1, 6)] = Player.White
            board.pieces[Position(1, 7)] = Player.White
            assertEquals(Template3, board.draw(true))
            assertEquals(MoveResult.Capture, board.move("b2a3"))
            assertEquals(null, board.result())
            assertEquals(Template4, board.draw(true))
            assertEquals(Player.Black, board.currentPlayer)
            assertEquals(MoveResult.Okay, board.move("h5h4"))
            assertEquals(Player.White, board.currentPlayer)
            assertEquals(MoveResult.Okay, board.move("h2h3"))
            assertEquals(GameResult.Stalemate, board.result())
        }

        private const val Template =
            """  +---+---+---+---+---+---+---+---+
8 |   |   |   |   |   |   |   |   |
  +---+---+---+---+---+---+---+---+
7 | B | B | B | B | B | B | B | B |
  +---+---+---+---+---+---+---+---+
6 |   |   |   |   |   |   |   |   |
  +---+---+---+---+---+---+---+---+
5 |   |   |   |   |   |   |   |   |
  +---+---+---+---+---+---+---+---+
4 |   |   |   |   |   |   |   |   |
  +---+---+---+---+---+---+---+---+
3 |   |   |   |   |   |   |   |   |
  +---+---+---+---+---+---+---+---+
2 | W | W | W | W | W | W | W | W |
  +---+---+---+---+---+---+---+---+
1 |   |   |   |   |   |   |   |   |
  +---+---+---+---+---+---+---+---+
    a   b   c   d   e   f   g   h"""

        private const val Template1 =
"""8|        |
7|B BBBBBB|
6|        |
5| B      |
4|W       |
3|        |
2| WWWWWWW|
1|        |
  abcdefgh"""
        private const val Template2 =
"""8|        |
7|B BBBBBB|
6|        |
5|        |
4|B       |
3|        |
2| WWWWWWW|
1|        |
  abcdefgh"""
        private const val Template3 =
            """8|        |
7|   W  W |
6|        |
5|       B|
4|        |
3|B       |
2| WW WWWW|
1|        |
  abcdefgh"""
        private const val Template4 =
            """8|        |
7|   W  W |
6|        |
5|       B|
4|        |
3|W       |
2|  W WWWW|
1|        |
  abcdefgh"""
    }
}

fun prompt(message: String): String {
    println(message)
    return readln()
}

fun main() {
    println(Title)
//    Chessboard.test()
//    Chessboard.testParty()
//    Chessboard.testStalemate()

    val chess = Chessboard(
        prompt("First Player's name:"),
        prompt("Second Player's name:")
    )
    println(chess.draw())
    game@ while (chess.result() == null) {
        val request = "${chess.player()}'s turn:"
        while (true) {
            val move = prompt(request)
            if (move == "exit")
                break@game
            when (chess.move(move)) {
                MoveResult.InvalidInput -> println("Invalid Input")
                MoveResult.NoPiece -> println("No ${chess.color()} pawn at ${move.substring(0, 2)}")
                MoveResult.Okay, MoveResult.Capture -> break
            }
        }
        println(chess.draw())
    }
    val result = chess.result()
    if (result != null)
        println(result)
    println("Bye!")
}
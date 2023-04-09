package indigo

val ranks = "A 2 3 4 5 6 7 8 9 10 J Q K".split(" ")
val suits = "♠ ♥ ♦ ♣".split(" ")

const val Title = "Indigo Card Game"

enum class YesNo {
    yes,
    no
}

class Card(val rank: String, val suit: String) {
    override fun toString(): String = rank + suit

    fun score(): Int = when (rank) {
            "A", "10", "J", "Q", "K" -> 1
            else -> 0
        }
}

class Deck {
    var cards: MutableList<Card> = mutableListOf()

    init {
        for (suit in suits)
            for (rank in ranks)
                cards.add(Card(rank, suit))
        cards.shuffle()
    }

    fun take(n: Int): List<Card> {
        val up = cards.take(n).toList()
        cards.removeAll(up)
        return up
    }
}

class Player (val name: String, val deck: Deck, val table: MutableList<Card>) {
    var hand: MutableList<Card> = mutableListOf()
    var quit = false
    var score = 0
    var cards = 0

    fun showPlayerHand(): String {
        val sb = StringBuilder("Cards in hand:")
        hand.forEachIndexed { index, card -> sb.append(" ${index + 1})$card") }
        return sb.toString()
    }

    fun showComputerHand(): String = hand.joinToString(" ")

    fun move(card: Card) {
        hand.remove(card)
        table.add(card)
    }

    fun deal() {
        hand.addAll(deck.take(6))
    }

    fun chooseComputerCard(): Card
    {
        val last = table.lastOrNull()
        var candidates = hand.filter { it.suit == last?.suit }
        if (candidates.isEmpty())
            candidates = hand.filter { it.rank == last?.rank }
        if (candidates.isEmpty())
            candidates = hand
        if (candidates.size == 1)
            return candidates.first()
        val suited = candidates.groupBy { it.suit }.maxByOrNull { it.value.size }!!.value
        val ranked = candidates.groupBy { it.rank }.maxByOrNull { it.value.size }!!.value
        candidates = if (ranked.size > suited.size) ranked else suited
        return candidates.random()
    }

    fun makeComputerMove(): Card {
        println(showComputerHand())
        val card = chooseComputerCard()
        println("Computer plays $card")
        move(card)
        return card
    }

    fun makePlayerMove(): Card? {
        println(showPlayerHand())
        while (true) {
            println("Choose a card to play (1-${hand.size}):")
            val move = readln()
            if ("exit".equals(move, true)) {
                quit = true
                return null
            }
            val index = move.toIntOrNull()
            if (index != null && 0 < index && index <= hand.size) {
                val card = hand[index - 1]
                move(card)
                return card
            }
        }
    }

    fun score(cards: List<Card>) {
        this.cards += cards.size
        this.score += cards.sumOf { it.score() }
    }
}

class Game () {
    val deck = Deck()
    val table = deck.take(4).toMutableList()
    val player = Player("Player", deck, table)
    val computer = Player("Computer", deck, table)
    var playerMove: Boolean = true

    private fun deal() {
        player.deal()
        computer.deal()
    }

    private fun getInitialMove() : Boolean {
        var answer: YesNo? = null
        while (answer == null) {
            println("Play first?")
            val command = readln()
            answer = YesNo.values().find { it.name.equals(command, true) }
        }
        return answer == YesNo.yes
    }

    fun run() {
        println(Title)
        playerMove = getInitialMove()
        deal()
        var winner: Player? = null
        println("Initial cards on the table: ${table.joinToString(" ")}")
        while (true) {
            val last = table.lastOrNull()
            if (last == null)
                println("No cards on the table")
            else
                println("${table.size} cards on the table, and the top card is ${last}")
            val playing = if (playerMove)
                this.player
            else
                this.computer
            if (winner == null)
                winner = playing
            if (playing.hand.isEmpty())
                if (deck.cards.isEmpty())
                    break
                else
                    playing.deal()
            val card = if (playerMove)
                player.makePlayerMove()
            else
                computer.makeComputerMove()
            if (card == null)
                break
            if (card.rank == last?.rank || card.suit == last?.suit) {
                println("${playing.name} wins cards")
                playing.score(table)
                table.clear()
                winner = playing
                println("Score: ${player.name} ${player.score} - ${computer.name} ${computer.score}")
                println("Cards: ${player.name} ${player.cards} - ${computer.name} ${computer.cards}")
            }
            playerMove = !playerMove
        }
        if (!player.quit && !computer.quit) {
            winner!!.score(table)
            if (player.cards > computer.cards)
                player.score += 3
            else if (player.cards < computer.cards)
                computer.score += 3
            else
                winner.score += 3
            println("Score: ${player.name} ${player.score} - ${computer.name} ${computer.score}")
            println("Cards: ${player.name} ${player.cards} - ${computer.name} ${computer.cards}")
        }
        println("Game over")
    }
}

fun main() {
    val game = Game()
    game.run()
}

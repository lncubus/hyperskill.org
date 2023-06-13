package flashcards

import java.io.File

data class Card(
    val content: String,
    val definition: String,
    var mistakes:Int)

enum class Command {
    ADD,
    REMOVE,
    IMPORT,
    EXPORT,
    ASK,
    EXIT,
    LOG,
    HARDEST_CARD,
    RESET_STATS
}

val logBuffer = StringBuilder()

fun output(message: String) {
    println(message)
    logBuffer.appendLine(message)
}

fun input(message: String): String {
    output(message)
    val answer = readln()
    logBuffer.appendLine(answer)
    return answer
}

val prompt = "Input the action (" +
        enumValues<Command>().joinToString(", ")
        { it.toString().lowercase().replace('_', ' ') } +
        "):"

fun inputCommand(): Command {
    while (true) {
        val command = input(prompt)
        try {
            return Command.valueOf(command.uppercase().replace(' ', '_'))
        } catch (e: IllegalArgumentException) {
            output("Unknown action \"$command\"")
        }
    }
}

fun add (cards: MutableCollection<Card>) {
    val term = input("The card:")
    if (cards.any { it.content == term }) {
        output("The card \"$term\" already exists.")
    } else {
        val definition = input("The definition of the card:")
        if (cards.any { it.definition == definition }) {
            output("The definition \"$definition\" already exists.")
        } else {
            cards.add(Card(term, definition, 0))
            output("The pair (\"$term\":\"$definition\") has been added.")
        }
    }
}

fun remove(cards: MutableCollection<Card>) {
    val term = input("Which card?")
    if (cards.any { it.content == term }) {
        cards.removeIf { it.content == term }
        output("The card has been removed.")
    } else {
        output("Can't remove \"$term\": there is no such card.")
    }
}

fun export(cards: Collection<Card>, filename: String) {
    val file = File(filename)
    val lines = cards.joinToString("\n") { "${it.content}:${it.definition}:${it.mistakes}" }
    file.writeText(lines)
    output("${cards.size} cards have been saved.")
}

fun import(cards: MutableList<Card>, filename: String) {
    if (!File(filename).exists()) {
        output("File not found.")
        return
    }
    val file = File(filename)
    val lines = file.readLines()
    for (line in lines) {
        val parts = line.split(":")
        if (parts.size < 2)
            continue
        val term = parts[0]
        val definition = parts[1]
        val mistakes = if (parts.size > 2)
            parts[2].toInt() else 0
        val card = Card(term, definition, mistakes)
        if (cards.any { it.content == term })
            cards.removeIf { it.content == term }
        cards.add(card)
    }
    output("${lines.size} cards have been loaded.")
}

fun parseArgs(vararg args: String): Map<String, String> {
    val options = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
        val arg = args[i]
        if (arg.startsWith("-")) {
            val option = arg.substring(1)
            if (i + 1 < args.size && !args[i + 1].startsWith("-")) {
                options[option] = args[i + 1]
                i++
            } else {
                options[option] = ""
            }
        }
        i++
    }
    return options
}

fun main(args: Array<String>) {
    val cards = mutableListOf<Card>()
    val options = parseArgs(*args)
    if (options.containsKey("import"))
        import(cards, options["import"]!!)
    while (true) {
        val command = inputCommand()
        when (command) {
            Command.ADD -> add(cards)
            Command.REMOVE -> remove(cards)
            Command.IMPORT -> {
                val filename = input("File name:")
                import(cards, filename)
            }
            Command.EXPORT -> {
                val filename = input("File name:")
                export(cards, filename)
            }
            Command.ASK -> ask(cards)
            Command.HARDEST_CARD -> hardestCard(cards)
            Command.RESET_STATS -> resetStats(cards)
            Command.LOG -> writeLog()
            Command.EXIT -> {
                output("Bye bye!")
                break
            }
        }
        if (options.containsKey("export"))
            export(cards, options["export"]!!)
    }
}

fun writeLog() {
    val filename = input("File name:")
    val file = File(filename)
    file.writeText(logBuffer.toString())
    output("The log has been saved.")
}

fun hardestCard(cards: Collection<Card>) {
    val worst = cards.maxOfOrNull { it.mistakes } ?: 0
    if (worst == 0 ) {
        output("There are no cards with errors.")
        return
    }
    val hardest = cards.filter { it.mistakes == worst }
    val terms = hardest.joinToString(", ") { "\"${ it.content }\"" }
    if (hardest.size == 1) {
        output("The hardest card is $terms. " +
                "You have $worst errors answering it.")
    } else {
        output("The hardest cards are $terms. " +
                "You have $worst errors answering them.")
    }
}

fun resetStats(cards: Collection<Card>) {
    cards.forEach { it.mistakes = 0 }
    output("Card statistics have been reset.")
}

fun ask(cards: Collection<Card>) {
    val n = input("How many times to ask?").toInt()
    for (i in 1..n) {
        val card = cards.random()
        val term = card.content
        val definition = card.definition
        val answer = input("Print the definition of \"$term\":")
        if (answer == definition) {
            output("Correct!")
        } else {
            card.mistakes++
            val valid = cards.find { it.definition == answer }
            output("Wrong. The right answer is \"$definition\"" +
                if (valid == null) "." else
                    ", but your definition is correct for \"${valid.content}\"")
        }
    }
}

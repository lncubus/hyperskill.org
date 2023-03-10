package wordsvirtuoso

import java.awt.Color
import java.io.File
import java.util.TreeSet
import kotlin.system.exitProcess

fun prompt(message: String): String {
    println(message)
    return readln()
}

enum class CheckWordResult(val message: String) {
    OK("The input is a valid string."),
    INVALID_LENGTH("The input isn't a 5-letter word."),
    INVALID_CHARACTERS("One or more letters of the input aren't valid."),
    DUPLICATE_LETTERS("The input has duplicate letters."),
}

fun checkWord(word: String): CheckWordResult {
    val wordLength = word.length
    if (wordLength != 5) {
        return CheckWordResult.INVALID_LENGTH
    }
    val wordSet = word.toSet()
    if (wordSet.any { it !in 'a'..'z' && it !in 'A'..'Z' }) {
        return CheckWordResult.INVALID_CHARACTERS
    }
    if (wordSet.size != wordLength) {
        return CheckWordResult.DUPLICATE_LETTERS
    }
    return CheckWordResult.OK
}

enum class FileRole(val called: String) {
    WORDS("The words file"),
    CANDIDATES("The candidate words file"),
}

enum class CheckFileResult() {
    OK,
    DOES_NOT_EXIST,
    INVALID_WORDS,
}

enum class TextColor(val code: String) {
    RESET("\u001B[0m"),
    RED("\u001B[48:5:1m"),
    GREEN("\u001B[48:5:10m"),
    YELLOW("\u001B[48:5:11m"),
    GRAY("\u001B[48:5:7m"),
    AZURE("\u001B[48:5:14m");
    override fun toString(): String = code
}

class WordsFile(val role: FileRole, val fileName: String) {
    val checkFileResult: CheckFileResult
    val words: Set<String>
    val countInvalid: Int

    init {
        val file = File(fileName)
        if (!file.exists()) {
            words = emptySet()
            countInvalid = 0
            checkFileResult = CheckFileResult.DOES_NOT_EXIST
        } else {
            words = file.readLines().map { it.lowercase() }.toSet()
            countInvalid = words.count { checkWord(it) != CheckWordResult.OK }
            checkFileResult =
                if (countInvalid > 0) CheckFileResult.INVALID_WORDS else CheckFileResult.OK
        }
    }

    fun ErrorMessage(): String {
        return when (checkFileResult) {
            CheckFileResult.DOES_NOT_EXIST -> "Error: ${role.called} $fileName doesn't exist."
            CheckFileResult.INVALID_WORDS -> "Error: $countInvalid invalid words were found in the $fileName file."
            else -> ""
        }
    }
}

fun Guess(guess: String, word: String): String {
    val sb: StringBuilder = StringBuilder()
    for (i in guess.indices) {
        val c = guess[i]
        if (c == word[i])
            sb.append(TextColor.GREEN)
        else if (c in word)
            sb.append(TextColor.YELLOW)
        else
            sb.append(TextColor.GRAY)
        sb.append(c.uppercase())
        sb.append(TextColor.RESET)
    }
    return sb.toString()
}


fun terminate(message: String) {
    println(message)
    exitProcess(0)
}

fun main(args: Array<String>) {
    if (args.size != 2)
        terminate("Error: Wrong number of arguments.")
    val wordsFile = WordsFile(FileRole.WORDS, args[0])
    val candidatesFile = WordsFile(FileRole.CANDIDATES, args[1])
    if (wordsFile.checkFileResult == CheckFileResult.DOES_NOT_EXIST)
        terminate(wordsFile.ErrorMessage())
    if (candidatesFile.checkFileResult == CheckFileResult.DOES_NOT_EXIST)
        terminate(candidatesFile.ErrorMessage())
    if (wordsFile.checkFileResult == CheckFileResult.INVALID_WORDS)
        terminate(wordsFile.ErrorMessage())
    if (candidatesFile.checkFileResult == CheckFileResult.INVALID_WORDS)
        terminate(candidatesFile.ErrorMessage())
    val missing = candidatesFile.words - wordsFile.words
    if (missing.any())
        terminate("Error: ${missing.size} candidate words are not included in the ${wordsFile.fileName} file.")
    println("Words Virtuoso")
    val word = candidatesFile.words.random()
    var count = 0
    val start = System.currentTimeMillis()
    val suggestions = mutableListOf<String>()
    val wordSet = word.toSet()
    val excluded = TreeSet<Char>()
    while (true) {
        val guess = prompt("Input a 5-letter word:").lowercase()
        count++
        if (guess == "exit")
            terminate("The game is over.")
        val checkWordResult = checkWord(guess)
        if (checkWordResult != CheckWordResult.OK) {
            println(checkWordResult.message)
            continue
        }
        if (guess !in wordsFile.words) {
            println("The input word isn't included in my words list.")
            continue
        }
        val suggestion = Guess(guess, word)
        suggestions.add(suggestion)
        excluded.addAll((guess.toSet() - wordSet).map { it.uppercase().single() })
        println(suggestions.joinToString("\n"))
        if (guess == word) {
            println("Correct!")
            break
        }
        print(TextColor.AZURE)
        print(excluded.joinToString(""))
        println(TextColor.RESET)
    }
    val end = System.currentTimeMillis()
    val time = (end - start) / 1000
    if (count == 1)
        println("Amazing luck! The solution was found at once.")
    else
        println("The solution was found after $count tries in $time seconds.")
}

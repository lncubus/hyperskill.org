package signature

import java.net.URL
import java.util.*

const val RomanFont = "https://stepik.org/media/attachments/lesson/226140/roman.txt"
const val MediumFont = "https://stepik.org/media/attachments/lesson/226140/medium.txt"

class Font(private val url: String, private val space: Int) {
    inner class Character(val height: Int, val width: Int, val lines: Array<String>) {
        init {
            require(lines.size == height)
            require(lines.all { it.length == width })
        }
    }
    val height: Int
    fun width(char: Char): Int = characters[char]?.width ?: space
    fun width(string: String): Int = string.sumOf { width(it) }

    private val count: Int
    private val characters = mutableMapOf<Char, Character>()

    init {
        val fontStream = URL(url).openStream()
        val scanner = Scanner(fontStream)
        height = scanner.nextInt()
        count = scanner.nextInt()
        for (i in 0 until count) {
            val char = scanner.next().single()
            val width = scanner.nextInt()
            scanner.nextLine()
            val lines = Array(height) { "" }
            for (j in 0 until height)
                lines[j] = scanner.nextLine()
            val character = Character(height, width, lines)
            characters[char] = character
        }
    }

    fun write(prefix: String, string: String, suffix: String) {
        for (i in 0 until height) {
            print(prefix)
            for (c in string) {
                if (c !in characters.keys)
                    print(" ".repeat(space))
                else {
                    val character = characters[c]!!
                    print(character.lines[i])
                }
            }
            println(suffix)
        }
    }
}

fun prompt(message: String): String {
    print(message)
    return readln()
}

fun main() {
    val roman = Font(RomanFont, 10)
    val medium = Font(MediumFont, 5)
    val name = prompt("Enter name and surname: ")
    val status = prompt("Enter person's status: ")
    val nameWidth = roman.width(name)
    val statusWidth = medium.width(status)
    val bannerWidth = maxOf(nameWidth, statusWidth) // + 9
    val spacesBeforeName = (bannerWidth - nameWidth) / 2
    val spacesAfterName = bannerWidth - nameWidth - spacesBeforeName
    val spacesBeforeStatus = (bannerWidth - statusWidth) / 2
    val spacesAfterStatus = bannerWidth - statusWidth - spacesBeforeStatus

    val prefixName = "88  " + " ".repeat(spacesBeforeName)
    val suffixName = " ".repeat(spacesAfterName) + "  88"
    val prefixStatus = "88  " + " ".repeat(spacesBeforeStatus)
    val suffixStatus = " ".repeat(spacesAfterStatus) + "  88"

    println("8".repeat(bannerWidth + 8))
    roman.write(prefixName, name, suffixName)
    medium.write(prefixStatus, status, suffixStatus)
    println("8".repeat(bannerWidth + 8))
}

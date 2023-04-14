package sorting

import java.io.InputStream
import java.util.Scanner

abstract class Processor<T> (val type: String, val separator: String = " ") where T : Comparable<T> {
    val list = mutableListOf<T>()
    abstract fun read(scanner: Scanner): T

    fun run(input: InputStream) {
        val scanner = Scanner(input)
        while (scanner.hasNext()) {
            val input = read(scanner)
            list.add(input)
        }
        list.sort()
        scanner.close()
    }

    override fun toString(): String =
        "Total ${type}s: ${list.size}."

    fun toString(sorting: SortingType): String =
        when (sorting) {
            SortingType.NATURAL -> "Sorted data:" + separator + list.joinToString(separator)
            SortingType.BYCOUNT -> {
                val map = mutableMapOf<T, Int>()
                for (word in list)
                    map[word] = map.getOrDefault(word, 0) + 1
                val sorted = map.toList().sortedBy { it.second }
                val result = StringBuilder()
                for ((word, count) in sorted)
                    result.append("$word: $count time(s), ${count * 100 / list.size}%\n")
                result.toString()
            }
        }
}

class WordProcessor() : Processor<String>("word") {
    override fun read(scanner: Scanner): String =
        scanner.next()
}

class LineProcessor() : Processor<String>("line", "\n") {
    override fun read(scanner: Scanner): String =
        scanner.nextLine()
}

class LongProcessor() : Processor<Long>("number") {
    override fun read(scanner: Scanner): Long =
        scanner.nextLong()
}

fun parse(arguments: MutableMap<String, String>, args: Array<String>) {
    var key = ""
    for (arg in args)
        if (arg.startsWith("-"))
            key = arg
        else
            arguments[key] = arg
}

enum class DataType(val processor: Processor<*>) {
    LONG(LongProcessor()),
    LINE(LineProcessor()),
    WORD(WordProcessor())
}

enum class SortingType() {
    NATURAL,
    BYCOUNT
}

fun main(args: Array<String>) {
    // write your code here
    val arguments =
        mutableMapOf("-dataType" to "word", "-sortingType" to "natural")
    parse(arguments, args)
    val dataType = DataType.valueOf(arguments["-dataType"]!!.uppercase())
    val sortingType = SortingType.valueOf(arguments["-sortingType"]!!.uppercase())
    val processor = dataType.processor
    val inputFile = arguments["-inputFile"]
    val input = if (inputFile == null)
        System.`in` else
        java.io.File(inputFile).inputStream()
    val outputFile = arguments["-outputFile"]
    val output = if (outputFile == null)
        System.out else
        java.io.PrintStream(java.io.File(outputFile))
    processor.run(input)
    output.println(processor)
    output.println(processor.toString(sortingType))
}

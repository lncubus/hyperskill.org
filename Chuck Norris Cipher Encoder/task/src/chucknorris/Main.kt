package chucknorris

fun Char.toBin() = this.code.toString(radix = 2).padStart(7, '0')

fun rle(s: String) = sequence {
    var position = 0
    while (position < s.length) {
        val current = s[position]
        var count = 0
        while (position < s.length && current == s[position]) {
            position++
            count++
        }
        yield(Pair(current, count))
    }
}

fun String.toBinary() = this.map { it.toBin() }.joinToString("")

fun encode(data: String) = rle(data.toBinary()).map {
    (if (it.first == '0') "00" else "0") + ' ' + "0".repeat(it.second) }.
    joinToString(" ")

const val InvalidCharacters =
    "The encoded message includes characters other than 0 or spaces"
const val InvalidBlocksNumber =
    "The number of blocks is odd"
const val InvalidHeader =
    "The first block of each sequence is not 0 or 00"
const val InvalidBitsNumber =
    "The length of the decoded binary string is not a multiple of 7"

fun decode(data: String): Pair<String?, String?> {
    if (data.any { it != '0' && it != ' '})
        return Pair(null, InvalidCharacters)
    val zeros = data.trim().split(' ', )
    if (zeros.size % 2 != 0)
        return Pair(null, InvalidBlocksNumber)
    val chunks = zeros.chunked(2)
    if (chunks.any { it[0] != "0" && it[0] != "00"} )
        return Pair(null, InvalidHeader)
    val bits = chunks.map {
            (if (it[0] != "0") "0" else "1").repeat(it[1].length)
        }.joinToString("")
    if (bits.length % 7 != 0)
        return Pair(null, InvalidBitsNumber)
    val chars = bits.chunked(7).map { it.toInt(radix = 2).toChar() }
    return Pair(chars.joinToString(""), null)
}

fun assertEquals(a: Any, b: Any) = if (a != b) throw AssertionError() else Unit

fun test() {
    assertEquals(encode("Hey!"),
        "0 0 00 00 0 0 00 000 0 00 00 00 0 0 00 0 0 00000 00 00 0 0 00 0 0 0 00 0000 0 0")
    assertEquals(decode("0 0 00 00 0 0 00 000 0 00 00 00 0 0 00 0 0 00000 00 00 0 0 00 0 0 0 00 0000 0 0"),
        Pair("Hey!", null))
    assertEquals(decode("0 0 00 00 0 0 00 000"), Pair("H", null))
    assertEquals(decode("0 0 1 00 0 0 1 000"), Pair(null, InvalidCharacters))
    assertEquals(decode("000 0 00 00 0000 0 00 000"), Pair(null, InvalidHeader))
    assertEquals(decode("0 0 00 00 0 0 00"), Pair(null, InvalidBlocksNumber))
    assertEquals(decode("0 0 00 00 0 0 00 00"), Pair(null, InvalidBitsNumber))
}

fun main() {
    //test()

    while (true) {
        println("Please input operation (encode/decode/exit):")
        when (val operation = readln()) {
            "exit" -> break
            "encode" -> {
                println("Input string:")
                val data = readln()
                val encoded = encode(data)
                println("Encoded string:")
                println(encoded)
            }
            "decode" -> {
                println("Input encoded string:")
                val data = readln()
                val (decoded, error) = decode(data)
                if (error != null)
                    println("Encoded string is not valid.")
                else {
                    println("Decoded string:")
                    println(decoded)
                }
            }
            else -> println("There is no '$operation' operation")
        }
    }
    println("Bye!")
}

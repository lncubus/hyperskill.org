package converter

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

// Do not delete this line

fun prompt(message: String): String {
    print(message)
    return readln()
}

val digits = (0..9).map { '0' + it } + ('a'..'z').map { it }

fun convertFractionToBase(num: BigDecimal, b: Int): String {
    var number = num
    val result = StringBuilder()
    val base = b.toBigDecimal()
    while (result.length < 5) {
        number *= base
        val digit = number.toInt()
        result.append(digits[digit])
        number -= digit.toBigDecimal()
    }
    return result.toString()
}

fun convertToBase(num: BigInteger, base: BigInteger): String {
    var number = num
    val result = StringBuilder()
    while (number.compareTo(BigInteger.ZERO) != 0) {
        val divmod= number.divideAndRemainder(base)
        result.append(digits[divmod[1].toInt()])
        number = divmod[0]
    }
    result.reverse()
    return result.toString()
}

fun convertFractionFromBase(number: String, b: Int): BigDecimal {
    var result = BigDecimal.ZERO
    val base = b.toBigDecimal()
    var power = BigDecimal.ONE
    for (c in number) {
        power = power.divide(base, 10, RoundingMode.HALF_UP)
        val digit = digits.indexOf(c).toBigDecimal()
        if (digit >= base)
            throw IllegalArgumentException("Invalid digit $c for base $base")
        result += digit * power
    }
    return result
}

fun convertFromBase(number: String, base: BigInteger): BigInteger {
    var result = BigInteger.ZERO
    for (c in number) {
        val digit = digits.indexOf(c)
        if (digit >= base.toInt()) {
            throw IllegalArgumentException("Invalid digit $c for base $base")
        }
        result = result * base + digit.toBigInteger()
    }
    return result
}

fun main() {
    while (true) {
        val commandPrompt1 =
            "Enter two numbers in format: {source base} {target base} (To quit type /exit"
        val bases = prompt(commandPrompt1).trim()
        if (bases == "/exit")
            break
        val (source, target) =
            bases.split(" ").map { it.toInt() }
        val commandPrompt2 =
            "Enter number in base $source to convert to base $target (To go back type /back):"
        while (true) {
            val number = prompt(commandPrompt2).trim()
            if (number == "/back")
                break
            try {
                if (number.contains('.')) {
                    val (intPart, fracPart) = number.split('.')
                    val convertedInt = convertToBase(convertFromBase(intPart,
                        source.toBigInteger()), target.toBigInteger())
                    val convertedFraction = convertFractionToBase(
                        convertFractionFromBase(fracPart, source), target)
                    println("Conversion result: $convertedInt.$convertedFraction")
                    continue
                } else {
                    val converted = convertToBase(convertFromBase(number,
                        source.toBigInteger()), target.toBigInteger())
                    println("Conversion result: $converted")
                }
            } catch (e: IllegalArgumentException) {
                println("Conversion error: ${e.message}")
            }
        }
    }
}

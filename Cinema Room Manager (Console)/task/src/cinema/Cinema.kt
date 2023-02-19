package cinema

const val GoodPrice = 10
const val FairPrice = 8

class Cinema(val rows: Int, val seats: Int) {
    val occupied = mutableSetOf<Pair<Int, Int>>()
    val total = rows * seats
    val totalIncome = if (total <= 60)
        total * GoodPrice
    else
        (rows / 2) * seats * GoodPrice + (rows - rows / 2) * seats * FairPrice
    var currentIncome = 0

    fun drawCinema(): String {
        val floor = StringBuilder()
        floor.append(" ")
        for (s in 1..seats)
            floor.append(" ").append(s)
        floor.appendLine()
        for (r in 1..rows) {
            floor.append(r)
            for (s in 1..seats)
                floor.append(" ").append(if (occupied.contains(Pair(r, s))) 'B' else 'S')
            floor.appendLine()
        }
        return floor.toString()
    }

    fun getPrice(row: Int, seat: Int): Int =
        if (row !in 1..rows || seat !in 1..seats)
            throw IndexOutOfBoundsException()
        else {
            if (rows * seats <= 60) {
                GoodPrice
            } else {
                if (row <= rows / 2) {
                    GoodPrice
                } else {
                    FairPrice
                }
            }
        }

    fun book(row: Int, seat: Int): Boolean =
        if (row !in 1..rows || seat !in 1..seats)
            throw IndexOutOfBoundsException()
        else
            occupied.add(Pair(row, seat))
}

const val Propmt = "1. Show the seats\n2. Buy a ticket\n3. Statistics\n0. Exit"

fun main() {
    // write your code here
    println("Enter the number of rows:")
    val rows = readln().toInt()
    println("Enter the number of seats in each row:")
    val seats = readln().toInt()
    val cinema = Cinema(rows, seats)
    while (true) {
        println(Propmt)
        val choice = readln()
        when (choice) {
            "1" -> {
                println("Cinema:")
                println(cinema.drawCinema())
            }
            "2" -> {
                while (true) {
                    try {
                        println("Enter a row number:")
                        val row = readln().toInt()
                        println("Enter a seat number in that row:")
                        val seat = readln().toInt()
                        val okay = cinema.book(row, seat)
                        if (!okay)
                            println("That ticket has already been purchased!")
                        else {
                            val price = cinema.getPrice(row, seat)
                            cinema.currentIncome += price
                            println("Ticket price: \$$price")
                            break
                        }
                    } catch (_: Exception) {
                        println("Wrong input!")
                    }
                }
            }
            "3" -> {
                println("Number of purchased tickets: ${cinema.occupied.size}")
                val percent = "%.2f".format(
                    cinema.occupied.size * 100.0/cinema.total)
                println("Percentage: ${percent}%")
                println("Current income: \$${cinema.currentIncome}")
                println("Total income: \$${cinema.totalIncome}")
            }
            "0" -> break
        }
    }
}
package parking

import java.util.function.Predicate

class Car(val number: String, val color: String) {
    constructor(arguments: List<String>):
        this(arguments.first(), arguments.last())
    override fun toString(): String = "$number $color"

    fun sameColor(color: String): Boolean = this.color.equals(color, true)
    fun sameNumber(number: String): Boolean = this.number == number
}

class Parking {
    var size = 0
    val slots = mutableMapOf<Int, Car>()

    fun reset(resize: Int) {
        size = resize
        slots.clear()
    }

    private fun findSpot(): Int? {
        if (slots.size >= size)
            return null
        for (i in 1..size)
            if (i !in slots)
                return i
        return null
    }

    fun park(car: Car): Int? {
        val index = findSpot()
        if (index != null)
            slots[index] = car
        return index
    }

    fun leave(index: Int) = slots.remove(index)

    fun spotBy(predicate: Predicate<Car>): List<Int> =
        slots.filter { predicate.test(it.value) }.keys.toList()

    fun regBy(predicate: Predicate<Car>): List<String> =
        slots.filter { predicate.test(it.value) }.values.map { it.number }

    override fun toString(): String =
        if (slots.isEmpty())
            "Parking lot is empty."
        else
            slots.toSortedMap().map { (index, car) ->
                "$index $car"
            }.joinToString("\n")
}

class Interactive() {
    val parking = Parking()

    fun run() {
        while (true) {
            val input = readln().split(" ")
            val command = input.firstOrNull()
            val arguments = input.drop(1)
            when (command) {
                "exit" -> break
                "create" -> {
                    val size = arguments.first().toInt()
                    parking.reset(size)
                    println("Created a parking lot with $size spots.")
                    continue
                }
            }
            if (parking.size == 0) {
                println("Sorry, a parking lot has not been created.")
                continue
            }
            when (command) {
                "park" -> {
                    val car = Car(arguments)
                    val parked = parking.park(car)
                    if (parked == null)
                        println("Sorry, the parking lot is full.")
                    else
                        println("${car.color} car parked in spot $parked.")
                }
                "leave" -> {
                    val index = arguments.first().toInt()
                    val car = parking.leave(index)
                    if (car == null)
                        println("There is no car in spot $index.")
                    else
                        println("Spot $index is free.")
                }
                "status" -> println(parking)
                "reg_by_color" -> {
                    val color = arguments.single()
                    val cars = parking.regBy { it.sameColor(color) }
                    if (cars.isEmpty())
                        println("No cars with color $color were found.")
                    else
                        println(cars.joinToString(", "))
                }
                "spot_by_reg" -> {
                    val number = arguments.single()
                    val positions = parking.spotBy { it.sameNumber(number) }
                    if (positions.isEmpty())
                        println("No cars with registration number $number were found.")
                    else
                        println(positions.joinToString(", "))
                }
                "spot_by_color" -> {
                    val color = arguments.single()
                    val positions = parking.spotBy { it.sameColor(color) }
                    if (positions.isEmpty())
                        println("No cars with color $color were found.")
                    else
                        println(positions.joinToString(", "))
                }
            }
        }
    }
}

fun main() {
    Interactive().run()
}

//    println("White car has parked.")
//    println("Yellow car left the parking lot.")
//    println("Green car just parked here.")


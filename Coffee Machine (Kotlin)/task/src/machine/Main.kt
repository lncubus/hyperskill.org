package machine

/*
    println("Write how many ml of water the coffee machine has:")
    val water = readln().toInt()
    println("Write how many ml of milk the coffee machine has:")
    val milk = readln().toInt()
    println("Write how many grams of coffee beans the coffee machine has:")
    val coffee = readln().toInt()
    val tops = minOf(water / 200, milk / 50, coffee / 15)
    println("Write how many cups of coffee you will need:")
    val cups = readln().toInt()
    when {
        tops < cups -> println("No, I can make only $tops cups of coffee")
        tops > cups -> println("Yes, I can make that amount of coffee (and even ${tops - cups} more than that)")
        tops == cups -> println("Yes, I can make that amount of coffee")
    }
    println("For $n cups of coffee you will need:")
    println("${200*n} ml of water")
    println("${50*n} ml of milk")
    println("${15*n} g of coffee beans")
    for (line in lines)
        println(line)
val lines = listOf (
"Starting to make a coffee",
"Grinding coffee beans",
"Boiling water",
"Mixing boiled water with crushed coffee beans",
"Pouring coffee into the cup",
"Pouring some milk into the cup",
"Coffee is ready!"
)
*/

enum class Commands {
    buy,
    fill,
    take,
    remaining,
    exit
}

enum class Coffee {
    espresso,
    latte,
    cappuccino
}

data class Recipe(val water: Int, val milk: Int, val coffee: Int, val cost: Int) {
}

class CoffeeMaker(var water: Int, var milk: Int, var coffee: Int,
                  var cups: Int, var money: Int) {

    val recipes = mapOf(
        Coffee.espresso to Recipe(water = 250, milk = 0, coffee = 16, cost = 4),
        Coffee.latte to Recipe(water = 350, milk = 75, coffee = 20, cost = 7),
        Coffee.cappuccino to Recipe(water = 200, milk = 100, coffee = 12, cost = 6)
    )

    fun take() {
        println("I gave you \$$money")
        money = 0
    }

    fun fill() {
        println("Write how many ml of water do you want to add:")
        water += readln().toInt()
        println("Write how many ml of milk do you want to add:")
        milk += readln().toInt()
        println("Write how many grams of coffee beans do you want to add:")
        coffee += readln().toInt()
        println("Write how many disposable cups of coffee do you want to add:")
        cups += readln().toInt()
    }

    fun buy() {
        println("What do you want to buy? 1 - espresso, 2 - latte, 3 - cappuccino, back - to main menu:")
        val kind = readln()
        if (kind == "back")
            return
        val recipe = recipes.getValue(Coffee.values()[kind.toInt() - 1])
        make(recipe)
    }

    fun make(recipe: Recipe) {
        when {
            water < recipe.water -> println("Sorry, not enough water!")
            milk < recipe.milk -> println("Sorry, not enough milk!")
            coffee < recipe.coffee -> println("Sorry, not enough coffee!")
            cups < 1 -> println("Sorry, not enough cups!")
            else -> {
                water -= recipe.water
                milk -= recipe.milk
                coffee -= recipe.coffee
                cups -= 1
                money += recipe.cost
                println("I have enough resources, making you a coffee!")
            }
        }
    }

    fun remaining() {
        println(this)
    }

    override fun toString(): String =
        "The coffee machine has:\n" +
                "$water ml of water\n" +
                "$milk ml of milk\n" +
                "$coffee g of coffee beans\n" +
                "$cups disposable cups\n" +
                "\$$money of money"
}

fun main() {
    val coffeeMaker = CoffeeMaker(
        water = 400, milk = 540, coffee = 120,
        cups = 9, money = 550
    )
    //println(coffeeMaker)
    while (true) {
        val commands = Commands.values().joinToString(separator = ", ")
        println("Write action ($commands):")
        val command = Commands.valueOf(readln())
        when (command) {
            Commands.fill -> coffeeMaker.fill()
            Commands.buy -> coffeeMaker.buy()
            Commands.take -> coffeeMaker.take()
            Commands.remaining -> coffeeMaker.remaining()
            Commands.exit -> break
        }
    }
}
//    }


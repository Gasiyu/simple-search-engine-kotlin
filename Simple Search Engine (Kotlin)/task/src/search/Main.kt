package search

import java.io.File
import kotlin.system.exitProcess

val people = mutableListOf<String>()
val invertedIndex = mutableMapOf<String, MutableList<Int>>()

enum class SearchStrategy {
    ALL, ANY, NONE
}

class PersonFinder(val query: String, val searchStrategy: SearchStrategy) {
    var result = mutableListOf<String>()

    init {
        when (searchStrategy) {
            SearchStrategy.ALL -> getAllResult()

            SearchStrategy.ANY -> getAnyResult()

            SearchStrategy.NONE -> getInvertedResult()
        }
    }

    private fun getAllResult() = result.addAll(people.filter { person ->
        person.split(" ").map { it.lowercase() }.containsAll(query.trim().split(" "))
    })

    private fun getAnyResult() = result.addAll(people.filter { person ->
        query.trim().split(" ").any { it in person.lowercase() }
    })

    private fun getInvertedResult() = result.addAll(people.filter { person ->
        query.trim().split(" ").none { it in person.lowercase() }
    })
}

fun printMenu() {
    println("\n=== Menu ===")
    println("1. Find a person")
    println("2. Print all people")
    println("0. Exit")
}

fun findPerson() {
    println("\nSelect a matching strategy: ALL, ANY, NONE")
    val searchStrategy = SearchStrategy.valueOf(readln().trim().uppercase())

    println("\nEnter a name or email to search all matching people.")
    val searchQuery = readln().trim().lowercase()

    val personFinder = PersonFinder(query = searchQuery, searchStrategy = searchStrategy)

    if (personFinder.result.isEmpty()) {
        println("No matching people found.")
    } else {
        println("\n${personFinder.result.size} persons found:")
        println(personFinder.result.joinToString("\n"))
    }
}

fun printAllPerson() {
    println("\n=== List of people ===")
    println(people.joinToString("\n"))
}

fun loadDataFromFile(args: Array<String>) {
    try {
        if (args.isEmpty() || args.first() != "--data" || args[1].isEmpty()) throw Exception("Invalid Arguments.")

        val file = File(args[1])
        file.forEachLine { line -> people.add(line.trim()) }
        createInvertedIndex()
    } catch (e: Exception) {
        println("Error loading data: ${e.message}")
        exitProcess(-1)
    }
}

fun loadDataFromInput(args: Array<String>) {
    println("Enter the number of people:")
    val peopleCount = readln().toInt()

    println("Enter all people:")
    repeat(peopleCount) {
        people.add(readln().trim())
    }
    createInvertedIndex()
}

fun createInvertedIndex() {
    people.forEachIndexed { index, person ->
        person.split(" ").map { it.lowercase() }.forEach { keyword ->
            if (!invertedIndex.containsKey(keyword)) {
                invertedIndex[keyword] = mutableListOf(index)
            } else {
                invertedIndex[keyword]?.add(index)
            }
        }
    }
}

fun determineDataLoadingMethod(loadFromFile: Boolean): (args: Array<String>) -> Unit {
    return if (loadFromFile) ::loadDataFromFile else ::loadDataFromInput
}

val loadData = determineDataLoadingMethod(true)

fun main(args: Array<String>) {

    loadData(args)

    menu@ while (true) {
        printMenu()

        when (readln().trim()) {
            "1" -> findPerson()
            "2" -> printAllPerson()
            "0" -> {
                println("\nBye!")
                break@menu
            }

            else -> println("\nIncorrect option! Try again.")
        }
    }
}

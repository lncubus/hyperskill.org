package svcs

import java.io.File
import kotlin.io.copyTo
import java.security.MessageDigest
import java.util.*

const val help =
"""These are SVCS commands:
config     Get and set a username.
add        Add a file to the index.
log        Show commit logs.
commit     Save changes.
checkout   Restore a file.
"""

const val Folder = "vcs"
const val ConfigFile = "$Folder/config.txt"
const val IndexFile = "$Folder/index.txt"
const val LogFile = "$Folder/log.txt"
const val CommitsFolder = "$Folder/commits"

class VersionControl(val args: List<String>) {
    val command = args.firstOrNull() ?: "--help"
    val arguments = args.drop(1)
    val folder = java.io.File(Folder)
    val commitsFolder = java.io.File(CommitsFolder)
    val config = java.io.File(ConfigFile)
    val index = java.io.File(IndexFile)
    val log = java.io.File(LogFile)
    var username = ""

    init {
        for (folder in listOf(folder, commitsFolder))
            if (!folder.exists())
                folder.mkdir()
        for (file in listOf(config, index, log))
            if (!file.exists())
                file.createNewFile()
        username = config.readText()
    }

    fun description(): String {
        val start = help.indexOf("$command  ")
        val end = help.indexOf('\n', start)
        return if (start == -1)
            "\'$command\' is not a SVCS command."
        else
            help.substring(start + command.length, end).trim()
    }

    fun config() {
        if (arguments.isEmpty() && username == "") {
            println("Please, tell me who you are.")
            return
        }
        if (!arguments.isEmpty()) {
            username = arguments.first()
            config.writeText(username)
        }
        println("The username is $username.")
    }

    fun add() {
        val files = index.readLines().filter { it != "" }.toMutableList()
        if (files.isEmpty() && arguments.isEmpty()) {
            println(description())
            return
        }
        if (arguments.isNotEmpty()) {
            val file = java.io.File(arguments.first())
            if (!file.exists()) {
                println("Can\'t find \'${file.name}\'.")
                return
            }
            files.add(arguments.first())
            index.writeText(files.joinToString("\n"))
            println("The file \'${file.name}\' is tracked.")
        } else {
            println("Tracked files:")
            files.forEach { println(it) }
        }
    }

    fun log() {
        val lines = log.readLines()
        if (lines.any { it != "" }) {
            lines.forEach { println(it) }
        } else {
            println("No commits yet.")
        }
    }

    fun digest(file: String): ByteArray =
        MessageDigest.getInstance("SHA-256").
        digest(File(file).readBytes())

    fun digestOf(files: List<String>): String {
        val global = MessageDigest.getInstance("SHA-256")
        val encoder = Base64.getUrlEncoder()
        for (file in files) {
            val localHash = digest(file)
            global.update(file.toByteArray())
            global.update(localHash)
        }
        val hash = String(encoder.encode(global.digest()))
        return hash
    }

    fun lastCommit(): String =
        (log.readLines().filter { it.startsWith("commit ") }.
        firstOrNull() ?: "").substringAfter("commit ")

    fun commit() {
        if (arguments.isEmpty()) {
            println("Message was not passed.")
            return
        }
        val files = index.readLines().filter { it != "" }
        if (files.isEmpty()) {
            println("Nothing to commit.")
            return
        }
        val hash = digestOf(files)
        if (lastCommit() == hash) {
            println("Nothing to commit.")
            return
        }
        val commit = File("$CommitsFolder/$hash")
        for (file in files) {
            val original = File(file)
            val target = File("${commit.path}/${original.name}")
            original.copyTo(target, true)
        }
        val logEntry = "commit $hash\nAuthor: $username\n${arguments.joinToString(" ")}\n\n"
        val logLines = log.readLines().toList()
        log.writeText(logEntry)
        log.appendText(logLines.joinToString("\n"))
        println("Changes are committed.")
    }

    fun checkout() {
        if (arguments.isEmpty()) {
            println("Commit id was not passed.")
            return
        }
        val hash = arguments.first()
        val commit = File("$CommitsFolder/$hash")
        if (!commit.exists()) {
            println("Commit does not exist.")
            return
        }
        val files = commit.listFiles() ?: emptyArray()
        for (file in files) {
            val original = File(file.path)
            val target = File(file.name)
            original.copyTo(target, true)
        }
        println("Switched to commit $hash.")
    }

    fun execute() {
        when (command) {
            "--help" -> println(help)
            "config" -> config()
            "add" -> add()
            "log" -> log()
            "commit" -> commit()
            "checkout" -> checkout()
            else -> println(description())
        }
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println(help)
        return
    }
    val versionControl = VersionControl(args.toList())
    versionControl.execute()
}


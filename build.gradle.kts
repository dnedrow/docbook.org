import java.util.logging.Logger
import java.io.*
import java.util.logging.Level


plugins {
    id("org.docbook.task")
    id("com.xmlcalabash.task")
}

dependencies {
    modules {
        module("org.docbook:docbook-xsl-java-saxon:1.2.1-95")
        module("org.docbook:docbook-schemas:5.1-1")
    }
}

val handAuthoredPages = arrayOf(
        "index",
        "whatis",
        "meetups",
        "tc",
        "help",
        "sitemap",
        "guidelines",
        "docs/index",
        "minutes/index",
        "ns/docbook",
        "ns/index",
        "rfe/index",
        "schemas/4x",
        "schemas/4x-custom",
        "schemas/5x",
        "schemas/5x-custom",
        "schemas/archives",
        "schemas/index",
        "specs/index",
        "tdg/errata",
        "tdg/index",
        "tdg5/index",
        "tdg5/publishers/index",
        "tdg51/index",
        "tools/index",
        "docs/howto/index",
        "docs/transclusion/index"
)

fun <T : Closeable, R> T.useWith(block: T.() -> R): R = use { with(it, block) }

handAuthoredPages.forEach {
    var taskName: String = "page_".plus(it.replace("/", "_"))
}

/**
 * This task generates an XML version of the docbook.org git log.
 */
tasks.register("gitlog") {
    group = "DocBook"
    description = "Get the current git log in XML."

    doLast {
        val pb: ProcessBuilder?
        val outFileName = "etc/git-log-summary.xml"
        pb = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            ProcessBuilder("cmd", "/c", "perl", "bin/git-log-summary")
        } else {
            ProcessBuilder("bin/git-log-summary")
        }

        val process = pb.start()
        val out = OutputHandler(process.inputStream, "UTF-8")
        val err = OutputHandler(process.errorStream, "UTF-8")
        val status = process.waitFor()

        if(status != 0) {
            this.logger.log(LogLevel.ERROR, err.text)
            throw RuntimeException("bin/git-log-summary failed with exit code: $status")
        }

        if(out.text.isEmpty()){
            val message = "bin/git-log-summary did not return any git log entries."
            throw RuntimeException(message)
        }

        println(out.text)
        println(status)
//        println("Status: $status")
//        out.join()
//        println("Output:")
//        println(out.text)
//        println()
//        err.join()
//        println("Error:")
//        println(err.text)

//        PrintWriter(outFileName).useWith {
//            println("There")
//            print(process.start().outputStream)
//        }

//        process.start().outputStream.useWith {
//            PrintWriter(outFileName).useWith {
//                print(inputs)
//            }
//        }
    }
}

internal class OutputHandler @Throws(UnsupportedEncodingException::class)
constructor(`in`: InputStream, encoding: String?) : Thread() {
    private val buf = StringBuilder()
    private val `in`: BufferedReader = BufferedReader(InputStreamReader(
            `in`, encoding ?: "UTF-8"))

    val text: String
        get() = synchronized(buf) {
            return buf.toString()
        }

    init {
        isDaemon = true
        start()
    }

    override fun run() {
        // Reading process output
        try {
            var s: String? = `in`.readLine()
            while (s != null) {
                synchronized(buf) {
                    buf.append(s)
                    buf.append('\n')
                }
                s = `in`.readLine()
            }
        } catch (ex: IOException) {
            Logger.getLogger("thisLogger").log(Level.SEVERE, null, ex)
        } finally {
            try {
                `in`.close()
            } catch (ex: IOException) {
                Logger.getLogger("thisLogger").log(Level.SEVERE, null, ex)
            }

        }
    }
}

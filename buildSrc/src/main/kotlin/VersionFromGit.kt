import java.io.File
import org.gradle.api.Project

data class VersionFromGit(
    val project: Project,
    val default: String
) {
    private val version: String by lazy {
        val process = "git describe --exact-match --match v*".execute(null, project.rootDir)
        if (process.waitFor() == 0) process.text.trim().removePrefix("v")
        else default
    }

    override fun toString(): String = version
}

fun String.execute(envp: Array<String>?, workingDir: File?) =
    Runtime.getRuntime().exec(this, envp, workingDir)

val Process.text: String
    get() = inputStream.bufferedReader().readText()

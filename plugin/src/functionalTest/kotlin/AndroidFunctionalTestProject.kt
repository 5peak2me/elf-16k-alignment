import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

internal class AndroidFunctionalTestProject(
  buildScript: String
) : AutoCloseable {
  val dir: Path = Files.createTempDirectory("elf-alignment-android-test")

  init {
    dir.resolve("settings.gradle.kts").writeText(
      """
      pluginManagement {
        repositories {
          google()
          gradlePluginPortal()
          mavenCentral()
        }
      }

      dependencyResolutionManagement {
        repositories {
          google()
          mavenCentral()
        }
      }

      rootProject.name = "functional-test"
      """.trimIndent()
    )
    dir.resolve("build.gradle.kts").writeText(buildScript.trimIndent())
    dir.resolve("local.properties").writeText(
      "sdk.dir=${androidSdk().replace("\\", "\\\\")}"
    )
    dir.resolve("src/main").createDirectories()
    dir.resolve("src/main/AndroidManifest.xml").writeText(
      """<manifest xmlns:android="http://schemas.android.com/apk/res/android"><application /></manifest>"""
    )
  }

  fun writeJni(abi: String, name: String, bytes: ByteArray) {
    val target = dir.resolve("src/main/jniLibs/$abi/$name")
    target.parent.createDirectories()
    target.writeBytes(bytes)
  }

  fun build(vararg arguments: String): BuildResult = GradleRunner.create()
    .withProjectDir(dir.toFile())
    .withArguments(
      *arguments,
      "--stacktrace",
      "--no-configuration-cache",
      "--console=plain"
    )
    .withPluginClasspath()
    .forwardOutput()
    .build()

  override fun close() {
    dir.toFile().deleteRecursively()
  }

  private fun androidSdk(): String =
    System.getenv("ANDROID_HOME")
      ?: System.getenv("ANDROID_SDK_ROOT")
      ?: error("ANDROID_HOME or ANDROID_SDK_ROOT must be configured")
}

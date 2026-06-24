import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElfAlignmentPluginFunctionalTest {

  @Test
  fun `android application registers configured alignment task`() {
    val projectDir = Files.createTempDirectory("elf-alignment-functional-test")
    try {
      projectDir.resolve("settings.gradle.kts").writeText(
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
      projectDir.resolve("build.gradle.kts").writeText(
        """
        plugins {
          id("com.android.application") version "9.2.1"
          id("io.github.5peak2me.gradle.elf-16k-alignment")
        }

        android {
          namespace = "com.example.functional"
          compileSdk = 37

          defaultConfig {
            applicationId = "com.example.functional"
            minSdk = 24
          }
        }

        elfAlignment {
          maxAlign.set(65_536L)
          resolveOnBuild.set(false)
          output {
            csv.set(true)
            html.set(true)
            json.set(true)
          }
        }

        tasks.register("verifyElfAlignmentPlugin") {
          doLast {
            val extension = project.extensions.getByType<ElfAlignmentExtension>()
            val task = tasks.named("analyzeDebugAlignment").get()

            check(extension.maxAlign.get() == 65_536L)
            check(!extension.resolveOnBuild.get())
            check(extension.output.csv.get())
            check(extension.output.html.get())
            check(extension.output.json.get())
            check(task.group == "elf-16k-alignment")
            check(task.outputs.files.singleFile.invariantSeparatorsPath.endsWith(
              "build/reports/elf-16k-alignment/debug"
            ))
          }
        }
        """.trimIndent()
      )
      projectDir.resolve("local.properties").writeText(
        "sdk.dir=${androidSdk().replace("\\", "\\\\")}"
      )
      projectDir.resolve("src/main").createDirectories()
      projectDir.resolve("src/main/AndroidManifest.xml").writeText(
        """<manifest xmlns:android="http://schemas.android.com/apk/res/android"><application /></manifest>"""
      )

      val result = GradleRunner.create()
        .withProjectDir(projectDir.toFile())
        .withArguments("verifyElfAlignmentPlugin", "--stacktrace", "--no-configuration-cache")
        .withPluginClasspath()
        .forwardOutput()
        .build()

      assertEquals(TaskOutcome.SUCCESS, result.task(":verifyElfAlignmentPlugin")?.outcome)
      assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    } finally {
      projectDir.toFile().deleteRecursively()
    }
  }

  private fun androidSdk(): String =
    System.getenv("ANDROID_HOME")
      ?: System.getenv("ANDROID_SDK_ROOT")
      ?: error("ANDROID_HOME or ANDROID_SDK_ROOT must be configured")
}

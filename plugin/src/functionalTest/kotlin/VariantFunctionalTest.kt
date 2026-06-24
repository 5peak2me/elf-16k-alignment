import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VariantFunctionalTest {

  @Test
  fun `debug variant provides command abi filters and local jni inputs`() {
    AndroidFunctionalTestProject(
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
          ndk {
            abiFilters += "arm64-v8a"
          }
        }
      }

      elfAlignment {
        resolveOnBuild.set(false)
      }

      tasks.register("verifyVariantInputs") {
        doLast {
          val inputs = tasks.named("analyzeDebugAlignment").get().inputs.properties
          val command = inputs.getValue("cmd").toString()
          val abiFilters = inputs.getValue("abiFilters") as Set<*>
          val aarLibs = inputs.getValue("aarLibs") as Map<*, *>
          val appLibs = aarLibs["app"] as Iterable<*>

          check(command.contains("./gradlew"))
          check(command.contains(":functional-test:dependencyInsight"))
          check(command.contains("debugRuntimeClasspath"))
          check(command.contains("--dependency %s"))
          check(abiFilters == setOf("arm64-v8a"))
          check(appLibs.any { it.toString().replace('\\', '/').endsWith(
            "src/main/jniLibs/arm64-v8a/liblocal.so"
          ) })
        }
      }
      """
    ).use { project ->
      project.writeJni(
        abi = "arm64-v8a",
        name = "liblocal.so",
        bytes = FunctionalElfTestData.elf64()
      )

      val result = project.build("verifyVariantInputs")

      assertEquals(TaskOutcome.SUCCESS, result.task(":verifyVariantInputs")?.outcome)
      assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
  }
}

import org.gradle.testkit.runner.TaskOutcome
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElfAlignmentTaskFunctionalTest {

  @Test
  fun `analyze task filters abi and writes enabled reports`() {
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
        maxAlign.set(16_384L)
        resolveOnBuild.set(false)
        output {
          csv.set(true)
          html.set(true)
          json.set(true)
        }
      }
      """
    ).use { project ->
      project.writeJni(
        abi = "arm64-v8a",
        name = "libaligned.so",
        bytes = FunctionalElfTestData.elf64(
          type = FunctionalElfTestData.PT_GNU_RELRO,
          align = 16_384L
        )
      )
      project.writeJni(
        abi = "x86_64",
        name = "libexcluded.so",
        bytes = FunctionalElfTestData.elf64(align = 4_096L)
      )

      val result = project.build("analyzeDebugAlignment")

      assertEquals(TaskOutcome.SUCCESS, result.task(":analyzeDebugAlignment")?.outcome)
      val reportDir = project.dir.resolve("build/reports/elf-16k-alignment/debug")
      val csv = reportDir.resolve("elf-16k-alignment.csv")
      val json = reportDir.resolve("elf-16k-alignment.json")
      val html = reportDir.resolve("elf-16k-alignment.html")
      assertTrue(csv.exists())
      assertTrue(json.exists())
      assertTrue(html.exists())

      val jsonContent = json.readText()
      assertTrue(jsonContent.contains("libaligned.so"))
      assertTrue(jsonContent.contains("arm64-v8a"))
      assertTrue(Regex(""""align"\s*:\s*16384""").containsMatchIn(jsonContent))
      assertTrue(Regex(""""relro"\s*:\s*true""").containsMatchIn(jsonContent))
      assertTrue(jsonContent.contains("debugRuntimeClasspath"))
      assertTrue(jsonContent.contains("--dependency app"))
      assertFalse(jsonContent.contains("libexcluded.so"))

      val csvContent = csv.readText()
      assertTrue(csvContent.contains("app,libaligned.so,arm64-v8a,16384,true,✅"))
      assertFalse(csvContent.contains("x86_64"))

      val htmlContent = html.readText()
      assertTrue(htmlContent.contains("libaligned.so"))
      assertFalse(htmlContent.contains("REPLACE_ME"))
    }
  }
}

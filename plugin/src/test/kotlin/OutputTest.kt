import org.gradle.api.file.DirectoryProperty
import org.gradle.testfixtures.ProjectBuilder
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OutputTest {

  @Test
  fun `create enabled always includes console and selected file outputs`() {
    val project = ProjectBuilder.builder().build()
    val parameters = project.objects.newInstance(
      ElfAlignmentTask.ElfAlignmentWorkAction.Parameters::class.java
    )
    parameters.reportDir.set(project.layout.buildDirectory.dir("reports/test"))
    parameters.csvOutput.set(true)
    parameters.htmlOutput.set(false)
    parameters.jsonOutput.set(true)

    val outputs = Output.createEnabled(parameters)

    assertEquals(3, outputs.size)
    assertIs<Output.Console>(outputs[0])
    assertIs<Output.Csv>(outputs[1])
    assertIs<Output.Json>(outputs[2])
  }

  @Test
  fun `csv output writes headers and library rows`() = withReportDirectory { dir, property ->
    Output.Csv(property).dump(sampleAarLibs())

    val content = dir.resolve("elf-16k-alignment.csv").readText()
    assertEquals(
      """
      artifact,name,abi,p_align,relro,16k compatible
      com.example:library:1.0.0,libaligned.so,arm64-v8a,16384,true,✅
      com.example:library:1.0.0,liblegacy.so,x86_64,4096,false,❌

      """.trimIndent(),
      content
    )
  }

  @Test
  fun `json output serializes report data`() = withReportDirectory { dir, property ->
    Output.Json(property).dump(sampleAarLibs())

    val content = dir.resolve("elf-16k-alignment.json").readText()
    assertTrue(content.containsJson("name", "com.example:library:1.0.0"))
    assertTrue(
      content.containsJson(
        "url",
        "https://central.sonatype.com/artifact/com.example/library/1.0.0"
      )
    )
    assertTrue(content.containsJson("name", "liblegacy.so"))
    assertTrue(Regex(""""align"\s*:\s*4096""").containsMatchIn(content))
    assertFalse(Regex(""""max"\s*:""").containsMatchIn(content))
  }

  @Test
  fun `html output injects json into template`() = withReportDirectory { dir, property ->
    Output.Html(property).dump(sampleAarLibs())

    val content = dir.resolve("elf-16k-alignment.html").readText()
    assertTrue(content.contains("<title>ELF 16KB Alignment Report</title>"))
    assertTrue(Regex("""let data\s*=\s*\[""").containsMatchIn(content))
    assertTrue(content.containsJson("name", "com.example:library:1.0.0"))
    assertFalse(content.contains("REPLACE_ME"))
  }

  @Test
  fun `html details dialog uses invoker commands`() {
    val content = requireNotNull(
      javaClass.classLoader.getResource("elf-16k-alignment.html")
    ).readText()

    assertTrue(content.contains("""<dialog id="modalOverlay""""))
    assertTrue(content.contains("""commandfor="modalOverlay" command="show-modal""""))
    assertTrue(content.contains("""commandfor="modalOverlay" command="close""""))
    assertFalse(content.contains("""modal.classList.remove('hidden')"""))
  }

  @Test
  fun `html status badge reuses statistic labels`() {
    val content = requireNotNull(
      javaClass.classLoader.getResource("elf-16k-alignment.html")
    ).readText()

    assertTrue(content.contains("""${'$'}{isCompliant ? t('statPass') : t('statFail')}"""))
    assertFalse(content.contains("compliant:"))
    assertFalse(content.contains("nonCompliant:"))
  }

  @Test
  fun `console output renders compatibility table`() {
    val original = System.out
    val output = ByteArrayOutputStream()
    try {
      System.setOut(PrintStream(output, true, Charsets.UTF_8))
      Output.Console().dump(sampleAarLibs())
    } finally {
      System.setOut(original)
    }

    val content = output.toString(Charsets.UTF_8)
    assertTrue(content.contains("com.example:library:1.0.0"))
    assertTrue(content.contains("libaligned.so"))
    assertTrue(content.contains("liblegacy.so"))
    assertTrue(content.contains("✔"))
    assertTrue(content.contains("⚠"))
    assertTrue(content.contains("Powered by Gradle & elf-16k-alignment"))
  }

  private fun sampleAarLibs() = listOf(
    AarLib(
      name = "com.example:library:1.0.0",
      cmd = "./gradlew dependencyInsight",
      jniLibs = listOf(
        JniLib(
          name = "libaligned.so",
          abi = "arm64-v8a",
          align = 16_384,
          relro = true,
          path = "/tmp/arm64-v8a/libaligned.so",
          max = 16_384
        ),
        JniLib(
          name = "liblegacy.so",
          abi = "x86_64",
          align = 4_096,
          relro = false,
          path = "/tmp/x86_64/liblegacy.so",
          max = 16_384
        )
      )
    )
  )

  private fun withReportDirectory(
    block: (java.nio.file.Path, DirectoryProperty) -> Unit
  ) {
    val dir = Files.createTempDirectory("elf-alignment-output-test")
    try {
      val project = ProjectBuilder.builder().build()
      val property = project.objects.directoryProperty().apply {
        set(dir.toFile())
      }
      block(dir, property)
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  private fun String.containsJson(name: String, value: String): Boolean =
    Regex(""""$name"\s*:\s*"\Q$value\E"""").containsMatchIn(this)
}

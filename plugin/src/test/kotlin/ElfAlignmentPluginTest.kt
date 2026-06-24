import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ElfAlignmentPluginTest {

  @Test
  fun `plugin registers extension with default values`() {
    val project = ProjectBuilder.builder().build()

    project.pluginManager.apply("io.github.5peak2me.gradle.elf-16k-alignment")

    val extension = project.extensions.findByType(ElfAlignmentExtension::class.java)
    assertNotNull(extension)
    assertEquals(16_384L, extension.maxAlign.get())
    assertTrue(extension.resolveOnBuild.get())
    assertFalse(extension.output.csv.get())
    assertFalse(extension.output.html.get())
    assertFalse(extension.output.json.get())
  }
}

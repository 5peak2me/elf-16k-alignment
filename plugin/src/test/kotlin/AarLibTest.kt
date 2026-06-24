import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AarLibTest {

  @Test
  fun `creates maven url for standard coordinates`() {
    val aarLib = AarLib(
      name = "com.example:library:1.0.0",
      cmd = "dependencyInsight"
    )

    assertEquals(
      "https://central.sonatype.com/artifact/com.example/library/1.0.0",
      aarLib.url
    )
  }

  @Test
  fun `does not create maven url for non-standard coordinates`() {
    listOf(
      "local-library.aar",
      "com.example:library",
      "com.example::1.0.0",
      "com.example:library:1.0.0:debug"
    ).forEach { name ->
      assertNull(AarLib(name = name, cmd = "dependencyInsight").url)
    }
  }
}

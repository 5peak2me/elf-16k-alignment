import kotlin.test.Test
import kotlin.test.assertEquals

class JniLibTest {

  @Test
  fun `compatibility reports compatible when alignment reaches maximum`() {
    val jniLib = jniLib(align = 16_384, max = 16_384)

    assertEquals("✅", jniLib.compatibility())
    assertEquals("✔", jniLib.compatibility(console = true))
  }

  @Test
  fun `compatibility reports incompatible when alignment is below maximum`() {
    val jniLib = jniLib(align = 4_096, max = 16_384)

    assertEquals("❌", jniLib.compatibility())
    assertEquals("⚠", jniLib.compatibility(console = true))
  }

  private fun jniLib(align: Long, max: Long) = JniLib(
    name = "libexample.so",
    abi = "arm64-v8a",
    align = align,
    relro = true,
    path = "/tmp/libexample.so",
    max = max
  )
}

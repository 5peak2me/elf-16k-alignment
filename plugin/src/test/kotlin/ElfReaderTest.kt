import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ElfReaderTest {

  @Test
  fun `parses elf64 little endian header`() {
    val info = ElfReader.parseHeader(ElfTestData.elf64())

    assertEquals(
      ElfReader.ElfInfo(
        is64 = true,
        order = ByteOrder.LITTLE_ENDIAN,
        phOff = 64,
        phEntSize = 56,
        phNum = 1
      ),
      info
    )
  }

  @Test
  fun `reads maximum alignment and relro from elf64`() {
    val elf = ElfTestData.withRelro(
      alignments = listOf(4_096L, 65_536L, 16_384L)
    )

    assertEquals(65_536L, ElfReader.maxPAlign(elf))
    assertTrue(ElfReader.hasGnuRelro(elf))
  }

  @Test
  fun `reads maximum alignment and relro from big endian elf32`() {
    val elf = ElfTestData.withRelro(
      is64 = false,
      order = ByteOrder.BIG_ENDIAN,
      alignments = listOf(4_096L, 16_384L)
    )

    assertEquals(16_384L, ElfReader.maxPAlign(elf))
    assertTrue(ElfReader.hasGnuRelro(elf))
  }

  @Test
  fun `reports no relro when segment is absent`() {
    assertFalse(ElfReader.hasGnuRelro(ElfTestData.elf64()))
  }

  @Test
  fun `handles invalid and truncated input`() {
    val invalid = "not an elf".encodeToByteArray()
    val truncated = ElfTestData.elf64().copyOf(64)

    assertNull(ElfReader.parseHeader(invalid))
    assertEquals(0L, ElfReader.maxPAlign(invalid))
    assertFalse(ElfReader.hasGnuRelro(invalid))
    assertEquals(0L, ElfReader.maxPAlign(truncated))
    assertFalse(ElfReader.hasGnuRelro(truncated))
  }
}

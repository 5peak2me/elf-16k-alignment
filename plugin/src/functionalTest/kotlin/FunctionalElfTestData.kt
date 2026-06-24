import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object FunctionalElfTestData {
  const val PT_GNU_RELRO = 0x6474e552

  fun elf64(
    type: Int = 1,
    align: Long = 16_384L
  ): ByteArray {
    val headerSize = 64
    val entrySize = 56
    val bytes = ByteArray(headerSize + entrySize)
    bytes[0] = 0x7f
    bytes[1] = 'E'.code.toByte()
    bytes[2] = 'L'.code.toByte()
    bytes[3] = 'F'.code.toByte()
    bytes[4] = 2
    bytes[5] = 1

    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).apply {
      putLong(32, headerSize.toLong())
      putShort(54, entrySize.toShort())
      putShort(56, 1)
      putInt(headerSize, type)
      putLong(headerSize + 48, align)
    }
    return bytes
  }
}

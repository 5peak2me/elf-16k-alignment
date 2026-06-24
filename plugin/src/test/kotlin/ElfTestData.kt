import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object ElfTestData {
  private const val PT_LOAD = 1
  private const val PT_GNU_RELRO = 0x6474e552

  fun elf64(
    order: ByteOrder = ByteOrder.LITTLE_ENDIAN,
    headers: List<Pair<Int, Long>> = listOf(PT_LOAD to 16_384L)
  ): ByteArray = elf(
    is64 = true,
    order = order,
    headers = headers
  )

  fun elf32(
    order: ByteOrder = ByteOrder.LITTLE_ENDIAN,
    headers: List<Pair<Int, Long>> = listOf(PT_LOAD to 16_384L)
  ): ByteArray = elf(
    is64 = false,
    order = order,
    headers = headers
  )

  fun withRelro(
    is64: Boolean = true,
    order: ByteOrder = ByteOrder.LITTLE_ENDIAN,
    alignments: List<Long> = listOf(4_096L, 16_384L)
  ): ByteArray {
    val headers = alignments.mapIndexed { index, align ->
      (if (index == alignments.lastIndex) PT_GNU_RELRO else PT_LOAD) to align
    }
    return elf(is64, order, headers)
  }

  private fun elf(
    is64: Boolean,
    order: ByteOrder,
    headers: List<Pair<Int, Long>>
  ): ByteArray {
    val headerSize = if (is64) 64 else 52
    val entrySize = if (is64) 56 else 32
    val bytes = ByteArray(headerSize + entrySize * headers.size)
    bytes[0] = 0x7f
    bytes[1] = 'E'.code.toByte()
    bytes[2] = 'L'.code.toByte()
    bytes[3] = 'F'.code.toByte()
    bytes[4] = if (is64) 2 else 1
    bytes[5] = if (order == ByteOrder.LITTLE_ENDIAN) 1 else 2

    val buffer = ByteBuffer.wrap(bytes).order(order)
    if (is64) {
      buffer.putLong(32, headerSize.toLong())
      buffer.putShort(54, entrySize.toShort())
      buffer.putShort(56, headers.size.toShort())
    } else {
      buffer.putInt(28, headerSize)
      buffer.putShort(42, entrySize.toShort())
      buffer.putShort(44, headers.size.toShort())
    }

    headers.forEachIndexed { index, (type, align) ->
      val offset = headerSize + index * entrySize
      buffer.putInt(offset, type)
      if (is64) {
        buffer.putLong(offset + 48, align)
      } else {
        buffer.putInt(offset + 28, align.toInt())
      }
    }
    return bytes
  }
}

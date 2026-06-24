package internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StringTest {

  @Test
  fun `maven creates central url for standard coordinates`() {
    assertEquals(
      "https://central.sonatype.com/artifact/com.example/library/1.0.0",
      "com.example:library:1.0.0".maven()
    )
  }

  @Test
  fun `maven creates google url for google coordinates`() {
    listOf(
      "androidx.core:core-ktx:1.0.0",
      "com.google.android.material:material:1.0.0",
      "com.android.tools.build:gradle:9.0.0"
    ).forEach { coordinate ->
      assertEquals(
        "https://maven.google.com/web/index.html#$coordinate",
        coordinate.maven()
      )
    }
  }

  @Test
  fun `maven creates jitpack url without version`() {
    listOf(
      "com.github.user:library:1.0.0" to "https://jitpack.io/#com.github.user/library",
      "io.github.user:library:1.0.0" to "https://jitpack.io/#io.github.user/library"
    ).forEach { (coordinate, expected) ->
      assertEquals(expected, coordinate.maven())
    }
  }

  @Test
  fun `maven returns null for non-standard coordinates`() {
    listOf(
      "",
      "local-library.aar",
      "com.example:library",
      "com.example::1.0.0",
      "com.example:library:1.0.0:debug",
      " com.example:library:1.0.0",
      "com.example:library:1.0.0 ",
      "com.example:lib rary:1.0.0",
      "com.example:library:\t1.0.0"
    ).forEach { coordinate ->
      assertNull(coordinate.maven())
    }
  }
}

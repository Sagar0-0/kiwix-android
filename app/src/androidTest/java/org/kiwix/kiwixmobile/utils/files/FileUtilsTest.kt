/*
 * Kiwix Android
 * Copyright (c) 2022 Kiwix <android.kiwix.org>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.kiwix.kiwixmobile.utils.files

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.kiwix.kiwixmobile.BaseActivityTest
import org.kiwix.kiwixmobile.core.CoreApp
import org.kiwix.kiwixmobile.core.entity.LibraryNetworkEntity.Book
import org.kiwix.kiwixmobile.core.utils.SharedPreferenceUtil
import org.kiwix.kiwixmobile.core.utils.files.FileUtils
import java.io.File

class FileUtilsTest : BaseActivityTest() {

  private val mockFile: File = mockk()
  private val testBook = Book().apply { file = mockFile }
  private val testId = "8ce5775a-10a9-bbf3-178a-9df69f23263c"
  private val fileName = "/data/user/0/org.kiwix.kiwixmobile/files${File.separator}$testId"

  @Before
  override fun waitForIdle() {
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).waitForIdle()
    PreferenceManager.getDefaultSharedPreferences(context).edit {
      putBoolean(SharedPreferenceUtil.PREF_SHOW_INTRO, false)
      putBoolean(SharedPreferenceUtil.PREF_WIFI_ONLY, false)
    }
  }

  @BeforeEach
  fun init() {
    clearMocks(mockFile)
  }

  @Test
  fun fileNameEndsWithZimAndFileDoesNotExistAtTheLocation() {
    testWith(".zim", false)
  }

  @Test
  fun fileNameEndsWithZimAndFileExistsAtTheLocation() {
    testWith(".zim", true)
  }

  @Test
  fun fileNameEndsWithZimPartAndFileDoesNotExistAtTheLocation() {
    testWith(".zim.part", false)
  }

  @Test
  fun fileNameEndsWithZimPartAndFileExistsAtTheLocation() {
    testWith(".zim.part", true)
  }

  @Test
  fun fileNameEndsWithZimAndNoSuchFileExistsAtAnySuchLocation() {
    expect("zimab", false)
    assertEquals(
      FileUtils.getAllZimParts(testBook).size,
      0,
      "Nothing is returned in this case"
    )
  }

  private fun testWith(extension: String, fileExists: Boolean) {
    expect(extension, fileExists)
    val coreApp = mockk<CoreApp>()
    CoreApp.instance = coreApp
    every { coreApp.packageName } returns "mock_package"
    val files = FileUtils.getAllZimParts(testBook)
    assertEquals(
      files.size,
      1,
      "Only a single book is returned in case the file has extension $extension"
    )
    if (fileExists) {
      assertEquals(
        testBook.file,
        files[0],
        "The filename retained as such"
      )
    } else {
      assertEquals(
        testBook.file.toString() + ".part",
        files[0].path,
        "The filename is appended with .part"
      )
    }
  }

  private fun expect(extension: String, fileExists: Boolean) {
    every { mockFile.path } returns "$fileName$extension"
    every { mockFile.exists() } returns fileExists
  }

  @Test
  fun testDecodeFileName() {
    val fileName =
      FileUtils.getDecodedFileName(
        url = "https://kiwix.org/contributors/contributors_list.pdf",
        src = null
      )
    assertEquals(fileName, "contributors_list.pdf")
  }

  @Test
  fun testFileNameIfExtensionDoesNotExist() {
    val fileName = FileUtils.getDecodedFileName(url = "https://kiwix.org/contributors/", src = null)
    assertEquals(fileName, "")
  }

  @Test
  fun testFileNameIfTheUrlAndSrcDoesNotExist() {
    val fileName = FileUtils.getDecodedFileName(url = null, src = null)
    assertEquals(fileName, "")
  }

  @Test
  fun testFileNameIfOnlyFileNameExist() {
    val fileName = FileUtils.getDecodedFileName(src = "android_tutorials.pdf", url = null)
    assertEquals(fileName, "")
  }

  @Test
  fun testFileNameIfUrlDoesNotExist() {
    val fileName = FileUtils.getDecodedFileName(url = null, src = "/html/images/test.png")
    assertEquals(fileName, "test.png")
  }

  @Test
  fun testFileNameIfUrlAndSrcExtensionDoesNotExist() {
    val fileName = FileUtils.getDecodedFileName(url = null, src = "/html/images/")
    assertEquals(fileName, "")
  }
}

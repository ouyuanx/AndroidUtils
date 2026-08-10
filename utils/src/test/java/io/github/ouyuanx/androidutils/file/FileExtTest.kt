package io.github.ouyuanx.androidutils.file

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import kotlin.io.FileAlreadyExistsException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FileExtTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `ensureDirectory creates nested directories`() {
        val directory = File(temporaryFolder.root, "parent/child")

        assertSame(directory, directory.ensureDirectory())
        assertTrue(directory.isDirectory)
    }

    @Test(expected = IOException::class)
    fun `ensureDirectory rejects an existing file`() {
        temporaryFolder.newFile("occupied").ensureDirectory()
    }

    @Test
    fun `createFile creates parents and an empty file`() {
        val file = File(temporaryFolder.root, "parent/created.txt")

        assertSame(file, file.createFile())
        assertTrue(file.isFile)
        assertEquals(0L, file.length())
    }

    @Test
    fun `createFile overwrites and truncates an existing file`() {
        val file = temporaryFolder.newFile("existing.txt").apply { writeText("content") }

        file.createFile(overwrite = true)

        assertEquals(0L, file.length())
    }

    @Test(expected = FileAlreadyExistsException::class)
    fun `createFile refuses to overwrite by default`() {
        temporaryFolder.newFile("existing.txt").createFile()
    }

    @Test
    fun `copyToWithParents copies a regular file`() {
        val source = temporaryFolder.newFile("copy-source.txt").apply { writeText("copy") }
        val target = File(temporaryFolder.root, "copy/target.txt")

        assertEquals(target, source.copyToWithParents(target))
        assertEquals("copy", target.readText())
        assertEquals("copy", source.readText())
    }

    @Test
    fun `moveTo moves a regular file`() {
        val source = temporaryFolder.newFile("move-source.txt").apply { writeText("move") }
        val target = File(temporaryFolder.root, "move/target.txt")

        assertEquals(target, source.moveTo(target))
        assertFalse(source.exists())
        assertEquals("move", target.readText())
    }

    @Test
    fun `rename changes only the file name`() {
        val source = temporaryFolder.newFile("before.txt").apply { writeText("rename") }

        val target = source.rename("after.txt")

        assertEquals(File(temporaryFolder.root, "after.txt"), target)
        assertFalse(source.exists())
        assertEquals("rename", target.readText())
    }

    @Test
    fun `deleteIfExists reports whether a file was deleted`() {
        val file = temporaryFolder.newFile("delete.txt")

        assertTrue(file.deleteIfExists())
        assertFalse(file.deleteIfExists())
    }

    @Test
    fun `sha256 returns lowercase checksum`() {
        val file = temporaryFolder.newFile("message.txt").apply {
            writeText("hello")
        }

        assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            file.sha256(),
        )
    }

    @Test
    fun `formattedSize uses binary units`() {
        val file = temporaryFolder.newFile("sized.bin").apply {
            writeBytes(ByteArray(1536))
        }

        assertEquals("1.5 KiB", file.formattedSize())
    }

    @Test
    fun `copyToFile copies uri content and returns byte count`() {
        val source = temporaryFolder.newFile("source.txt").apply {
            writeText("AndroidUtils")
        }
        val target = File(temporaryFolder.root, "nested/target.txt")

        val count = contentResolver().copyToFile(Uri.fromFile(source), target)

        assertEquals(source.length(), count)
        assertEquals("AndroidUtils", target.readText())
    }

    @Test
    fun `copyToFile refuses to overwrite by default`() {
        val source = temporaryFolder.newFile("source.txt").apply { writeText("new") }
        val target = temporaryFolder.newFile("target.txt").apply { writeText("old") }

        try {
            contentResolver().copyToFile(Uri.fromFile(source), target)
            throw AssertionError("应抛出 FileAlreadyExistsException")
        } catch (_: FileAlreadyExistsException) {
            assertEquals("old", target.readText())
        }
    }

    @Test
    fun `copyToFile overwrites when requested`() {
        val source = temporaryFolder.newFile("source.txt").apply { writeText("new") }
        val target = temporaryFolder.newFile("target.txt").apply { writeText("old") }

        contentResolver().copyToFile(Uri.fromFile(source), target, overwrite = true)

        assertEquals("new", target.readText())
    }

    @Test
    fun `copyToFile does not leave a target when source cannot be opened`() {
        val target = File(temporaryFolder.root, "missing-target.txt")

        try {
            contentResolver().copyToFile(Uri.parse("file:///definitely/missing/source.txt"), target)
            throw AssertionError("应抛出 IOException")
        } catch (_: IOException) {
            assertFalse(target.exists())
        }
    }

    @Test
    fun `copyToUri writes file content`() {
        val source = temporaryFolder.newFile("uri-source.txt").apply { writeText("to uri") }
        val target = temporaryFolder.newFile("uri-target.txt")

        val count = source.copyToUri(contentResolver(), Uri.fromFile(target))

        assertEquals(source.length(), count)
        assertEquals("to uri", target.readText())
    }

    private fun contentResolver() =
        (RuntimeEnvironment.getApplication() as Context).contentResolver
}

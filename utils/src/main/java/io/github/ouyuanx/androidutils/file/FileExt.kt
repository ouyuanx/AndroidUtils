@file:JvmName("FileUtils")

package io.github.ouyuanx.androidutils.file

import android.content.ContentResolver
import android.net.Uri
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.security.MessageDigest
import java.util.Locale
import kotlin.io.FileAlreadyExistsException

/**
 * 确保当前 [File] 是一个目录，并返回自身。
 *
 * 目录不存在时会连同父目录一起创建；路径已被普通文件占用或创建失败时抛出 [IOException]。
 */
@Throws(IOException::class)
fun File.ensureDirectory(): File {
    if (isDirectory) return this
    if (exists()) throw IOException("路径已存在且不是目录：$absolutePath")
    if (!mkdirs() && !isDirectory) throw IOException("无法创建目录：$absolutePath")
    return this
}

/**
 * 创建一个空文件，并自动创建父目录。
 *
 * 文件已存在时默认抛出 [FileAlreadyExistsException]；[overwrite] 为 `true` 时会清空已有文件。
 */
@JvmOverloads
@Throws(IOException::class)
fun File.createFile(overwrite: Boolean = false): File {
    absoluteFile.parentFile?.ensureDirectory()

    if (exists()) {
        if (!isFile) throw IOException("路径已存在且不是普通文件：$absolutePath")
        if (!overwrite) throw FileAlreadyExistsException(this)
        outputStream().use { }
        return this
    }

    if (!createNewFile() && !isFile) throw IOException("无法创建文件：$absolutePath")
    return this
}

/**
 * 复制当前文件到 [target]，并自动创建目标父目录。
 *
 * 本方法只处理普通文件，不递归复制目录。
 */
@JvmOverloads
@Throws(IOException::class)
fun File.copyToWithParents(target: File, overwrite: Boolean = false): File {
    requireRegularFile()
    if (canonicalFile == target.canonicalFile) {
        throw IOException("源文件和目标文件不能相同：$absolutePath")
    }

    val targetExisted = target.prepareForWrite(overwrite)
    return try {
        inputStream().buffered().use { source ->
            target.outputStream().buffered().use(source::copyTo)
        }
        target
    } catch (error: Exception) {
        if (!targetExisted) target.delete()
        throw error
    }
}

/**
 * 移动当前文件到 [target]，并自动创建目标父目录。
 *
 * 优先使用同文件系统重命名；失败时回退到复制后删除源文件。本方法不移动目录。
 */
@JvmOverloads
@Throws(IOException::class)
fun File.moveTo(target: File, overwrite: Boolean = false): File {
    requireRegularFile()
    if (canonicalFile == target.canonicalFile) return target

    val targetExisted = target.prepareForWrite(overwrite)
    if (!targetExisted && renameTo(target)) return target

    copyToWithParents(target, overwrite)
    if (!delete()) {
        if (!targetExisted) target.delete()
        throw IOException("已复制文件，但无法删除源文件：$absolutePath")
    }
    return target
}

/** 在原目录中将当前文件重命名为 [newName]。 */
@JvmOverloads
@Throws(IOException::class)
fun File.rename(newName: String, overwrite: Boolean = false): File {
    require(
        newName.isNotBlank() &&
            newName != "." &&
            newName != ".." &&
            '/' !in newName &&
            '\\' !in newName,
    ) { "newName 必须是不带路径分隔符的文件名" }

    val parent = absoluteFile.parentFile
        ?: throw IOException("无法获取文件的父目录：$absolutePath")
    return moveTo(File(parent, newName), overwrite)
}

/** 删除普通文件或空目录；路径不存在时返回 `false`。 */
@Throws(IOException::class)
fun File.deleteIfExists(): Boolean {
    if (!exists()) return false
    if (!delete()) throw IOException("无法删除文件或空目录：$absolutePath")
    return true
}

/** 计算文件内容的 SHA-256，返回小写、不带分隔符的十六进制字符串。 */
@Throws(IOException::class)
fun File.sha256(): String {
    requireRegularFile()

    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().buffered().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (count > 0) digest.update(buffer, 0, count)
        }
    }
    return digest.digest().toLowerHexString()
}

/** 以 B、KiB、MiB、GiB 或 TiB 格式返回普通文件的大小。 */
@Throws(IOException::class)
fun File.formattedSize(): String {
    requireRegularFile()
    val bytes = length()
    if (bytes < BYTES_PER_UNIT) return "$bytes B"

    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= BYTES_PER_UNIT && unitIndex < FILE_SIZE_UNITS.lastIndex) {
        value /= BYTES_PER_UNIT
        unitIndex++
    }
    return String.format(Locale.ROOT, "%.1f %s", value, FILE_SIZE_UNITS[unitIndex])
}

/**
 * 将 [uri] 的内容复制到 [target]，返回写入的字节数。
 *
 * 会自动创建目标文件的父目录。默认不覆盖已有文件；新文件复制失败时会尝试删除未完整的目标文件。
 */
@JvmOverloads
@Throws(IOException::class)
fun ContentResolver.copyToFile(
    uri: Uri,
    target: File,
    overwrite: Boolean = false,
): Long {
    val targetExisted = target.prepareForWrite(overwrite)

    val input = openInputStream(uri)
        ?: throw FileNotFoundException("无法打开 Uri：$uri")

    return try {
        input.use { source ->
            target.outputStream().buffered().use(source::copyTo)
        }
    } catch (error: Exception) {
        if (!targetExisted) target.delete()
        throw error
    }
}

/** 将当前文件写入 [uri]，返回写入的字节数。 */
@Throws(IOException::class)
fun File.copyToUri(contentResolver: ContentResolver, uri: Uri): Long {
    requireRegularFile()
    val output = contentResolver.openOutputStream(uri, "w")
        ?: throw FileNotFoundException("无法打开 Uri：$uri")

    return inputStream().buffered().use { source ->
        output.buffered().use(source::copyTo)
    }
}

@Throws(IOException::class)
private fun File.requireRegularFile() {
    if (!isFile) throw FileNotFoundException("文件不存在或不是普通文件：$absolutePath")
}

@Throws(IOException::class)
private fun File.prepareForWrite(overwrite: Boolean): Boolean {
    val existed = exists()
    if (existed && !overwrite) throw FileAlreadyExistsException(this)
    if (existed && !isFile) throw IOException("目标路径不是普通文件：$absolutePath")
    absoluteFile.parentFile?.ensureDirectory()
    return existed
}

private fun ByteArray.toLowerHexString(): String = buildString(size * 2) {
    for (byte in this@toLowerHexString) {
        val value = byte.toInt() and 0xFF
        append(LOWER_HEX_DIGITS[value ushr 4])
        append(LOWER_HEX_DIGITS[value and 0x0F])
    }
}

private const val LOWER_HEX_DIGITS = "0123456789abcdef"
private const val BYTES_PER_UNIT = 1024.0
private val FILE_SIZE_UNITS = arrayOf("B", "KiB", "MiB", "GiB", "TiB")

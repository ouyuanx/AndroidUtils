package io.github.ouyuanx.androidutils.uri

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

/** 获取 Content Uri 对应的显示名称；Provider 未提供名称时返回 `null`。 */
public fun ContentResolver.displayName(uri: Uri): String? =
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && !cursor.isNull(index)) cursor.getString(index) else null
    }

/** 获取 Content Uri 对应的字节数；Provider 未提供大小时返回 `null`。 */
public fun ContentResolver.contentSize(uri: Uri): Long? =
    query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        val index = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (index >= 0 && !cursor.isNull(index)) cursor.getLong(index) else null
    }

/** 获取 Content Uri 的 MIME 类型。 */
public fun ContentResolver.mimeType(uri: Uri): String? = getType(uri)

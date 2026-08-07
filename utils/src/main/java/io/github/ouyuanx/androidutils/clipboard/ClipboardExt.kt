package io.github.ouyuanx.androidutils.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/** 将 [text] 作为纯文本复制到系统剪贴板。 */
public fun Context.copyText(
    text: CharSequence,
    label: CharSequence = packageName,
) {
    val clipboard = requireNotNull(getSystemService(ClipboardManager::class.java)) {
        "Clipboard service is unavailable."
    }
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

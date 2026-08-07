package io.github.ouyuanx.androidutils.intent

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.CheckResult
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.core.os.BundleCompat

/**
 * 安全启动 [intent]。
 *
 * 非 Activity Context 会自动添加 [Intent.FLAG_ACTIVITY_NEW_TASK]。没有可处理该 Intent
 * 的应用或调用被系统安全策略拒绝时返回 `false`。
 */
@CheckResult
public fun Context.startActivitySafely(intent: Intent): Boolean {
    val launchIntent = Intent(intent).apply {
        if (this@startActivitySafely !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    return try {
        startActivity(launchIntent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}

/** 使用系统浏览器打开 HTTP 或 HTTPS 地址。地址无效或无法处理时返回 `false`。 */
@CheckResult
public fun Context.openUrl(url: String): Boolean {
    val uri = url.toUri()
    if (uri.scheme?.lowercase() !in setOf("http", "https")) return false

    return startActivitySafely(Intent(Intent.ACTION_VIEW, uri))
}

/** 调用系统分享面板分享纯文本。无法打开分享面板时返回 `false`。 */
@CheckResult
public fun Context.shareText(
    text: CharSequence,
    chooserTitle: CharSequence? = null,
): Boolean {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    return startActivitySafely(Intent.createChooser(sendIntent, chooserTitle))
}

/** 兼容不同 Android 版本读取 Parcelable Extra。 */
public fun <T : Parcelable> Intent.parcelableExtra(name: String, clazz: Class<T>): T? =
    IntentCompat.getParcelableExtra(this, name, clazz)

/** 使用 reified 泛型读取 Parcelable Extra。 */
public inline fun <reified T : Parcelable> Intent.parcelableExtra(name: String): T? =
    parcelableExtra(name, T::class.java)

/** 兼容不同 Android 版本读取 Bundle 中的 Parcelable。 */
public fun <T : Parcelable> Bundle.parcelable(name: String, clazz: Class<T>): T? =
    BundleCompat.getParcelable(this, name, clazz)

/** 使用 reified 泛型读取 Bundle 中的 Parcelable。 */
public inline fun <reified T : Parcelable> Bundle.parcelable(name: String): T? =
    parcelable(name, T::class.java)

/** 将 [Uri] 包装为浏览 Intent，便于需要自行补充 flags 或 extras 的调用方使用。 */
public fun Uri.toViewIntent(): Intent = Intent(Intent.ACTION_VIEW, this)

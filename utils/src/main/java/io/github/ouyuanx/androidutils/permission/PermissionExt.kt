package io.github.ouyuanx.androidutils.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.annotation.CheckResult
import androidx.core.content.ContextCompat
import io.github.ouyuanx.androidutils.intent.startActivitySafely

/** 判断当前应用是否已经获得指定权限。 */
public fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/** 打开当前应用的系统详情设置页。无法打开时返回 `false`。 */
@CheckResult
public fun Context.openAppSettings(): Boolean {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    )
    return startActivitySafely(intent)
}

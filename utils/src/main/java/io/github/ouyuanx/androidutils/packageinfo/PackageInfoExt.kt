package io.github.ouyuanx.androidutils.packageinfo

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

/** 获取当前应用的 versionName；系统无法读取包信息时返回 `null`。 */
public fun Context.appVersionName(): String? = ownPackageInfo()?.versionName

/** 获取当前应用的长整型 versionCode；系统无法读取包信息时返回 `null`。 */
public fun Context.appVersionCode(): Long? =
    ownPackageInfo()?.let(PackageInfoCompat::getLongVersionCode)

private fun Context.ownPackageInfo(): PackageInfo? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }
} catch (_: PackageManager.NameNotFoundException) {
    null
}

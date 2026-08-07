@file:JvmName("PackageUtils")

package io.github.ouyuanx.androidutils.packageinfo

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.annotation.CheckResult
import androidx.core.content.pm.PackageInfoCompat
import io.github.ouyuanx.androidutils.intent.startActivitySafely
import java.security.MessageDigest

/** 当前应用签名证书支持的摘要算法。 */
public enum class SignatureDigestAlgorithm(
    internal val jcaName: String,
) {
    /** 仅建议用于兼容需要 MD5 指纹的第三方平台。 */
    MD5("MD5"),

    /** 仅建议用于兼容需要 SHA-1 指纹的第三方平台。 */
    SHA1("SHA-1"),

    /** 推荐用于签名证书指纹校验。 */
    SHA256("SHA-256"),
}

/** 获取当前应用向用户展示的名称。 */
public fun Context.appName(): CharSequence = applicationInfo.loadLabel(packageManager)

/** 获取当前应用的 versionName；系统无法读取包信息时返回 `null`。 */
public fun Context.appVersionName(): String? = ownPackageInfo()?.versionName

/** 获取当前应用的长整型 versionCode；系统无法读取包信息时返回 `null`。 */
public fun Context.appVersionCode(): Long? =
    ownPackageInfo()?.let(PackageInfoCompat::getLongVersionCode)

/**
 * 判断 [targetPackageName] 对应的应用是否已安装且对当前应用可见。
 *
 * Android 11（API 30）及以上版本会过滤其他应用信息。查询普通第三方应用前，使用方可能需要
 * 在自身 AndroidManifest.xml 的 `<queries>` 中声明目标包名。
 */
@CheckResult
public fun Context.isPackageInstalled(targetPackageName: String): Boolean {
    if (targetPackageName.isBlank()) return false

    return try {
        packageManager.packageInfo(targetPackageName)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}

/** 打开 [targetPackageName] 对应应用的启动页；应用不可见、未安装或无法启动时返回 `false`。 */
@CheckResult
public fun Context.openApp(targetPackageName: String): Boolean {
    if (targetPackageName.isBlank()) return false

    val launchIntent = packageManager.getLaunchIntentForPackage(targetPackageName) ?: return false
    return startActivitySafely(launchIntent)
}

/**
 * 获取当前应用签名证书的全部指纹。
 *
 * 默认只读取当前 APK 的签名证书。将 [includeHistory] 设为 `true` 后，使用单签名证书轮换的
 * 应用还会包含历史证书；多签名应用始终返回当前的全部签名证书。返回值使用大写十六进制并以
 * 冒号分隔，例如 `12:AB:34:CD`。
 */
@CheckResult
@JvmOverloads
public fun Context.appSignatureFingerprints(
    algorithm: SignatureDigestAlgorithm = SignatureDigestAlgorithm.SHA256,
    includeHistory: Boolean = false,
): List<String> = signingCertificates(includeHistory)
    .map { certificate -> certificate.toByteArray().signatureFingerprint(algorithm) }
    .distinct()

/** 获取当前应用第一个签名证书的 MD5 指纹；仅建议用于兼容旧平台。 */
public fun Context.appSignatureMd5(): String? =
    appSignatureFingerprints(SignatureDigestAlgorithm.MD5).firstOrNull()

/** 获取当前应用第一个签名证书的 SHA-1 指纹；仅建议用于兼容旧平台。 */
public fun Context.appSignatureSha1(): String? =
    appSignatureFingerprints(SignatureDigestAlgorithm.SHA1).firstOrNull()

/** 获取当前应用第一个签名证书的 SHA-256 指纹。 */
public fun Context.appSignatureSha256(): String? =
    appSignatureFingerprints(SignatureDigestAlgorithm.SHA256).firstOrNull()

private fun Context.ownPackageInfo(flags: Long = 0): PackageInfo? = try {
    packageManager.packageInfo(packageName, flags)
} catch (_: PackageManager.NameNotFoundException) {
    null
}

private fun PackageManager.packageInfo(packageName: String, flags: Long = 0): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags))
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo(packageName, flags.toInt())
    }

private fun Context.signingCertificates(includeHistory: Boolean): List<Signature> {
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        PackageManager.GET_SIGNING_CERTIFICATES.toLong()
    } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_SIGNATURES.toLong()
    }
    val packageInfo = ownPackageInfo(flags) ?: return emptyList()

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        @Suppress("DEPRECATION")
        return packageInfo.signatures?.toList().orEmpty()
    }

    val signingInfo = packageInfo.signingInfo ?: return emptyList()
    val signatures = if (includeHistory && !signingInfo.hasMultipleSigners()) {
        signingInfo.signingCertificateHistory
    } else {
        signingInfo.apkContentsSigners
    }
    return signatures?.toList().orEmpty()
}

internal fun ByteArray.signatureFingerprint(algorithm: SignatureDigestAlgorithm): String {
    val digest = MessageDigest.getInstance(algorithm.jcaName).digest(this)
    return buildString(digest.size * 3 - 1) {
        digest.forEachIndexed { index, byte ->
            val value = byte.toInt() and 0xFF
            if (index > 0) append(':')
            append(HEX_DIGITS[value ushr 4])
            append(HEX_DIGITS[value and 0x0F])
        }
    }
}

private val HEX_DIGITS: CharArray = "0123456789ABCDEF".toCharArray()

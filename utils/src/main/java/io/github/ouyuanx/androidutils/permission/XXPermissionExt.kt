@file:JvmName("PermissionUtils")

package io.github.ouyuanx.androidutils.permission

import android.content.Context
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.base.IPermission
import io.github.ouyuanx.androidutils.activity.findActivity

/**
 * 一次 XXPermissions 请求的完整结果。
 *
 * [grantedPermissions] 和 [deniedPermissions] 分别保存本次已授予和未授予的权限。
 * [doNotAskAgain] 表示至少有一个被拒权限已不适合再次直接弹出系统授权框。
 */
data class PermissionRequestResult(
    val grantedPermissions: List<IPermission>,
    val deniedPermissions: List<IPermission>,
    val doNotAskAgain: Boolean,
) {
    /** 本次请求中的权限是否已全部授予。 */
    val allGranted: Boolean
        get() = deniedPermissions.isEmpty()
}

/**
 * 使用 XXPermissions 请求 [permissions]。
 *
 * 当前 [Context] 无法找到 Activity 时不会发起请求，也不会执行 [onResult]。
 * 本方法不显示 Toast，也不会自动跳转设置页，界面反馈由调用方根据 [PermissionRequestResult]
 * 决定。
 */
fun Context.requestPermissions(
    permissions: List<IPermission>,
    onResult: (PermissionRequestResult) -> Unit,
) {
    require(permissions.isNotEmpty()) { "permissions 不能为空" }
    val activity = findActivity() ?: return

    XXPermissions.with(activity)
        .permissions(permissions)
        .request { grantedList, deniedList ->
            onResult(
                PermissionRequestResult(
                    grantedPermissions = grantedList.toList(),
                    deniedPermissions = deniedList.toList(),
                    doNotAskAgain = deniedList.isNotEmpty() &&
                            XXPermissions.isDoNotAskAgainPermissions(activity, deniedList),
                ),
            )
        }
}

/** 使用可变参数请求一个或多个 XXPermissions 权限对象。 */
fun Context.requestPermissions(
    vararg permissions: IPermission,
    onResult: (PermissionRequestResult) -> Unit,
) {
    requestPermissions(permissions.toList(), onResult)
}

/** 判断一个 XXPermissions 权限对象是否已授予。 */
fun Context.hasPermission(permission: IPermission): Boolean =
    XXPermissions.isGrantedPermission(this, permission)

/** 判断 [permissions] 中的权限是否已全部授予。 */
fun Context.hasPermissions(permissions: List<IPermission>): Boolean {
    require(permissions.isNotEmpty()) { "permissions 不能为空" }
    return XXPermissions.isGrantedPermissions(this, permissions)
}

/** 判断可变参数中的权限是否已全部授予。 */
fun Context.hasPermissions(vararg permissions: IPermission): Boolean =
    hasPermissions(permissions.toList())

/**
 * 打开权限设置页。
 *
 * [permissions] 为空时打开应用权限设置页；不为空时由 XXPermissions 尽量跳转到对应权限页面。
 */
@JvmOverloads
fun Context.openPermissionSettings(permissions: List<IPermission> = emptyList()) {
    if (permissions.isEmpty()) {
        XXPermissions.startPermissionActivity(this)
    } else {
        XXPermissions.startPermissionActivity(this, permissions)
    }
}

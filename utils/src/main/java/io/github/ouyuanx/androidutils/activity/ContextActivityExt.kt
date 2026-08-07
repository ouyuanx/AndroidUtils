package io.github.ouyuanx.androidutils.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * 从当前 [Context] 及其 [ContextWrapper] 链中查找 [Activity]。
 *
 * 找不到时返回 `null`，不会保存 Context 引用，也不会造成 Activity 泄漏。
 */
public fun Context.findActivity(): Activity? {
    var current: Context = this

    while (current is ContextWrapper) {
        if (current is Activity) return current

        val baseContext = current.baseContext
        if (baseContext === current) return null
        current = baseContext
    }

    return current as? Activity
}

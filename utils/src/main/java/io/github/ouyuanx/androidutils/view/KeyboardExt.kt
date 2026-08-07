package io.github.ouyuanx.androidutils.view

import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import io.github.ouyuanx.androidutils.activity.findActivity

/** 请求焦点并显示软键盘。View 尚未附着到窗口时可能不会立即显示。 */
public fun View.showKeyboard() {
    requestFocus()
    post {
        val window = context.findActivity()?.window ?: return@post
        WindowCompat.getInsetsController(window, this).show(WindowInsetsCompat.Type.ime())
    }
}

/** 隐藏当前 View 所在窗口的软键盘。 */
public fun View.hideKeyboard() {
    val window = context.findActivity()?.window ?: return
    WindowCompat.getInsetsController(window, this).hide(WindowInsetsCompat.Type.ime())
}

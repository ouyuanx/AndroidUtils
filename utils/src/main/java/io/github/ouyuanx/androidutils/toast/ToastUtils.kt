package io.github.ouyuanx.androidutils.toast

import android.app.Application
import androidx.annotation.StringRes
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.hjq.toast.style.BlackToastStyle
import com.hjq.toast.style.CustomToastStyle
import com.hjq.toast.style.WhiteToastStyle
import io.github.ouyuanx.androidutils.R

/** 基于 Toaster 的全局 Toast 入口。 */
object ToastUtils {
    @Volatile
    private var application: Application? = null

    /** 初始化 Toast，建议在 `Application.onCreate` 中调用。 */
    @JvmOverloads
    @Synchronized
    fun init(application: Application, darkMode: Boolean = false) {
        this.application = application
        Toaster.init(application, defaultStyle(darkMode))
    }

    /** 根据当前主题切换默认 Toast 样式。 */
    fun setDarkMode(darkMode: Boolean) {
        checkInitialized()
        Toaster.setStyle(defaultStyle(darkMode))
    }

    fun show(text: CharSequence) {
        checkInitialized()
        Toaster.show(text)
    }

    fun show(@StringRes resId: Int) = show(text(resId))

    fun success(text: CharSequence) = showStyled(text, R.layout.android_utils_toast_success)

    fun success(@StringRes resId: Int) = success(text(resId))

    fun error(text: CharSequence) = showStyled(text, R.layout.android_utils_toast_error)

    fun error(@StringRes resId: Int) = error(text(resId))

    fun warning(text: CharSequence) = showStyled(text, R.layout.android_utils_toast_warning)

    fun warning(@StringRes resId: Int) = warning(text(resId))

    fun showShort(text: CharSequence) {
        checkInitialized()
        Toaster.showShort(text)
    }

    fun showShort(@StringRes resId: Int) = showShort(text(resId))

    fun showLong(text: CharSequence) {
        checkInitialized()
        Toaster.showLong(text)
    }

    fun showLong(@StringRes resId: Int) = showLong(text(resId))

    fun showDelayed(text: CharSequence, delayMillis: Long) {
        require(delayMillis >= 0) { "delayMillis 不能小于 0" }
        checkInitialized()
        Toaster.delayedShow(text, delayMillis)
    }

    fun showDelayed(@StringRes resId: Int, delayMillis: Long) =
        showDelayed(text(resId), delayMillis)

    fun cancel() {
        checkInitialized()
        Toaster.cancel()
    }

    private fun showStyled(text: CharSequence, layoutRes: Int) {
        checkInitialized()
        Toaster.show(
            ToastParams().apply {
                this.text = text
                style = CustomToastStyle(layoutRes)
            },
        )
    }

    private fun text(@StringRes resId: Int): CharSequence =
        requireNotNull(application) { "ToastUtils 尚未初始化，请先调用 init(application)" }
            .getText(resId)

    private fun checkInitialized() {
        check(application != null && Toaster.isInit()) {
            "ToastUtils 尚未初始化，请先调用 init(application)"
        }
    }

    private fun defaultStyle(darkMode: Boolean) =
        if (darkMode) WhiteToastStyle() else BlackToastStyle()
}

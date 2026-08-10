package io.github.ouyuanx.androidutils.log

import timber.log.Timber

/** 基于 Timber 的统一日志入口。 */
object LogUtils {
    private var debugTree: Timber.Tree? = null

    /**
     * 配置调试日志。
     *
     * 开启时植入一个 [Timber.DebugTree]；关闭时只移除本工具植入的调试树，不影响调用方的自定义树。
     */
    @Synchronized
    fun init(isDebug: Boolean) {
        debugTree?.let(Timber::uproot)
        debugTree = null

        if (isDebug) {
            Timber.DebugTree().also { tree ->
                Timber.plant(tree)
                debugTree = tree
            }
        }
    }

    fun v(message: String, vararg args: Any?) {
        Timber.v(message, *args)
    }

    fun v(throwable: Throwable, message: String, vararg args: Any?) {
        Timber.v(throwable, message, *args)
    }

    fun d(message: String, vararg args: Any?) {
        Timber.d(message, *args)
    }

    fun d(throwable: Throwable, message: String, vararg args: Any?) {
        Timber.d(throwable, message, *args)
    }

    fun i(message: String, vararg args: Any?) {
        Timber.i(message, *args)
    }

    fun i(throwable: Throwable, message: String, vararg args: Any?) {
        Timber.i(throwable, message, *args)
    }

    fun w(message: String, vararg args: Any?) {
        Timber.w(message, *args)
    }

    fun w(throwable: Throwable, message: String, vararg args: Any?) {
        Timber.w(throwable, message, *args)
    }

    fun e(message: String, vararg args: Any?) {
        Timber.e(message, *args)
    }

    fun e(throwable: Throwable, message: String, vararg args: Any?) {
        Timber.e(throwable, message, *args)
    }

    fun wtf(message: String, vararg args: Any?) {
        Timber.wtf(message, *args)
    }

    fun wtf(throwable: Throwable, message: String, vararg args: Any?) {
        Timber.wtf(throwable, message, *args)
    }
}

package com.thondar.utils

import android.app.Application
import android.content.pm.ApplicationInfo
import io.github.ouyuanx.androidutils.log.LogUtils
import io.github.ouyuanx.androidutils.storage.MMKVUtils
import io.github.ouyuanx.androidutils.toast.ToastUtils

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MMKVUtils.init(this)
        ToastUtils.init(this)
        val isDebug = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        LogUtils.init(isDebug)
    }
}

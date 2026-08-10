package com.thondar.utils

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.thondar.utils.ui.theme.AndroidUtilsTheme
import io.github.ouyuanx.androidutils.activity.findActivity
import io.github.ouyuanx.androidutils.clipboard.copyText
import io.github.ouyuanx.androidutils.intent.openUrl
import io.github.ouyuanx.androidutils.intent.shareText
import io.github.ouyuanx.androidutils.log.LogUtils
import io.github.ouyuanx.androidutils.network.NetworkMonitor
import io.github.ouyuanx.androidutils.network.NetworkState
import io.github.ouyuanx.androidutils.packageinfo.appVersionCode
import io.github.ouyuanx.androidutils.packageinfo.appVersionName
import io.github.ouyuanx.androidutils.permission.PermissionRequestResult
import io.github.ouyuanx.androidutils.permission.hasPermission
import io.github.ouyuanx.androidutils.permission.openPermissionSettings
import io.github.ouyuanx.androidutils.permission.requestPermissions
import io.github.ouyuanx.androidutils.storage.MMKVUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidUtilsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AndroidUtilsDemo(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun AndroidUtilsDemo(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val monitor = remember(context) { NetworkMonitor(context) }
    var networkState by remember(monitor) { mutableStateOf(monitor.currentState()) }
    var permissionStatus by remember { mutableStateOf("尚未执行权限用例") }
    var storageStatus by remember { mutableStateOf("尚未执行存储用例") }

    DisposableEffect(monitor) {
        val observation = monitor.observe { networkState = it }
        onDispose(observation::close)
    }

    DemoContent(
        versionName = context.appVersionName() ?: "未知",
        versionCode = context.appVersionCode()?.toString() ?: "未知",
        networkState = networkState,
        onCopy = { context.copyText("AndroidUtils") },
        onOpenRepository = {
            context.openUrl("https://github.com/ouyuanx/AndroidUtils")
        },
        onShare = {
            context.shareText(
                text = "AndroidUtils：https://github.com/ouyuanx/AndroidUtils",
                chooserTitle = "分享 AndroidUtils",
            )
        },
        permissionStatus = permissionStatus,
        onCheckCameraPermission = {
            val granted = context.hasPermission(PermissionLists.getCameraPermission())
            permissionStatus = "相机权限：${if (granted) "已授权" else "未授权"}"
        },
        onRequestCameraPermission = {
            context.requestPermissions(
                PermissionLists.getCameraPermission(),
                onResult = { permissionStatus = it.toStatusText("相机权限") },
            )
        },
        onRequestMicrophonePermission = {
            context.runPermissionCase(
                "麦克风权限",
                PermissionLists.getRecordAudioPermission(),
                onStatus = { permissionStatus = it },
            )
        },
        onRequestLocationPermission = {
            context.runPermissionCase(
                "位置权限",
                PermissionLists.getAccessFineLocationPermission(),
                PermissionLists.getAccessCoarseLocationPermission(),
                onStatus = { permissionStatus = it },
            )
        },
        onRequestNotificationPermission = {
            context.runPermissionCase(
                "通知权限",
                PermissionLists.getPostNotificationsPermission(),
                onStatus = { permissionStatus = it },
            )
        },
        onRequestImagePermission = {
            context.runPermissionCase(
                "图片读取权限",
                PermissionLists.getReadMediaImagesPermission(),
                onStatus = { permissionStatus = it },
            )
        },
        onOpenPermissionSettings = {
            context.openPermissionSettings()
            permissionStatus = "已尝试打开应用权限设置页"
        },
        storageStatus = storageStatus,
        onWriteStorage = {
            val value = "写入时间：${System.currentTimeMillis()}"
            MMKVUtils.putString(DEMO_STORAGE_KEY, value)
            storageStatus = "已保存：$value"
            LogUtils.d("MMKV 写入成功：%s", value)
        },
        onReadStorage = {
            val value = MMKVUtils.getString(DEMO_STORAGE_KEY)
            storageStatus = value?.let { "读取结果：$it" } ?: "没有找到已保存的数据"
            LogUtils.i("AndroidUtilsDemo - MMKV 读取结果：%s", value)
        },
        onRemoveStorage = {
            MMKVUtils.remove(DEMO_STORAGE_KEY)
            storageStatus = "已删除测试数据"
            LogUtils.w("MMKV 测试数据已删除")
        },
        onWriteLog = {
            LogUtils.d("AndroidUtilsDemo - 这是一条 Timber 调试日志")
            LogUtils.i("当前 MMKV 键数量：%d", MMKVUtils.count())
            storageStatus = "日志已输出，请在 Logcat 中查看 AndroidUtilsDemo"
        },
        modifier = modifier,
    )
}

@Composable
private fun DemoContent(
    versionName: String,
    versionCode: String,
    networkState: NetworkState,
    onCopy: () -> Unit,
    onOpenRepository: () -> Unit,
    onShare: () -> Unit,
    permissionStatus: String,
    onCheckCameraPermission: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onRequestMicrophonePermission: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestImagePermission: () -> Unit,
    onOpenPermissionSettings: () -> Unit,
    storageStatus: String,
    onWriteStorage: () -> Unit,
    onReadStorage: () -> Unit,
    onRemoveStorage: () -> Unit,
    onWriteLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transports = networkState.transports
        .joinToString(separator = "、") { it.name }
        .ifEmpty { "无" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "AndroidUtils 示例", style = MaterialTheme.typography.headlineMedium)
        Text(text = "应用版本：$versionName ($versionCode)")
        Text(text = "网络可用：${if (networkState.isAvailable) "是" else "否"}")
        Text(text = "网络已验证：${if (networkState.isValidated) "是" else "否"}")
        Text(text = "计费网络：${if (networkState.isMetered) "是" else "否"}")
        Text(text = "网络类型：$transports")

        Button(onClick = onCopy) {
            Text("复制库名称")
        }
        Button(onClick = onOpenRepository) {
            Text("打开 GitHub 仓库")
        }
        Button(onClick = onShare) {
            Text("分享项目")
        }

        Text(text = "权限测试", style = MaterialTheme.typography.titleLarge)
        Text(text = permissionStatus)

        Button(onClick = onCheckCameraPermission) {
            Text("检查相机权限")
        }
        Button(onClick = onRequestCameraPermission) {
            Text("申请相机权限")
        }
        Button(onClick = onRequestMicrophonePermission) {
            Text("申请麦克风权限")
        }
        Button(onClick = onRequestLocationPermission) {
            Text("申请位置权限")
        }
        Button(onClick = onRequestNotificationPermission) {
            Text("申请通知权限")
        }
        Button(onClick = onRequestImagePermission) {
            Text("申请图片读取权限")
        }
        Button(onClick = onOpenPermissionSettings) {
            Text("打开权限设置")
        }

        Text(text = "MMKV 与日志测试", style = MaterialTheme.typography.titleLarge)
        Text(text = storageStatus)

        Button(onClick = onWriteStorage) {
            Text("写入 MMKV")
        }
        Button(onClick = onReadStorage) {
            Text("读取 MMKV")
        }
        Button(onClick = onRemoveStorage) {
            Text("删除 MMKV 测试数据")
        }
        Button(onClick = onWriteLog) {
            Text("输出 Timber 日志")
        }
    }
}

private fun Context.runPermissionCase(
    caseName: String,
    vararg permissions: IPermission,
    onStatus: (String) -> Unit,
) {
    val activity = findActivity()
    if (activity == null) {
        onStatus("$caseName：无法获取 Activity，请求未发起")
        return
    }

    requestPermissions(*permissions) { result ->
        onStatus(result.toStatusText(caseName))
    }
}

private fun PermissionRequestResult.toStatusText(caseName: String): String = when {
    allGranted -> "$caseName：已全部授权"
    doNotAskAgain -> "$caseName：已被永久拒绝，请打开权限设置"
    else -> "$caseName：已授权 ${grantedPermissions.size} 项，拒绝 ${deniedPermissions.size} 项"
}

@Preview(showBackground = true)
@Composable
private fun AndroidUtilsDemoPreview() {
    AndroidUtilsTheme {
        DemoContent(
            versionName = "0.1.0",
            versionCode = "1",
            networkState = NetworkState(
                isAvailable = true,
                isValidated = true,
                isMetered = false,
                transports = emptySet(),
            ),
            onCopy = {},
            onOpenRepository = {},
            onShare = {},
            permissionStatus = "相机权限：未授权",
            onCheckCameraPermission = {},
            onRequestCameraPermission = {},
            onRequestMicrophonePermission = {},
            onRequestLocationPermission = {},
            onRequestNotificationPermission = {},
            onRequestImagePermission = {},
            onOpenPermissionSettings = {},
            storageStatus = "尚未执行存储用例",
            onWriteStorage = {},
            onReadStorage = {},
            onRemoveStorage = {},
            onWriteLog = {},
        )
    }
}

private const val DEMO_STORAGE_KEY = "android_utils_demo_message"

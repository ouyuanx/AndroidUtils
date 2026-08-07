package com.thondar.utils

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
import com.thondar.utils.ui.theme.AndroidUtilsTheme
import io.github.ouyuanx.androidutils.clipboard.copyText
import io.github.ouyuanx.androidutils.intent.openUrl
import io.github.ouyuanx.androidutils.intent.shareText
import io.github.ouyuanx.androidutils.network.NetworkMonitor
import io.github.ouyuanx.androidutils.network.NetworkState
import io.github.ouyuanx.androidutils.packageinfo.appVersionCode
import io.github.ouyuanx.androidutils.packageinfo.appVersionName

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
    }
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
        )
    }
}

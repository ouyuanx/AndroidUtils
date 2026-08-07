package io.github.ouyuanx.androidutils.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.CheckResult
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import java.io.Closeable
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/** 接收默认网络状态变化。 */
public fun interface NetworkStateListener {
    public fun onNetworkStateChanged(state: NetworkState)
}

/**
 * 读取并监听系统默认网络状态。
 *
 * 使用前需要在应用 Manifest 中声明 `android.permission.ACCESS_NETWORK_STATE`。
 * 监听完成后必须关闭 [Closeable]，避免继续持有回调。
 */
public class NetworkMonitor(context: Context) {
    private val appContext: Context = context.applicationContext
    private val connectivityManager: ConnectivityManager = requireNotNull(
        appContext.getSystemService(ConnectivityManager::class.java),
    ) { "Connectivity service is unavailable." }

    /** 读取当前默认网络的即时状态。 */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public fun currentState(): NetworkState {
        val network = connectivityManager.activeNetwork ?: return NetworkState.Unavailable
        return connectivityManager.getNetworkCapabilities(network).toNetworkState()
    }

    /**
     * 监听默认网络变化，并立即发送一次当前状态。
     *
     * 默认在主线程回调；调用方也可以传入自己的 [Executor]。返回值应在不再监听时关闭。
     */
    @CheckResult
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public fun observe(
        executor: Executor = ContextCompat.getMainExecutor(appContext),
        listener: NetworkStateListener,
    ): Closeable {
        val closed = AtomicBoolean(false)
        val lastState = AtomicReference<NetworkState?>()

        fun publish(state: NetworkState) {
            if (lastState.getAndSet(state) == state) return
            executor.execute {
                if (!closed.get()) listener.onNetworkStateChanged(state)
            }
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                publish(networkCapabilities.toNetworkState())
            }

            @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
            override fun onLost(network: Network) {
                publish(currentState())
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)
        publish(currentState())

        return Closeable {
            if (closed.compareAndSet(false, true)) {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
    }
}

private fun NetworkCapabilities?.toNetworkState(): NetworkState {
    if (this == null) return NetworkState.Unavailable

    return NetworkState(
        isAvailable = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
        isValidated = hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
        isMetered = !hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
        transports = transportSet(),
    )
}

private fun NetworkCapabilities.transportSet(): Set<NetworkTransport> = buildSet {
    fun addIfPresent(transportType: Int, transport: NetworkTransport) {
        if (hasTransport(transportType)) add(transport)
    }

    addIfPresent(NetworkCapabilities.TRANSPORT_WIFI, NetworkTransport.WIFI)
    addIfPresent(NetworkCapabilities.TRANSPORT_CELLULAR, NetworkTransport.CELLULAR)
    addIfPresent(NetworkCapabilities.TRANSPORT_ETHERNET, NetworkTransport.ETHERNET)
    addIfPresent(NetworkCapabilities.TRANSPORT_VPN, NetworkTransport.VPN)
    addIfPresent(NetworkCapabilities.TRANSPORT_BLUETOOTH, NetworkTransport.BLUETOOTH)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        addIfPresent(NetworkCapabilities.TRANSPORT_WIFI_AWARE, NetworkTransport.WIFI_AWARE)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        addIfPresent(NetworkCapabilities.TRANSPORT_LOWPAN, NetworkTransport.LOWPAN)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        addIfPresent(NetworkCapabilities.TRANSPORT_USB, NetworkTransport.USB)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        addIfPresent(NetworkCapabilities.TRANSPORT_THREAD, NetworkTransport.THREAD)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        addIfPresent(NetworkCapabilities.TRANSPORT_SATELLITE, NetworkTransport.SATELLITE)
    }
}

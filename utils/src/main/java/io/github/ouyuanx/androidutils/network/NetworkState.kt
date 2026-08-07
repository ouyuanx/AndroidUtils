package io.github.ouyuanx.androidutils.network

/** 系统当前默认网络使用的传输类型。 */
public enum class NetworkTransport {
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    BLUETOOTH,
    WIFI_AWARE,
    LOWPAN,
    USB,
    THREAD,
    SATELLITE,
}

/**
 * 当前默认网络状态快照。
 *
 * [isAvailable] 表示网络声明具备互联网能力；[isValidated] 表示 Android 系统已经验证
 * 该网络能够访问公共互联网。两者都可能随时发生变化。
 */
public data class NetworkState(
    public val isAvailable: Boolean,
    public val isValidated: Boolean,
    public val isMetered: Boolean,
    public val transports: Set<NetworkTransport>,
) {
    public companion object {
        /** 没有可用默认网络时的状态。 */
        @JvmField
        public val Unavailable: NetworkState = NetworkState(
            isAvailable = false,
            isValidated = false,
            isMetered = false,
            transports = emptySet(),
        )
    }
}

package io.github.ouyuanx.androidutils.storage

import android.content.Context
import android.os.Parcelable
import com.tencent.mmkv.MMKV

/** 基于 MMKV 的键值存储入口。 */
object MMKVUtils {
    private val isInitialized: Boolean
        get() = MMKV.getRootDir() != null

    /**
     * 初始化 MMKV，建议在 Application.onCreate 中调用。
     *
     * 重复调用不会再次初始化。
     */
    @Synchronized
    fun init(context: Context) {
        if (!isInitialized) {
            MMKV.initialize(context.applicationContext)
        }
    }

    /** 获取默认单进程实例。 */
    private val default: MMKV
        get() {
            checkInitialized()
            return MMKV.defaultMMKV()
        }

    fun putBoolean(key: String, value: Boolean) {
        default.encode(key, value)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        default.decodeBool(key, defaultValue)

    fun putInt(key: String, value: Int) {
        default.encode(key, value)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int =
        default.decodeInt(key, defaultValue)

    fun putLong(key: String, value: Long) {
        default.encode(key, value)
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long =
        default.decodeLong(key, defaultValue)

    fun putFloat(key: String, value: Float) {
        default.encode(key, value)
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float =
        default.decodeFloat(key, defaultValue)

    fun putDouble(key: String, value: Double) {
        default.encode(key, value)
    }

    fun getDouble(key: String, defaultValue: Double = 0.0): Double =
        default.decodeDouble(key, defaultValue)

    /** 写入字符串；[value] 为 `null` 时由 MMKV 删除对应值。 */
    fun putString(key: String, value: String?) {
        default.encode(key, value)
    }

    /** 读取字符串；键不存在时返回 `null`。 */
    fun getString(key: String): String? = default.decodeString(key)

    /** 读取字符串；键不存在时返回 [defaultValue]。 */
    fun getString(key: String, defaultValue: String): String =
        default.decodeString(key, defaultValue) ?: defaultValue

    fun putBytes(key: String, value: ByteArray?) {
        default.encode(key, value)
    }

    fun getBytes(key: String): ByteArray? = default.decodeBytes(key)

    fun <T : Parcelable> putParcelable(key: String, value: T?) {
        default.encode(key, value)
    }

    fun <T : Parcelable> getParcelable(key: String, clazz: Class<T>): T? =
        default.decodeParcelable(key, clazz)

    inline fun <reified T : Parcelable> getParcelable(key: String): T? =
        getParcelable(key, T::class.java)

    fun putStringSet(key: String, value: Set<String>?) {
        default.encode(key, value)
    }

    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String> =
        default.decodeStringSet(key, defaultValue) ?: defaultValue

    fun containsKey(key: String): Boolean = default.containsKey(key)

    fun remove(key: String) {
        default.removeValueForKey(key)
    }

    /** 删除键名以 [prefix] 开头的全部值。 */
    fun removeByPrefix(prefix: String) {
        require(prefix.isNotEmpty()) { "prefix 不能为空，清空全部数据请调用 clearAll()" }
        val keys = default.allKeys()?.filter { it.startsWith(prefix) }.orEmpty()
        if (keys.isNotEmpty()) {
            default.removeValuesForKeys(keys.toTypedArray())
        }
    }

    fun clearAll() {
        default.clearAll()
    }

    fun allKeys(): Set<String> = default.allKeys()?.toSet().orEmpty()

    fun count(): Long = default.count()

    fun totalSize(): Long = default.totalSize()

    private fun checkInitialized() {
        check(isInitialized) {
            "MMKV 尚未初始化，请先在 Application.onCreate 中调用 MMKVUtils.init(context)"
        }
    }
}

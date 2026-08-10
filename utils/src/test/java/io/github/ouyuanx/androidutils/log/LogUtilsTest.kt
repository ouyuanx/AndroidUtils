package io.github.ouyuanx.androidutils.log

import android.util.Log
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class LogUtilsTest {
    private lateinit var recordingTree: RecordingTree

    @Before
    fun setUp() {
        Timber.uprootAll()
        recordingTree = RecordingTree()
        Timber.plant(recordingTree)
    }

    @After
    fun tearDown() {
        Timber.uprootAll()
    }

    @Test
    fun `debug log formats arguments`() {
        LogUtils.d("用户 %s 登录", "ouyuanx")

        assertEquals(Log.DEBUG, recordingTree.priority)
        assertEquals("用户 ouyuanx 登录", recordingTree.message)
    }

    private class RecordingTree : Timber.Tree() {
        var priority: Int? = null
        var message: String? = null

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            this.priority = priority
            this.message = message
        }
    }
}

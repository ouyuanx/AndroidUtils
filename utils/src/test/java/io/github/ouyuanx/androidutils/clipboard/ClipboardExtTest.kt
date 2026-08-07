package io.github.ouyuanx.androidutils.clipboard

import android.content.ClipboardManager
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClipboardExtTest {
    @Test
    fun `copyText writes plain text to clipboard`() {
        val context: Context = RuntimeEnvironment.getApplication()

        context.copyText(text = "AndroidUtils", label = "test")

        val clipboard = context.getSystemService(ClipboardManager::class.java)
        assertEquals("AndroidUtils", clipboard.primaryClip?.getItemAt(0)?.text?.toString())
        assertEquals("test", clipboard.primaryClipDescription?.label?.toString())
    }
}

package io.github.ouyuanx.androidutils.toast

import android.app.Application
import com.hjq.toast.Toaster
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ToastUtilsTest {
    @Test
    fun `init configures Toaster`() {
        val application: Application = RuntimeEnvironment.getApplication()

        ToastUtils.init(application)

        assertTrue(Toaster.isInit())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `showDelayed rejects negative delay`() {
        val application: Application = RuntimeEnvironment.getApplication()
        ToastUtils.init(application)

        ToastUtils.showDelayed("message", -1)
    }
}

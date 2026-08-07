package io.github.ouyuanx.androidutils.activity

import android.app.Activity
import android.content.ContextWrapper
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ContextActivityExtTest {
    @Test
    fun `findActivity unwraps nested ContextWrapper instances`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val context = ContextWrapper(ContextWrapper(activity))

        assertSame(activity, context.findActivity())
    }

    @Test
    fun `findActivity returns null for application context`() {
        assertNull(RuntimeEnvironment.getApplication().findActivity())
    }
}

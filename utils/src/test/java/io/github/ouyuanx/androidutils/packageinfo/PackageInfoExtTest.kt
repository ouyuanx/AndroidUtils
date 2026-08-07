package io.github.ouyuanx.androidutils.packageinfo

import android.content.Context
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PackageInfoExtTest {
    @Test
    fun `appVersionCode reads current package info`() {
        val context: Context = RuntimeEnvironment.getApplication()

        assertNotNull(context.appVersionCode())
    }
}

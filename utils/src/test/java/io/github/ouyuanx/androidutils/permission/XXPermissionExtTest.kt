package io.github.ouyuanx.androidutils.permission

import android.content.Context
import com.hjq.permissions.permission.PermissionLists
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class XXPermissionExtTest {
    private val cameraPermission = PermissionLists.getCameraPermission()

    @Test
    fun `allGranted reflects denied permission list`() {
        assertTrue(
            PermissionRequestResult(
                grantedPermissions = listOf(cameraPermission),
                deniedPermissions = emptyList(),
                doNotAskAgain = false,
            ).allGranted,
        )
        assertFalse(
            PermissionRequestResult(
                grantedPermissions = emptyList(),
                deniedPermissions = listOf(cameraPermission),
                doNotAskAgain = true,
            ).allGranted,
        )
    }

    @Test
    fun `requestPermissions does not request without an Activity`() {
        val context: Context = RuntimeEnvironment.getApplication()
        var callbackCalled = false

        context.requestPermissions(cameraPermission) {
            callbackCalled = true
        }

        assertFalse(callbackCalled)
    }
}

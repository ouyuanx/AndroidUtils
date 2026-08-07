package io.github.ouyuanx.androidutils.packageinfo

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PackageInfoExtTest {
    @Test
    fun `appName reads current application label`() {
        val context: Context = RuntimeEnvironment.getApplication()

        assertTrue(context.appName().isNotBlank())
    }

    @Test
    fun `appVersionCode reads current package info`() {
        val context: Context = RuntimeEnvironment.getApplication()

        assertNotNull(context.appVersionCode())
    }

    @Test
    fun `isPackageInstalled handles visible and invalid packages`() {
        val context: Context = RuntimeEnvironment.getApplication()

        assertTrue(context.isPackageInstalled(context.packageName))
        assertFalse(context.isPackageInstalled(""))
        assertFalse(context.isPackageInstalled("invalid.package.name"))
    }

    @Test
    fun `signatureFingerprint formats MD5 with uppercase colon separated hex`() {
        assertEquals(
            "5D:41:40:2A:BC:4B:2A:76:B9:71:9D:91:10:17:C5:92",
            "hello".encodeToByteArray().signatureFingerprint(SignatureDigestAlgorithm.MD5),
        )
    }

    @Test
    fun `signatureFingerprint formats SHA1 with uppercase colon separated hex`() {
        assertEquals(
            "AA:F4:C6:1D:DC:C5:E8:A2:DA:BE:DE:0F:3B:48:2C:D9:AE:A9:43:4D",
            "hello".encodeToByteArray().signatureFingerprint(SignatureDigestAlgorithm.SHA1),
        )
    }

    @Test
    fun `signatureFingerprint formats SHA256 with uppercase colon separated hex`() {
        assertEquals(
            "2C:F2:4D:BA:5F:B0:A3:0E:26:E8:3B:2A:C5:B9:E2:9E:1B:16:1E:5C:1F:A7:42:5E:73:04:33:62:93:8B:98:24",
            "hello".encodeToByteArray().signatureFingerprint(SignatureDigestAlgorithm.SHA256),
        )
    }
}

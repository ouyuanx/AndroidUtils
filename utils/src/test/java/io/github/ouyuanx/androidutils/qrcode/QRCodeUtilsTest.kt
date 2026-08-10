package io.github.ouyuanx.androidutils.qrcode

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QRCodeUtilsTest {
    @Test
    fun `generate creates requested bitmap size`() {
        val bitmap = QRCodeUtils.generate("https://github.com/ouyuanx/AndroidUtils", 256)

        assertEquals(256, bitmap.width)
        assertEquals(256, bitmap.height)
        assertFalse(bitmap.isRecycled)
        bitmap.recycle()
    }

    @Test
    fun `generate supports a center logo without recycling input`() {
        val logo = Bitmap.createBitmap(32, 16, Bitmap.Config.ARGB_8888)
        val bitmap = QRCodeUtils.generate("AndroidUtils", 200, logo)

        assertEquals(200, bitmap.width)
        assertFalse(logo.isRecycled)
        bitmap.recycle()
        logo.recycle()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generate rejects blank content`() {
        QRCodeUtils.generate(" ", 100)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generate rejects non-positive size`() {
        QRCodeUtils.generate("AndroidUtils", 0)
    }
}

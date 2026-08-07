package io.github.ouyuanx.androidutils.intent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IntentExtTest {
    @Test
    fun `parcelableExtra reads a typed Parcelable`() {
        val expected = Uri.parse("https://github.com/ouyuanx/AndroidUtils")
        val intent = Intent().putExtra("uri", expected)

        assertEquals(expected, intent.parcelableExtra<Uri>("uri"))
    }

    @Test
    fun `Bundle parcelable reads a typed Parcelable`() {
        val expected = Uri.parse("content://android-utils/item")
        val bundle = Bundle().apply { putParcelable("uri", expected) }

        assertEquals(expected, bundle.parcelable<Uri>("uri"))
    }

    @Test
    fun `openUrl rejects non-http schemes`() {
        assertFalse(RuntimeEnvironment.getApplication().openUrl("javascript:alert(1)"))
    }
}

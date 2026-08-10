package io.github.ouyuanx.androidutils.qrcode

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

/** 二维码生成工具。 */
object QRCodeUtils {
    /**
     * 生成边长为 [size] 像素的黑白二维码。
     *
     * [logo] 不为 `null` 时会缩放后绘制在二维码中心，调用方仍负责回收传入的 Bitmap。
     */
    @JvmOverloads
    fun generate(
        content: String,
        size: Int,
        logo: Bitmap? = null,
    ): Bitmap {
        require(content.isNotBlank()) { "content 不能为空" }
        require(size > 0) { "size 必须大于 0" }
        require(logo == null || (!logo.isRecycled && logo.width > 0 && logo.height > 0)) {
            "logo 必须是未回收的有效 Bitmap"
        }

        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.MARGIN, 1)
        }
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        return createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bitmap ->
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
            if (logo != null) bitmap.drawCenteredLogo(logo)
        }
    }

    private fun Bitmap.drawCenteredLogo(logo: Bitmap) {
        val logoMaxSide = (width * LOGO_SIZE_RATIO).toInt().coerceAtLeast(1)
        val logoScale = minOf(
            logoMaxSide.toFloat() / logo.width,
            logoMaxSide.toFloat() / logo.height,
        )
        val scaledWidth = (logo.width * logoScale).toInt().coerceAtLeast(1)
        val scaledHeight = (logo.height * logoScale).toInt().coerceAtLeast(1)
        val scaledLogo = logo.scale(scaledWidth, scaledHeight)
        val left = (width - scaledWidth) / 2f
        val top = (height - scaledHeight) / 2f
        val padding = (width * LOGO_BACKGROUND_PADDING_RATIO).coerceAtLeast(1f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val canvas = Canvas(this)

        paint.color = Color.WHITE
        canvas.drawRoundRect(
            RectF(
                left - padding,
                top - padding,
                left + scaledWidth + padding,
                top + scaledHeight + padding,
            ),
            padding,
            padding,
            paint,
        )
        canvas.drawBitmap(scaledLogo, left, top, paint)

        if (scaledLogo !== logo) scaledLogo.recycle()
    }

    private const val LOGO_SIZE_RATIO = 0.2f
    private const val LOGO_BACKGROUND_PADDING_RATIO = 0.02f
}

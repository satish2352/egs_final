package com.sumagoinfotech.digicopy.utils
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.graphics.Path
import android.graphics.RectF
import androidx.appcompat.widget.AppCompatImageView
class CircularImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val clipPath = Path()

    override fun onDraw(canvas: Canvas) {
        clipPath.reset()
        val radius = width.coerceAtLeast(height) / 2f
        val centerX = width / 2f
        val centerY = height / 2f
        clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
        canvas.clipPath(clipPath)
        super.onDraw(canvas)
    }
}
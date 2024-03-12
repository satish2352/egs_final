package com.sumagoinfotech.digicopy.utils
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    private var mScaleFactor = 1.0f

    init {
        // Initialize ScaleGestureDetector
        mScaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Pass touch events to ScaleGestureDetector
        event?.let { mScaleGestureDetector.onTouchEvent(it) }
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            detector?.let {
                mScaleFactor *= it.scaleFactor
                mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f))
                scaleX = mScaleFactor
                scaleY = mScaleFactor
            }
            return true
        }

    }
}

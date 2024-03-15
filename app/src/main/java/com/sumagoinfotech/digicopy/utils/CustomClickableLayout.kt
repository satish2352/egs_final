package com.sumagoinfotech.digicopy.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import com.sumagoinfotech.digicopy.R

class CustomClickableLayout : LinearLayout {

    private var cornerRadius = 0f
    private var backgroundColorNormal = 0
    private var backgroundColorPressed = 0
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var rect = RectF()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomClickableLayout)
        cornerRadius = typedArray.getDimension(R.styleable.CustomClickableLayout_cornerRadius, 0f)
        backgroundColorNormal = typedArray.getColor(R.styleable.CustomClickableLayout_backgroundColorNormal, 0)
        backgroundColorPressed = typedArray.getColor(R.styleable.CustomClickableLayout_backgroundColorPressed, 0)
        typedArray.recycle()
    }

    override fun dispatchDraw(canvas: Canvas) {
        paint.color = if (isPressed) backgroundColorPressed else backgroundColorNormal
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        super.dispatchDraw(canvas)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is TextView) {
                child.setOnClickListener {
                    setBackgroundColor(backgroundColorPressed)
                }
            }
        }
    }
}

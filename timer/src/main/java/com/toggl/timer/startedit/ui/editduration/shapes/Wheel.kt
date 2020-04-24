package com.toggl.timer.startedit.ui.editduration.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt

class Wheel(
    private val bounds: RectF,
    strokeWidth: Float,
    @ColorInt fillColor: Int
) {
    private var hidden = false

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.STROKE
        setStrokeWidth(strokeWidth)
    }

    fun onDraw(canvas: Canvas?) {
        if (hidden) return
        canvas?.drawArc(bounds, 0f, 360f, false, paint)
    }

    fun setFillColor(@ColorInt fillColor: Int) {
        paint.color = fillColor
    }

    fun setHidden(isHidden: Boolean) {
        hidden = isHidden
    }
}
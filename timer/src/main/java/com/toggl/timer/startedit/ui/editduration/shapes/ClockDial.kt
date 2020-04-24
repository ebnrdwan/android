package com.toggl.timer.startedit.ui.editduration.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import androidx.annotation.ColorInt
import com.toggl.timer.startedit.util.MathConstants.fullCircle
import com.toggl.timer.startedit.util.MathConstants.minutesInAnHour
import com.toggl.timer.startedit.util.updateWithPointOnCircumference

class ClockDial(
    private val dialCenter: PointF,
    textSize: Float,
    @ColorInt textColor: Int,
    private val textRadius: Float
) {
    private val angleOffsetCorrection = fullCircle / 4f
    private val numberPaddingChar = '0'
    private val digitsCount = 2
    private val textBounds = Rect()
    private val textCenter = PointF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        setTextSize(textSize)
        color = textColor
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }

    fun onDraw(canvas: Canvas?) = canvas?.run {
        for (minute in (0 until minutesInAnHour) step 5) {
            val angle = fullCircle * (minute.toFloat() / minutesInAnHour) - angleOffsetCorrection
            drawMinuteNumber(this, minute, angle)
        }
    }

    private fun drawMinuteNumber(canvas: Canvas, minute: Int, angle: Double) {
        val minuteText = minute.toString().padStart(digitsCount, numberPaddingChar)
        textCenter.updateWithPointOnCircumference(dialCenter, angle, textRadius.toDouble())
        paint.getTextBounds(minuteText, 0, minuteText.length, textBounds)
        val centeredTextX = textCenter.x - textBounds.width() / 2f
        val centeredTextY = textCenter.y + textBounds.height() / 2f
        canvas.drawText(minuteText, centeredTextX, centeredTextY, paint)
    }
}
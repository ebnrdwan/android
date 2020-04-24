package com.toggl.timer.startedit.ui.editduration

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.toggl.timer.R
import com.toggl.timer.startedit.ui.editduration.shapes.ClockDial
import com.toggl.timer.startedit.ui.editduration.shapes.Wheel

class WheelBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val center = PointF()
    private val bounds = RectF()

    @ColorInt
    private var wheelColor: Int

    @ColorInt
    private var textColor: Int

    private var radius: Float = 0f
    private var arcWidth: Float = 0f
    private var capWidth: Float = 0f
    private var textRadius: Float = 0f
    private var textSize: Float = 0f
    private var extraTextDistanceFromArc: Float = 0f

    private lateinit var wheel: Wheel
    private lateinit var clockDial: ClockDial

    init {
        val defaultArcWidth = context.resources.getDimension(R.dimen.default_wheel_background_arc_width)
        val defaultCapWidth = context.resources.getDimension(R.dimen.default_wheel_background_cap_width)
        val defaultTextSize = context.resources.getDimension(R.dimen.default_wheel_background_text_size)
        val defaultExtraTextDistanceFromArc = context.resources.getDimension(R.dimen.default_wheel_background_extra_text_distance_from_arc)
        val defaultWheelColor = ContextCompat.getColor(context, R.color.default_wheel_background_wheel_color)
        val defaultTextColor = ContextCompat.getColor(context, R.color.default_wheel_background_text_color)
        context.theme.obtainStyledAttributes(attrs, R.styleable.WheelBackgroundView, 0, 0).apply {
            try {
                arcWidth = getDimension(R.styleable.WheelBackgroundView_arcWidth, defaultArcWidth)
                capWidth = getDimension(R.styleable.WheelBackgroundView_capWidth, defaultCapWidth)
                textSize = getDimension(R.styleable.WheelBackgroundView_textSize, defaultTextSize)
                extraTextDistanceFromArc = getDimension(R.styleable.WheelBackgroundView_extraTextDistanceFromArc, defaultExtraTextDistanceFromArc)
                wheelColor = getColor(R.styleable.WheelBackgroundView_wheelColor, defaultWheelColor)
                textColor = getColor(R.styleable.WheelBackgroundView_textColor, defaultTextColor)
            } finally {
                recycle()
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        radius = width * 0.5f
        center.set(radius, radius)
        bounds.set(capWidth, capWidth, width - capWidth, width - capWidth)
        textRadius = radius - capWidth * 2 - extraTextDistanceFromArc
        setupDrawingDelegates()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        wheel.onDraw(canvas)
        clockDial.onDraw(canvas)
    }

    private fun setupDrawingDelegates() {
        wheel = Wheel(bounds, arcWidth, wheelColor)
        clockDial = ClockDial(center, textSize, textColor, textRadius)
    }
}
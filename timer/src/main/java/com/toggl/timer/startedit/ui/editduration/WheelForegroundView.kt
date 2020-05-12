package com.toggl.timer.startedit.ui.editduration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import com.toggl.common.performTickEffect
import com.toggl.timer.R
import com.toggl.timer.extensions.absoluteDurationBetween
import com.toggl.timer.extensions.roundToClosestMinute
import com.toggl.timer.extensions.timeOfDay
import com.toggl.timer.startedit.ui.editduration.shapes.Arc
import com.toggl.timer.startedit.ui.editduration.shapes.Dot
import com.toggl.timer.startedit.ui.editduration.shapes.HandleCap
import com.toggl.timer.startedit.ui.editduration.shapes.Wheel
import com.toggl.timer.startedit.util.MathConstants
import com.toggl.timer.startedit.util.angleBetween
import com.toggl.timer.startedit.util.angleToTime
import com.toggl.timer.startedit.util.distanceSq
import com.toggl.timer.startedit.util.getPingPongIndexedItem
import com.toggl.timer.startedit.util.isBetween
import com.toggl.timer.startedit.util.toAngleOnTheDial
import com.toggl.timer.startedit.util.toPositiveAngle
import com.toggl.timer.startedit.util.updateWithPointOnCircumference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

@FlowPreview
@ExperimentalCoroutinesApi
class WheelForegroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var hapticFeedbackProvider: Vibrator?

    @ColorInt
    private var capBackgroundColor: Int

    @ColorInt
    private var capBorderColor: Int

    @ColorInt
    private var capIconColor: Int

    @ColorInt
    private var capShadowColor: Int

    @DrawableRes
    private var startCapIconId: Int

    @DrawableRes
    private var endCapIconId: Int

    private var rainbowColors: List<Int>
    private var wheelHandleDotIndicatorRadius = 0f
    private var arcWidth = 0f
    private var capWidth = 0f
    private var capBorderStrokeWidth = 0f
    private var capShadowWidth = 0f
    private var capIconSize = 0

    private var radius = 0f
    private val center: PointF = PointF(Float.NaN, Float.NaN)
    private val startTimePosition = PointF(Float.NaN, Float.NaN)
    private val endTimePosition = PointF(Float.NaN, Float.NaN)
    private val touchInteractionPointF = PointF()
    private val wheelBounds: RectF = RectF()
    private var wheelHandleDotIndicatorDistanceToCenter = 0f
    private val extendedRadiusMultiplier = 1.5f
    private var handleCapsPositionRadius = 0.0

    private lateinit var fullWheel: Wheel
    private lateinit var arc: Arc
    private lateinit var endCap: HandleCap
    private lateinit var startCap: HandleCap
    private lateinit var wheelHandleDotIndicator: Dot

    private var updateType: WheelUpdateType = WheelUpdateType.None
    private var editBothAtOnceStartTimeAngleOffset = 0.0

    var isRunning: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }

    var minimumStartTime: OffsetDateTime = OffsetDateTime.MIN
    var maximumStartTime: OffsetDateTime = OffsetDateTime.now()
    var minimumEndTime: OffsetDateTime = OffsetDateTime.now()
    var maximumEndTime: OffsetDateTime = OffsetDateTime.MAX

    private var startTimeAngle = 0.0
    private var endTimeAngle = 0.0

    private var startAndEndTimeChannel = ConflatedBroadcastChannel<Pair<OffsetDateTime, OffsetDateTime>>(OffsetDateTime.now() to OffsetDateTime.now())

    private var startAndEndTime: Pair<OffsetDateTime, OffsetDateTime>
        get() = startAndEndTimeChannel.value
        set(value) {
            var (newStartTime, newEndTime) = value

            if (startAndEndTimeChannel.value.first != newStartTime) {
                newStartTime = newStartTime.coerceIn(minimumStartTime, maximumStartTime)

                if (center.x.isNaN()) return

                startTimeAngle = newStartTime.timeOfDay().toAngleOnTheDial().toPositiveAngle()
                startTimePosition.updateWithPointOnCircumference(center, startTimeAngle, handleCapsPositionRadius)
            }
            if (startAndEndTimeChannel.value.second != newEndTime) {
                newEndTime = newEndTime.coerceIn(minimumEndTime, maximumEndTime)

                if (center.x.isNaN()) return

                endTimeAngle = newEndTime.timeOfDay().toAngleOnTheDial().toPositiveAngle()
                endTimePosition.updateWithPointOnCircumference(center, endTimeAngle, handleCapsPositionRadius)
            }

            arc.update(startTimeAngle, endTimeAngle)
            wheelHandleDotIndicator.update(startTimeAngle, endTimeAngle)

            startAndEndTimeChannel.offer(newStartTime to newEndTime)
            invalidate()
        }

    var startTime: OffsetDateTime
        get() = startAndEndTimeChannel.value.first
        set(value) {
            startAndEndTime = value to endTime
        }

    var endTime: OffsetDateTime
        get() = startAndEndTimeChannel.value.second
        set(value) {
            startAndEndTime = startTime to value
        }

    val startAndEndTimeFlow = startAndEndTimeChannel.asFlow().distinctUntilChanged()

    private val isEditingChannel = ConflatedBroadcastChannel(false)
    val isEditingFlow = isEditingChannel.asFlow()

    fun isEditing() = isEditingChannel.value

    init {
        hapticFeedbackProvider = context.getSystemService()

        context.resources.run {
            val defaultArcWidth = getDimension(R.dimen.default_wheel_foreground_arc_width)
            val defaultCapWidth = getDimension(R.dimen.default_wheel_foreground_cap_width)
            val defaultCapIconSize = getDimensionPixelSize(R.dimen.default_wheel_foreground_cap_icon_size)
            val defaultCapShadowWidth = getDimension(R.dimen.default_wheel_foreground_cap_shadow_width)
            val defaultCapBorderStrokeWidth = getDimension(R.dimen.default_wheel_foreground_cap_border_stroke_width)
            val defaultHandleDotIndicatorRadius = getDimension(R.dimen.default_wheel_foreground_handle_dot_indicator_radius)
            val defaultCapBackgroundColor =
                ContextCompat.getColor(context, R.color.default_wheel_foreground_cap_background_color)
            val defaultCapBorderColor = ContextCompat.getColor(context, R.color.default_wheel_foreground_cap_border_color)
            val defaultCapIconColor = ContextCompat.getColor(context, R.color.default_wheel_foreground_cap_icon_color)
            val defaultCapShadowColor = ContextCompat.getColor(context, R.color.default_wheel_foreground_cap_shadow_color)
            context.theme.obtainStyledAttributes(attrs, R.styleable.WheelForegroundView, 0, 0).apply {
                try {
                    arcWidth = getDimension(R.styleable.WheelForegroundView_arcWidth, defaultArcWidth)
                    capWidth = getDimension(R.styleable.WheelForegroundView_capWidth, defaultCapWidth)
                    capIconSize = getDimensionPixelSize(R.styleable.WheelForegroundView_capIconSize, defaultCapIconSize)
                    capShadowWidth = getDimension(R.styleable.WheelForegroundView_capShadowWidth, defaultCapShadowWidth)
                    capBorderStrokeWidth =
                        getDimension(R.styleable.WheelForegroundView_capBorderStrokeWidth, defaultCapBorderStrokeWidth)
                    capBackgroundColor =
                        getColor(R.styleable.WheelForegroundView_capBackgroundColor, defaultCapBackgroundColor)
                    capBorderColor = getColor(R.styleable.WheelForegroundView_capBorderColor, defaultCapBorderColor)
                    capIconColor = getColor(R.styleable.WheelForegroundView_capIconColor, defaultCapIconColor)
                    capShadowColor = getColor(R.styleable.WheelForegroundView_capShadowColor, defaultCapShadowColor)
                    wheelHandleDotIndicatorRadius = getDimension(
                        R.styleable.WheelForegroundView_wheelHandleDotIndicatorRadius,
                        defaultHandleDotIndicatorRadius
                    )
                    val rainbowColorsId =
                        getResourceId(R.styleable.WheelForegroundView_wheelRainbowColors, R.array.default_wheel_rainbow_colors)
                    rainbowColors = getIntArray(rainbowColorsId).toList()
                    startCapIconId = getResourceId(R.styleable.WheelForegroundView_startCapIcon, R.drawable.ic_play)
                    endCapIconId = getResourceId(R.styleable.WheelForegroundView_endCapIcon, R.drawable.ic_stop)
                } finally {
                    recycle()
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!changed) return

        radius = width * 0.5f
        center.set(radius, radius)
        wheelBounds.set(capWidth, capWidth, width - capWidth, width - capWidth)
        wheelHandleDotIndicatorDistanceToCenter = radius - capWidth / 2f
        handleCapsPositionRadius = (radius - capWidth).toDouble()
        startTimeAngle = startTime.timeOfDay().toAngleOnTheDial().toPositiveAngle()
        startTimePosition.updateWithPointOnCircumference(center, startTimeAngle, handleCapsPositionRadius)
        endTimeAngle = endTime.timeOfDay().toAngleOnTheDial().toPositiveAngle()
        endTimePosition.updateWithPointOnCircumference(center, endTimeAngle, handleCapsPositionRadius)
        setupDrawingDelegates()
        arc.update(startTimeAngle, endTimeAngle)
        wheelHandleDotIndicator.update(startTimeAngle, endTimeAngle)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        updateUIElements()
        fullWheel.onDraw(canvas)
        arc.onDraw(canvas)
        wheelHandleDotIndicator.onDraw(canvas)
        endCap.onDraw(canvas)
        startCap.onDraw(canvas)
    }

    private fun numberOfFullLoops() = (endTime.absoluteDurationBetween(startTime)).toHours().toInt()

    private fun isFullCircle() = numberOfFullLoops() >= 1

    @ColorInt
    private fun backgroundColor(): Int = rainbowColors.getPingPongIndexedItem(numberOfFullLoops())

    @ColorInt
    private fun foregroundColor(): Int = rainbowColors.getPingPongIndexedItem(numberOfFullLoops() + 1)

    private fun setupDrawingDelegates() {
        fullWheel = Wheel(wheelBounds, arcWidth, backgroundColor())
        arc = Arc(wheelBounds, arcWidth, foregroundColor())
        val endCapBitmap = ContextCompat.getDrawable(context, endCapIconId)!!.toBitmap(capIconSize, capIconSize)
        val startCapBitmap = ContextCompat.getDrawable(context, startCapIconId)!!.toBitmap(capIconSize, capIconSize)
        endCap = createCapWithIcon(endCapBitmap)
        startCap = createCapWithIcon(startCapBitmap)
        wheelHandleDotIndicator = Dot(
            center,
            wheelHandleDotIndicatorDistanceToCenter,
            wheelHandleDotIndicatorRadius,
            capIconColor
        )
    }

    private fun createCapWithIcon(iconBitmap: Bitmap): HandleCap =
        HandleCap(
            radius = capWidth / 2f,
            arcWidth = arcWidth,
            capColor = capBackgroundColor,
            capBorderColor = capBorderColor,
            foregroundColor = foregroundColor(),
            capShadowColor = capShadowColor,
            capBorderStrokeWidth = capBorderStrokeWidth,
            iconBitmap = iconBitmap,
            iconColor = capIconColor,
            shadowWidth = capShadowWidth
        )

    private fun updateUIElements() {
        startCap.setPosition(startTimePosition)
        startCap.setForegroundColor(foregroundColor())
        endCap.setPosition(endTimePosition)
        endCap.setForegroundColor(foregroundColor())
        endCap.setShowOnlyBackground(isRunning)

        fullWheel.setFillColor(backgroundColor())
        fullWheel.setHidden(!isFullCircle())

        arc.setFillColor(foregroundColor())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchInteractionPointF.setCoordinatesFrom(event)
                touchesBegan()
                true
            }
            MotionEvent.ACTION_MOVE -> {
                touchInteractionPointF.setCoordinatesFrom(event)
                touchesMoved()
                true
            }
            MotionEvent.ACTION_UP -> {
                touchesEnded()
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                touchesCancelled()
                super.onTouchEvent(event)
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun PointF.setCoordinatesFrom(event: MotionEvent) {
        set(event.x, event.y)
    }

    private fun touchesBegan() {
        if (touchInteractionIsValid()) {
            isEditingChannel.offer(true)
        }
    }

    private fun touchesMoved() {
        if (!isEditingChannel.value) return

        val previousAngle = when (updateType) {
            WheelUpdateType.EditStartTime -> startTimeAngle
            WheelUpdateType.EditEndTime -> endTimeAngle
            else -> startTimeAngle + editBothAtOnceStartTimeAngleOffset
        }

        val currentAngle = angleBetween(touchInteractionPointF, center)
        var angleChange = currentAngle - previousAngle
        while (angleChange < -kotlin.math.PI) angleChange += MathConstants.fullCircle
        while (angleChange > kotlin.math.PI) angleChange -= MathConstants.fullCircle

        updateEditedTime(angleChange.angleToTime())
    }

    private fun touchesEnded() {
        finishTouchEditing()
    }

    private fun touchesCancelled() {
        finishTouchEditing()
    }

    private fun finishTouchEditing() {
        isEditingChannel.offer(false)
    }

    private fun touchInteractionIsValid(): Boolean {
        val intention = determineTouchIntention()
        updateType = intention

        if (intention == WheelUpdateType.None)
            return false

        if (intention == WheelUpdateType.EditBothAtOnce) {
            editBothAtOnceStartTimeAngleOffset = angleBetween(touchInteractionPointF, center) - startTimeAngle
        }

        return true
    }

    private fun determineTouchIntention(): WheelUpdateType {
        if (touchesStartCap()) {
            return WheelUpdateType.EditStartTime
        }

        if (!isRunning && touchesEndCap()) {
            return WheelUpdateType.EditEndTime
        }

        if (touchesStartCap(useExtendedRadius = true)) {
            return WheelUpdateType.EditStartTime
        }

        if (!isRunning && touchesEndCap(useExtendedRadius = true)) {
            return WheelUpdateType.EditEndTime
        }

        if (!isRunning && isTouchingBetweenStartAndStop()) {
            return WheelUpdateType.EditBothAtOnce
        }

        return WheelUpdateType.None
    }

    private fun touchesStartCap(useExtendedRadius: Boolean = false): Boolean =
        touchInteractionPointF.isCloseEnoughTo(startTimePosition, calculateCapRadius(useExtendedRadius))

    private fun touchesEndCap(useExtendedRadius: Boolean = false): Boolean =
        touchInteractionPointF.isCloseEnoughTo(endTimePosition, calculateCapRadius(useExtendedRadius))

    private fun calculateCapRadius(extendedRadius: Boolean): Float =
        (if (extendedRadius) extendedRadiusMultiplier else 1f) * (capWidth / 2)

    private fun PointF.isCloseEnoughTo(endPoint: PointF, radius: Float): Boolean =
        distanceSq(this, endPoint) < radius * radius

    private fun isTouchingBetweenStartAndStop(): Boolean {
        val distanceFromCenterSq = distanceSq(center, touchInteractionPointF)

        if (distanceFromCenterSq < capWidth * capWidth || distanceFromCenterSq > radius * radius)
            return false

        val angle = angleBetween(touchInteractionPointF, center)
        return isFullCircle() || angle.toDouble().isBetween(startTimeAngle, endTimeAngle)
    }

    private fun updateEditedTime(diff: Duration) {
        var giveFeedback = false

        if (updateType == WheelUpdateType.EditBothAtOnce) {
            val nextEndTime = (endTime + diff).roundToClosestMinute()
            val nextStartTime = (startTime + diff).roundToClosestMinute()
            giveFeedback = nextStartTime != startTime
            startAndEndTime = nextStartTime to nextEndTime
        } else {
            if (updateType == WheelUpdateType.EditStartTime) {
                val nextStartTime = (startTime + diff).roundToClosestMinute()
                giveFeedback = nextStartTime != startTime
                startTime = nextStartTime
            }
            if (updateType == WheelUpdateType.EditEndTime) {
                val nextEndTime = (endTime + diff).roundToClosestMinute()
                giveFeedback = nextEndTime != endTime
                endTime = nextEndTime
            }
        }

        if (giveFeedback) {
            hapticFeedbackProvider?.performTickEffect()
        }
    }

    enum class WheelUpdateType {
        EditStartTime,
        EditEndTime,
        EditBothAtOnce,
        None
    }
}
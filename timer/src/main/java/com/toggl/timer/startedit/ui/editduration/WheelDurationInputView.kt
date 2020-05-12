package com.toggl.timer.startedit.ui.editduration

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.toggl.timer.R
import com.toggl.timer.startedit.ui.editduration.model.DurationFieldInfo
import com.toggl.timer.startedit.util.asDurationString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import org.threeten.bp.Duration

@ExperimentalCoroutinesApi
@FlowPreview
class WheelDurationInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr), TextWatcher, View.OnTouchListener {
    @ColorInt
    private val fadedTextColor: Int
    private val sansSerifMediumTypefaceSpan = TypefaceSpan("sans-serif-medium")
    private val fadedTextColorTypeSpan: ForegroundColorSpan

    private var originalDuration: Duration = Duration.ZERO
    private var duration: Duration = Duration.ZERO
    private var input: DurationFieldInfo = DurationFieldInfo()
    private var isEditing = false

    private val durationChannel = ConflatedBroadcastChannel<Duration>()
    val durationFlow = durationChannel.asFlow()

    init {
        val defaultFadedTextColor = ContextCompat.getColor(context, R.color.default_wheel_duration_input_faded_text_color)
        context.theme.obtainStyledAttributes(attrs, R.styleable.WheelDurationInputView, 0, 0).apply {
            try {
                fadedTextColor = getColor(R.styleable.WheelDurationInputView_fadedTextColor, defaultFadedTextColor)
                fadedTextColorTypeSpan = ForegroundColorSpan(fadedTextColor)
            } finally {
                recycle()
            }
        }
    }

    init {
        isFocusable = false
        addTextChangedListener(this)
        setOnTouchListener(this)
        transformationMethod = null
        filters = arrayOf(WheelDurationInputFilter(::onDigitEntered, ::onDeletionDetected))
    }

    override fun onEditorAction(actionCode: Int) {
        if (actionCode == EditorInfo.IME_ACTION_DONE || actionCode == EditorInfo.IME_ACTION_NEXT)
            removeFocus()
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER)
            removeFocus()

        return super.onKeyPreIme(keyCode, event)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        moveCursorToTheEnd()
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (focused) {
            originalDuration = duration
            input = DurationFieldInfo()
            setText(input.toString())
            moveCursorToTheEnd()
        } else {
            applyDurationIfBeingEdited()
        }

        isEditing = focused

        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    override fun afterTextChanged(editable: Editable) {
        val text = if (isEditing) input.toString() else editable.toString()

        if (this.text.toString() == text) {
            applyFormatting(text, editable)
            moveCursorToTheEnd()
            return
        }

        setText(text)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        moveCursorToTheEnd()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        isFocusableInTouchMode = true
        return false
    }

    fun setDuration(duration: Duration) {
        this.duration = duration
        input = DurationFieldInfo.fromDuration(duration)
        setText(duration.asDurationString())
    }

    private fun applyDurationIfBeingEdited() {
        if (!isEditing) return

        val actualDuration = if (input.isEmpty()) originalDuration else input.toDuration()
        setText(actualDuration.asDurationString())
        durationChannel.offer(actualDuration)
        isFocusable = false
    }

    private fun onDeletionDetected() {
        val nextInput = input.pop()
        updateDuration(nextInput)
    }

    private fun onDigitEntered(digit: Int) {
        val nextInput = input.push(digit)
        updateDuration(nextInput)
    }

    private fun updateDuration(nextInput: DurationFieldInfo) {
        if (nextInput == input) return

        input = nextInput
        duration = input.toDuration()
        setText(input.toString())
    }

    private fun getFormattingSplitPoint(text: String): Int {
        val colonCount = text.count { it == ':' }

        // Text in edit mode always has one colon
        if (colonCount == 1)
            return text.takeWhile { it == '0' || it == ':' }.count()

        // Text in display mode always has two colons
        if (colonCount == 2)
            return text.lastIndexOf(':')

        return 0
    }

    private fun applyFormatting(text: String, editable: Editable) {
        val splitPoint = getFormattingSplitPoint(text)

        editable.clearSpans()

        if (isEditing) {
            editable.setSpan(sansSerifMediumTypefaceSpan, 0, editable.length, SPAN_INCLUSIVE_INCLUSIVE)
            editable.setSpan(fadedTextColorTypeSpan, 0, splitPoint, SPAN_INCLUSIVE_INCLUSIVE)
        } else {
            editable.setSpan(sansSerifMediumTypefaceSpan, 0, splitPoint, SPAN_INCLUSIVE_INCLUSIVE)
        }
    }

    private fun moveCursorToTheEnd() {
        text?.run {
            if (length == selectionEnd && length == selectionStart)
                return

            setSelection(length)
        }
    }

    private fun removeFocus() {
        val inputMethodManager = context.getSystemService<InputMethodManager>()
        post {
            clearFocus()
            inputMethodManager?.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}
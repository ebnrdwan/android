package com.toggl.common

import android.view.MotionEvent
import android.widget.CompoundButton

fun CompoundButton.addInterceptingOnClickListener(action: () -> Unit) {
    this.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            action()
            true
        } else false
    }
}
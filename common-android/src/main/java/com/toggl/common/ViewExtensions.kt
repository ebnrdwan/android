package com.toggl.common

import android.view.MotionEvent
import android.view.View

fun View.addInterceptingOnClickListener(action: () -> Unit) {
    this.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            action()
            true
        } else false
    }
}
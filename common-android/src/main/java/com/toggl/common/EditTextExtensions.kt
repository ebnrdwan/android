package com.toggl.common

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged

fun EditText.setSafeText(newText: String) {
    val lastDispatchedDescription = this.tag as? String
    if (lastDispatchedDescription == null || lastDispatchedDescription == newText) {
        if (newText != this.text.toString()) {
            this.setText(newText)
            this.setSelection(newText.length)
        }
        this.tag = null
    }
}

fun EditText.doSafeAfterTextChanged(action: (text: Editable?) -> Unit): TextWatcher {
    return this.doAfterTextChanged {
        this.tag = it.toString()
        action(it)
    }
}
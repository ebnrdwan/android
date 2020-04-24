package com.toggl.timer.extensions

import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

fun OffsetDateTime.formatForDisplayingTime() = this.format(timeFormatter)
fun OffsetDateTime.formatForDisplayingDate() = this.format(dateFormatter)

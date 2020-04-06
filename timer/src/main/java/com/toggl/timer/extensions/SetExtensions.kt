package com.toggl.timer.extensions

fun <T> Set<T>.containsExactly(elements: Collection<T>) = this.containsAll(elements) && this.size == elements.size

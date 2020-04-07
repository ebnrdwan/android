package com.toggl.timer.extensions

fun <T> Set<T>.containsExactly(elements: Collection<T>) = this.size == elements.size && this.containsAll(elements)

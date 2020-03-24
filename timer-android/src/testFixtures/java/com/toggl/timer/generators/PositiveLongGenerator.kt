package com.toggl.timer.generators

import io.kotlintest.properties.Gen

fun Gen.Companion.positiveLong(): Gen<Long> =
    long().filter { it > 0 }
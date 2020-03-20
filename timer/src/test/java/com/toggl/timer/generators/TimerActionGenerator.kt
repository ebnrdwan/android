package com.toggl.timer.generators

import com.toggl.timer.common.domain.TimerAction
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.properties.Gen
import io.kotlintest.specs.FreeSpec
import kotlin.random.Random

fun Gen.Companion.timerAction(): Gen<TimerAction> =
    object : Gen<TimerAction> {

        private val allValues = sequence {
            startTimeEntryAction()
                .constants()
                .map(TimerAction::StartTimeEntry)
                .also { yieldAll(it) }

            timeEntriesLogAction()
                .constants()
                .map(TimerAction::TimeEntriesLog)
                .also { yieldAll(it) }
        }.toList()

        override fun constants(): Iterable<TimerAction> = allValues

        override fun random(): Sequence<TimerAction> = generateSequence {
            allValues[Random.nextInt(allValues.size)]
        }
    }

class TimerActionGeneratorTests : FreeSpec({
    "The timer action generator's constants are exhaustive" - {
        val constants = Gen.timerAction().constants().map { it::class.qualifiedName }.distinct()
        val possibleActionTypes = TimerAction::class.sealedSubclasses.map { it.qualifiedName }
        constants shouldContainAll possibleActionTypes
    }
})
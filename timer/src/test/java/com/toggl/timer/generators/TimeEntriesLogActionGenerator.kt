package com.toggl.timer.generators

import com.toggl.models.common.SwipeDirection
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.log.domain.TimeEntriesLogAction
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.properties.Gen
import io.kotlintest.specs.FreeSpec
import io.kotlintest.specs.WordSpec
import kotlin.random.Random

fun Gen.Companion.timeEntriesLogAction() : Gen<TimeEntriesLogAction> =
    object : Gen<TimeEntriesLogAction> {
        private val allValues = listOf(
            TimeEntriesLogAction.ContinueButtonTapped(0),
            TimeEntriesLogAction.TimeEntryTapped(0),
            TimeEntriesLogAction.TimeEntrySwiped(0, SwipeDirection.Right),
            TimeEntriesLogAction.TimeEntryGroupTapped(listOf()),
            TimeEntriesLogAction.TimeEntryGroupSwiped(listOf(), SwipeDirection.Right),
            TimeEntriesLogAction.TimeEntriesDeleted(hashSetOf()),
            TimeEntriesLogAction.TimeEntryStarted(createTimeEntry(0), null)
        )

        override fun constants(): Iterable<TimeEntriesLogAction> = allValues

        override fun random(): Sequence<TimeEntriesLogAction> = generateSequence {
            allValues[Random.nextInt(allValues.size)]
        }
    }


class TimeEntriesLogActionGeneratorTests : FreeSpec({
    "The time entries log action generator's constants are exhaustive" - {
        val constants = Gen.timeEntriesLogAction().constants().map { it::class.qualifiedName }.distinct()
        val possibleActionTypes = TimeEntriesLogAction::class.sealedSubclasses.map { it.qualifiedName }
        constants shouldContainAll possibleActionTypes
    }
})
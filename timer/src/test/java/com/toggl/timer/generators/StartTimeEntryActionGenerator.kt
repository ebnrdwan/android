package com.toggl.timer.generators

import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.start.domain.StartTimeEntryAction
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.properties.Gen
import io.kotlintest.specs.FreeSpec
import io.kotlintest.specs.WordSpec
import kotlin.random.Random

fun Gen.Companion.startTimeEntryAction() : Gen<StartTimeEntryAction> =
    object : Gen<StartTimeEntryAction> {
        private val allValues = listOf(
            StartTimeEntryAction.StartTimeEntryButtonTapped,
            StartTimeEntryAction.StopTimeEntryButtonTapped,
            StartTimeEntryAction.ToggleBillable,
            StartTimeEntryAction.TimeEntryDescriptionChanged(""),
            StartTimeEntryAction.TimeEntryUpdated(0, createTimeEntry(0)),
            StartTimeEntryAction.TimeEntryStarted(createTimeEntry(0), null)
        )

        override fun constants(): Iterable<StartTimeEntryAction> = allValues

        override fun random(): Sequence<StartTimeEntryAction> = generateSequence {
            allValues[Random.nextInt(allValues.size)]
        }
    }

class StartTimeEntryActionGeneratorTests : FreeSpec({
    "The start time entry action generator's constants are exhaustive" - {
        val constants = Gen.startTimeEntryAction().constants().map { it::class.qualifiedName }.distinct()
        val possibleActionTypes = StartTimeEntryAction::class.sealedSubclasses.map { it.qualifiedName }
        constants shouldContainAll possibleActionTypes
    }
})
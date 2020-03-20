package com.toggl.domain.generators

import com.toggl.domain.AppAction
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.User
import com.toggl.models.domain.Workspace
import com.toggl.models.validation.ApiToken
import com.toggl.onboarding.domain.actions.OnboardingAction
import com.toggl.timer.generators.timerAction
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.properties.Gen
import io.kotlintest.specs.FreeSpec
import kotlin.random.Random

fun Gen.Companion.onboardingAction(): Gen<OnboardingAction> =
    object : Gen<OnboardingAction> {
        private val allValues = listOf(
            OnboardingAction.LoginTapped,
            OnboardingAction.SetUser(User(ApiToken.from(""))),
            OnboardingAction.SetUserError(Exception()),
            OnboardingAction.EmailEntered(""),
            OnboardingAction.PasswordEntered("")
        )

        override fun constants(): Iterable<OnboardingAction> = allValues

        override fun random(): Sequence<OnboardingAction> = generateSequence {
            allValues[Random.nextInt(allValues.size)]
        }
    }

fun Gen.Companion.appAction(): Gen<AppAction> =
    object : Gen<AppAction> {
        private val allValues = sequence {
            yield(AppAction.Load)
            yield(AppAction.WorkspacesLoaded(listOf<Workspace>()))
            yield(AppAction.TimeEntriesLoaded(listOf<TimeEntry>()))

            timerAction()
                .constants()
                .map(AppAction::Timer)
                .also { yieldAll(it) }

            onboardingAction()
                .constants()
                .map(AppAction::Onboarding)
                .also { yieldAll(it) }
        }.toList()

        override fun constants(): Iterable<AppAction> = allValues

        override fun random(): Sequence<AppAction> = generateSequence {
            allValues[Random.nextInt(allValues.size)]
        }
    }

class AppActionGeneratorTests : FreeSpec({
    "The app action generator's constants are exhaustive" - {
        val constants = Gen.appAction().constants().map { it::class.qualifiedName }.distinct()
        val possibleActionTypes = AppAction::class.sealedSubclasses.map { it.qualifiedName }
        constants shouldContainAll possibleActionTypes
    }
})
package com.toggl.timer.log.domain

import com.toggl.environment.services.time.TimeService
import com.toggl.models.domain.Client
import com.toggl.models.domain.Project
import com.toggl.models.domain.TimeEntry
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.generators.timeEntries
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.numerics.shouldBeExactly
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.matchers.types.shouldNotBeTypeOf
import io.kotlintest.properties.Gen
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.every
import io.mockk.mockk
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.Year
import org.threeten.bp.format.DateTimeFormatter

class TimeEntriesLogSelectorTests : FreeSpec({

    val todayString = "Today"
    val yesterdayString = "Yesterday"
    val timeService = mockk<TimeService>()
    val today = OffsetDateTime.now()
    val localToday = today.toLocalDate()
    val yesterday = localToday.minusDays(1)

    val selectorToTest: (Map<Long, TimeEntry>, Map<Long, Project>, Map<Long, Client>) -> List<TimeEntryViewModel> =
        { timeEntries, projects, clients ->
            timeEntriesLogSelector(
                timeEntries,
                projects,
                clients,
                timeService,
                todayString,
                yesterdayString,
                false,
                setOf()
            )
        }

    val groupSelectorToTest: (Map<Long, TimeEntry>, Map<Long, Project>, Map<Long, Client>, Set<Long>) -> List<TimeEntryViewModel> =
        { timeEntries, projects, clients, expandedGroups ->
            timeEntriesLogSelector(
                timeEntries,
                projects,
                clients,
                timeService,
                todayString,
                yesterdayString,
                true,
                expandedGroups
            )
        }

    every { timeService.now() } returns today

    /*
     *  Test Data
     */
    val projectsMap = mapOf(
        1L to Project(
            id = 1,
            name = "First Project",
            color = "",
            active = true,
            isPrivate = true,
            billable = true,
            workspaceId = 1,
            clientId = 1
        ),
        2L to Project(
            id = 2,
            name = "Second Project",
            color = "",
            active = true,
            isPrivate = true,
            billable = true,
            workspaceId = 1,
            clientId = 2
        )
    )

    val clientsMap = mapOf(
        1L to Client(
            id = 1,
            name = "Client numero 1",
            workspaceId = 1
        ),
        2L to Client(
            id = 2,
            name = "Cliento 2",
            workspaceId = 1
        )
    )

    val timeEntries = (0L..10L).flatMap { daysInThePast ->
        val date = today.minusDays(daysInThePast)
        (0L..10L).map { id ->
            createTimeEntry(
                id = daysInThePast + id,
                description = "Time entry $id",
                startTime = date,
                duration = Duration.ofMinutes(30)
            )
        }
    }

    val shortDuration = Duration.ofMinutes(30)

    fun List<TimeEntry>.mapToYesterday() = this.map { it.copy(id = it.id * 100, startTime = it.startTime.minusDays(1)) }

    val singleItem = listOf(
        createTimeEntry(
            id = 1,
            description = "S",
            startTime = today,
            duration = shortDuration
        )
    )

    val yesterdaySingleItem = singleItem.mapToYesterday()

    val groupA = (2L..5L).map { id ->
        createTimeEntry(
            id = id,
            description = "A",
            startTime = today,
            duration = shortDuration.plusMinutes(id)
        )
    }

    val yesterdayGroupA = groupA.mapToYesterday()

    val groupB = (6L..9L).map { id ->
        createTimeEntry(
            id = id,
            description = "B",
            startTime = today,
            duration = shortDuration.plusMinutes(id)
        )
    }

    val yesterdayGroupB = groupB.mapToYesterday()

    val twoProjects = listOf(
        createTimeEntry(
            id = 10,
            description = "B",
            startTime = today,
            duration = shortDuration.plusMinutes(10),
            projectId = 1
        ),
        createTimeEntry(
            id = 11,
            description = "B",
            startTime = today,
            duration = shortDuration.plusMinutes(11),
            projectId = 2
        )
    )

    val yesterdayTwoProjects = twoProjects.mapToYesterday()

    val differentDescriptions = listOf(
        createTimeEntry(
            id = 12,
            description = "C1",
            startTime = today,
            duration = shortDuration.plusMinutes(12)
        ),
        createTimeEntry(
            id = 13,
            description = "C1",
            startTime = today,
            duration = shortDuration.plusMinutes(13)
        ),
        createTimeEntry(
            id = 14,
            description = "C2",
            startTime = today,
            duration = shortDuration.plusMinutes(14)
        )
    )

    val yesterdayDifferentDescriptions = differentDescriptions.mapToYesterday()

    val longDuration = listOf(
        createTimeEntry(
            id = 15,
            description = "D1",
            startTime = today,
            duration = Duration.ofHours(1)
        ),
        createTimeEntry(
            id = 16,
            description = "D1",
            startTime = today,
            duration = Duration.ofHours(2)
        ),
        createTimeEntry(
            id = 17,
            description = "D3",
            startTime = today,
            duration = Duration.ofHours(3)
        )
    )

    val yesterdayLongDuration = longDuration.mapToYesterday()

    val todayEntries = (
        singleItem +
            groupA +
            groupB +
            twoProjects +
            differentDescriptions +
            longDuration
        )

    val yesterdayEntries = (
        yesterdaySingleItem +
            yesterdayGroupA +
            yesterdayGroupB +
            yesterdayTwoProjects +
            yesterdayDifferentDescriptions +
            yesterdayLongDuration
        )

    val similarTimeEntries: List<TimeEntry> = yesterdayEntries + todayEntries

    val timeEntriesMap = timeEntries.associateBy { it.id }
    val similarTimeEntriesMap = similarTimeEntries.associateBy { it.id }

    /*
     *  Expected Data
     */
    val expectedTodayGroupedTimeEntries: List<TimeEntryViewModel> = listOf(
        singleItem.first().toFlatTimeEntryViewModel(projectsMap, clientsMap),
        groupA.toTimeEntryGroupViewModel(groupA.first().similarityHashCode(), false, projectsMap, clientsMap),
        groupB.toTimeEntryGroupViewModel(groupB.first().similarityHashCode(), false, projectsMap, clientsMap),
        twoProjects.first().toFlatTimeEntryViewModel(projectsMap, clientsMap),
        twoProjects[1].toFlatTimeEntryViewModel(projectsMap, clientsMap),
        differentDescriptions.dropLast(1).toTimeEntryGroupViewModel(differentDescriptions.first().similarityHashCode(), false, projectsMap, clientsMap),
        differentDescriptions.last().toFlatTimeEntryViewModel(projectsMap, clientsMap),
        longDuration.dropLast(1).toTimeEntryGroupViewModel(longDuration.first().similarityHashCode(), false, projectsMap, clientsMap),
        longDuration.last().toFlatTimeEntryViewModel(projectsMap, clientsMap)
    )

    val expectedYesterdayGroupedTimeEntries = listOf(
        singleItem.mapToYesterday().first().toFlatTimeEntryViewModel(projectsMap, clientsMap),
        groupA.mapToYesterday().toTimeEntryGroupViewModel(groupA.mapToYesterday().first().similarityHashCode(), false, projectsMap, clientsMap),
        groupB.mapToYesterday().toTimeEntryGroupViewModel(groupB.mapToYesterday().first().similarityHashCode(), false, projectsMap, clientsMap),
        twoProjects.mapToYesterday().first().toFlatTimeEntryViewModel(projectsMap, clientsMap),
        twoProjects.mapToYesterday()[1].toFlatTimeEntryViewModel(projectsMap, clientsMap),
        differentDescriptions.mapToYesterday().dropLast(1).toTimeEntryGroupViewModel(differentDescriptions.mapToYesterday().first().similarityHashCode(), false, projectsMap, clientsMap),
        differentDescriptions.mapToYesterday().last().toFlatTimeEntryViewModel(projectsMap, clientsMap),
        longDuration.mapToYesterday().dropLast(1).toTimeEntryGroupViewModel(longDuration.mapToYesterday().first().similarityHashCode(), false, projectsMap, clientsMap),
        longDuration.mapToYesterday().last().toFlatTimeEntryViewModel(projectsMap, clientsMap)
    )

    val expectedGroupedTimeEntries: List<TimeEntryViewModel> = listOf(
        DayHeaderViewModel(
            dayTitle = todayString,
            totalDuration = todayEntries.totalDuration()
        )
    ) + expectedTodayGroupedTimeEntries + listOf(
        DayHeaderViewModel(
            dayTitle = yesterdayString,
            totalDuration = yesterdayEntries.totalDuration()
        )
    ) + expectedYesterdayGroupedTimeEntries

    "The TimeEntriesLogSelector" - {
        "does not" - {
            "add running time entries to the groups" - {
                val timeEntriesButAllRunning = timeEntries.map { it.copy(duration = null) }
                val groupedTimeEntries = selectorToTest(
                    timeEntriesButAllRunning.associateBy { it.id },
                    projectsMap,
                    clientsMap
                )

                groupedTimeEntries.size shouldBe 0
            }

            "create headers for days that have no time entries" - {

                val groupedTimeEntries = selectorToTest(
                    timeEntriesMap,
                    projectsMap,
                    clientsMap
                )

                val headers =
                    groupedTimeEntries.filterIsInstance<DayHeaderViewModel>()
                        .map { it.dayTitle }

                val flatTimeEntryHeaders =
                    groupedTimeEntries
                        .asSequence()
                        .filterIsInstance<FlatTimeEntryViewModel>()
                        .map { it.startTime.toLocalDate() }
                        .distinct()
                        .map {
                            when (it) {
                                localToday -> todayString
                                yesterday -> yesterdayString
                                else -> it.format(
                                    DateTimeFormatter.ofPattern("eee, dd MMM")
                                )
                            }
                        }.distinct().toList()

                headers shouldContainExactlyInAnyOrder flatTimeEntryHeaders
            }
            "group similar time entries when grouping is disabled" - {

                val notGroupedTimeEntries = selectorToTest(
                    similarTimeEntriesMap,
                    projectsMap,
                    clientsMap
                )

                notGroupedTimeEntries.forEach {
                    it.shouldNotBeTypeOf<TimeEntryGroupViewModel>()
                }
            }
        }

        "the header titles are formatted" - {

            "as the localized string for Today when the header is for the current day" - {
                val groupedTimeEntries = selectorToTest(
                    timeEntriesMap,
                    projectsMap,
                    clientsMap
                )

                val headerTitles =
                    groupedTimeEntries.filterIsInstance<DayHeaderViewModel>()
                        .map { it.dayTitle }

                headerTitles shouldContain todayString
            }

            "as the localized string for Today when the header is for the previous day" - {
                val groupedTimeEntries = selectorToTest(
                    timeEntriesMap,
                    projectsMap,
                    clientsMap
                )

                val headerTitles =
                    groupedTimeEntries.filterIsInstance<DayHeaderViewModel>()
                        .map { it.dayTitle }

                headerTitles shouldContain yesterdayString
            }

            "using eee, dd MMM for all other days" - {
                val formatter = DateTimeFormatter.ofPattern("eee, dd MMM")

                val groupedTimeEntries = selectorToTest(
                    timeEntriesMap,
                    projectsMap,
                    clientsMap
                )

                val headerTitles =
                    groupedTimeEntries.filterIsInstance<DayHeaderViewModel>()
                        .map { it.dayTitle }
                        .filter { it != todayString && it != yesterdayString }
                        .distinct()

                val formattedDates =
                    groupedTimeEntries
                        .asSequence()
                        .filterIsInstance<FlatTimeEntryViewModel>()
                        .map { it.startTime.toLocalDate() }
                        .distinct()
                        .sortedDescending()
                        .drop(2)
                        .map { it.format(formatter) }
                        .toList()

                headerTitles shouldContainExactlyInAnyOrder formattedDates
            }
        }

        "groups similar time entries" - {
            val groupedTimeEntries = groupSelectorToTest(
                similarTimeEntriesMap,
                projectsMap,
                clientsMap,
                setOf()
            )
            groupedTimeEntries shouldBe expectedGroupedTimeEntries
        }

        "groups random similar time entries from the same year so that there is" - {
            val y = Year.of(2020)
            val randomSimilarInOneYear = Gen.timeEntries(
                description = "constant",
                year = y
            )
                .random()
                .take(10000)
                .associateBy { it.id }

            val groupedTimeEntries = groupSelectorToTest(
                randomSimilarInOneYear,
                projectsMap,
                clientsMap,
                setOf()
            )
            val timeEntryViewModels = groupedTimeEntries.count { it is TimeEntryGroupViewModel || it is FlatTimeEntryViewModel }
            val dayHeaderViewModels = groupedTimeEntries.count { it is DayHeaderViewModel }

            "no more day headers than days in a year" - {
                dayHeaderViewModels shouldBeLessThanOrEqual y.length()
            }

            "no more time entries/groups than days in a year" - {
                timeEntryViewModels shouldBeLessThanOrEqual y.length()
            }

            "the same number of time entries/groups as day headers" - {
                timeEntryViewModels shouldBe dayHeaderViewModels
            }
        }

        "handles group expanding so that" - {
            val y = Year.of(2020)
            val randomSimilarInOneYear = Gen.timeEntries(
                description = "constant",
                year = y
            )
                .random()
                .take(10000)
                .toList()

            val randomExpandedGroups = randomSimilarInOneYear
                .shuffled()
                .take(1000)
                .map(TimeEntry::similarityHashCode)
                .toSet()

            val groupedTimeEntries = groupSelectorToTest(
                randomSimilarInOneYear.associateBy { it.id },
                projectsMap,
                clientsMap,
                randomExpandedGroups
            )

            val allGroups = groupedTimeEntries.filterIsInstance<TimeEntryGroupViewModel>()

            "groups that should be expanded are expanded" - {
                val expandedGroups = allGroups.filter { randomExpandedGroups.contains(it.groupId) }
                expandedGroups.forEach {
                    it.isExpanded.shouldBeTrue()
                }
            }

            "the correct number of individual FlatTimeEntryViewModel is present for every expanded group" - {
                val expandedGroups = allGroups.filter { randomExpandedGroups.contains(it.groupId) }
                expandedGroups.forEach { group ->
                    val groupTimeEntries = groupedTimeEntries
                        .filterIsInstance<FlatTimeEntryViewModel>()
                        .filter { te -> group.timeEntryIds.contains(te.id) }
                    groupTimeEntries.size shouldBeExactly group.timeEntryIds.size
                }
            }

            "groups that shouldn't be expanded are not expanded" - {
                val nonExpandedGroups = allGroups.filter { !randomExpandedGroups.contains(it.groupId) }
                nonExpandedGroups.forEach {
                    it.isExpanded.shouldBeFalse()
                }
            }

            "there shouldn't be any individual FlatTimeEntryViewModel from unexpanded groups" - {
                val nonExpandedGroups = allGroups.filter { !randomExpandedGroups.contains(it.groupId) }
                nonExpandedGroups.forEach { group ->
                    val groupTimeEntries = groupedTimeEntries
                        .filterIsInstance<FlatTimeEntryViewModel>()
                        .filter { te -> group.timeEntryIds.contains(te.id) }
                    groupTimeEntries.shouldBeEmpty()
                }
            }
        }
    }
})
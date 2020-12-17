package uk.co.ceilingcat.kalendarapi

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import arrow.core.flatMap
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.co.ceilingcat.kalendarapi.CalendarApiError.ResultProcessingError
import uk.co.ceilingcat.kalendarapi.CalendarApiError.ScriptLoadingError
import uk.co.ceilingcat.kalendarapi.CalendarApiError.ScriptProcessingError
import uk.co.ceilingcat.kalendarapi.CalendarApiError.TemporaryFileCreationError
import uk.co.ceilingcat.kalendarapi.ScriptTemplate.CREATE_CALENDAR
import uk.co.ceilingcat.kalendarapi.ScriptTemplate.CREATE_EVENT
import uk.co.ceilingcat.kalendarapi.ScriptTemplate.DELETE_EVENT_BY_UID
import uk.co.ceilingcat.kalendarapi.ScriptTemplate.FIND_EVENTS_BY_SUMMARY_PREFIX
import uk.co.ceilingcat.kalendarapi.ScriptTemplate.FIND_EVENT_BY_SUMMARY
import uk.co.ceilingcat.kalendarapi.ScriptTemplate.LANGUAGE.APPLESCRIPT
import uk.co.ceilingcat.kalendarapi.ScriptTemplate.LANGUAGE.JAVASCRIPT
import uk.co.ceilingcat.kalendarapi.ScriptTemplate.RETRIEVE_CALENDARS
import uk.co.ceilingcat.kalendarapi.ScriptTemplate.RETRIEVE_CALENDAR_BY_NAME
import uk.co.ceilingcat.kalendarapi.ScriptTemplate.RETRIEVE_EVENT_BY_UID
import uk.co.ceilingcat.kalendarapi.ScriptTokens.CALENDAR
import uk.co.ceilingcat.kalendarapi.ScriptTokens.CALENDAR_NAME
import uk.co.ceilingcat.kalendarapi.ScriptTokens.COMMON
import uk.co.ceilingcat.kalendarapi.ScriptTokens.EVENT
import uk.co.ceilingcat.kalendarapi.ScriptTokens.EVENT_UID
import uk.co.ceilingcat.kalendarapi.ScriptTokens.SUMMARY
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempFile

/**
 * The API proper, obtain new instances using `createKalendarApi(...)`.
 *
 * Calendars are referenced by name. Operations assume there is a single calendar with a given name and, if not, will generally fail.
 *
 * All functions and properties return `Arrow-kt`'s `Either<CalendarApiError, T>`, rather than throwing exceptions.
 * For more information about the `Either` class, refer to the [documentation](https://arrow-kt.io/docs/apidocs/arrow-core-data/arrow.core/-either/).
 *
 * @see uk.co.ceilingcat.kalendarapi.createKalendarApi
 */
interface KalendarApi {

    /**
     * Retrieve all calendars.
     */
    val retrieveCalendars: Either<CalendarApiError, Calendars>

    /**
     * Create a new calendar.
     *
     * @param calendar the new calendar's data
     * @return the new calendar's identifier if, no error occurred
     */
    fun createCalendar(calendar: NewCalendar): Either<CalendarApiError, Unit>

    /**
     * Retrieve a calendar.
     *
     * @param calendarName the name of the calendar to retrieve
     */
    fun retrieveCalendar(calendarName: CalendarName): Either<CalendarApiError, Calendar>

    /**
     * Create a new event.
     *
     * @param calendarName the name of the calendar, in which to create
     * @param event the new event's data
     * @return the created event's identifier if, no error occurred
     */
    fun createEvent(calendarName: CalendarName, event: NewEvent): Either<CalendarApiError, Identifier>

    /**
     * Delete an event.
     *
     * @param calendarName the name of the calendar, from which to delete
     * @param eventIdentifier the targeted event's identifier
     * @return the deleted event's identifier if, no error occurred
     */
    fun deleteEvent(calendarName: CalendarName, eventIdentifier: Identifier): Either<CalendarApiError, Identifier>

    /**
     * Retrieve an event.
     *
     * @param calendarName the name of the calendar, to retrieve from
     * @param eventIdentifier the new event's identifier
     */
    fun retrieveEvent(calendarName: CalendarName, eventIdentifier: Identifier): Either<CalendarApiError, Event>

    /**
     * Retrieve events.
     *
     * @param calendarName the name of the calendar, to retrieve from
     * @param summary the summary that the retrieved events must have
     */
    fun retrieveEventsBySummary(calendarName: CalendarName, summary: EventSummary): Either<CalendarApiError, Events>

    /**
     * Retrieve events.
     *
     * @param calendarName the name of the calendar, to retrieve from
     * @param summaryPrefix the summary-prefix the events retrieved must have
     */
    fun retrieveEventsBySummaryPrefix(
        calendarName: CalendarName,
        summaryPrefix: EventSummary
    ): Either<CalendarApiError, Events>
}

/**
 * The errors that `KalendarApi` may return.
 */
sealed class CalendarApiError(cause: Throwable? = null) : Throwable(cause) {

    /**
     * Signals that the api was not able to create a temporary file.
     *
     * @param cause an optional `Throwable` that caused this error to be signalled
     */
    data class TemporaryFileCreationError(override val cause: Throwable) : CalendarApiError(cause)

    /**
     * Signals that the api was not able to load a script.
     *
     * @param cause an optional `Throwable` that caused this error to be signalled
     */
    data class ScriptLoadingError(override val cause: Throwable? = null) : CalendarApiError(cause)

    /**
     * Signals that the api was not able to process a script.
     *
     * @param cause an optional `Throwable` that caused this error to be signalled
     */
    data class ScriptProcessingError(override val cause: Throwable? = null) : CalendarApiError(cause)

    /**
     * Signals that the api was not able to process the result from a script's execution.
     *
     * @param cause the `Throwable` that caused this error to be signalled
     */
    data class ResultProcessingError(override val cause: Throwable) : CalendarApiError(cause)
}

/**
 * Create a `KalendarApi` with the given maximum process duration value.
 *
 * Use `createDuration(...)` values for the 'maximumProcessDuration' parameter.
 *
 * @param maximumProcessDuration the maximum duration to wait whilst running any function
 *
 * @see uk.co.ceilingcat.kalendarapi.createDuration
 */
@ExperimentalPathApi
fun createKalendarApi(maximumProcessDuration: Duration): KalendarApi =
    object : KalendarApi {
        private val javaScriptProcessLauncher: ProcessLauncher =
            OsaScriptProcessLauncher(CalendarProcessCommandTerms(listOf("osascript", "-l", "JavaScript", "-e")))
        private val appleScriptProcessLauncher: ProcessLauncher =
            OsaScriptProcessLauncher(CalendarProcessCommandTerms(listOf("osascript", "-se", "-e")))

        override val retrieveCalendars: Either<CalendarApiError, Calendars>
            get() = executeScript(RETRIEVE_CALENDARS) { calendarsJson -> Json.decodeFromString(calendarsJson) }

        override fun retrieveCalendar(calendarName: CalendarName): Either<CalendarApiError, Calendar> =
            executeScript(
                RETRIEVE_CALENDAR_BY_NAME,
                mapOf(CALENDAR_NAME to calendarName.text)
            ) { calendarJson ->
                Json.decodeFromString(calendarJson)
            }

        override fun createCalendar(calendar: NewCalendar): Either<CalendarApiError, Unit> =
            executeScript(CREATE_CALENDAR, mapOf(CALENDAR to Json.encodeToString(calendar))) { }

        override fun createEvent(
            calendarName: CalendarName,
            event: NewEvent
        ): Either<CalendarApiError, Identifier> =
            executeScript(
                CREATE_EVENT,
                mapOf(
                    CALENDAR_NAME to calendarName.text,
                    EVENT to Json.encodeToString(event)
                )
            ) { uid -> Identifier(uid) }

        override fun deleteEvent(
            calendarName: CalendarName,
            eventIdentifier: Identifier
        ): Either<CalendarApiError, Identifier> =
            executeScript(
                DELETE_EVENT_BY_UID,
                mapOf(
                    CALENDAR_NAME to calendarName.text,
                    EVENT_UID to eventIdentifier.text
                )
            ) { deletedUid ->
                Identifier(deletedUid)
            }

        override fun retrieveEvent(
            calendarName: CalendarName,
            eventIdentifier: Identifier
        ): Either<CalendarApiError, Event> =
            executeScript(
                RETRIEVE_EVENT_BY_UID,
                mapOf(
                    CALENDAR_NAME to calendarName.text,
                    EVENT_UID to eventIdentifier.text
                )
            ) { eventJson ->
                Json.decodeFromString(eventJson)
            }

        override fun retrieveEventsBySummary(
            calendarName: CalendarName,
            summary: EventSummary
        ): Either<CalendarApiError, Events> =
            executeScript(
                FIND_EVENT_BY_SUMMARY,
                mapOf(
                    CALENDAR_NAME to calendarName.text,
                    SUMMARY to summary.text
                )
            ) { eventsJson -> Json.decodeFromString(eventsJson) }

        override fun retrieveEventsBySummaryPrefix(calendarName: CalendarName, summaryPrefix: EventSummary):
            Either<CalendarApiError, Events> = executeScript(
                FIND_EVENTS_BY_SUMMARY_PREFIX,
                mapOf(
                    CALENDAR_NAME to calendarName.text,
                    SUMMARY to summaryPrefix.text
                )
            ) { eventsJson -> Json.decodeFromString(eventsJson) }

        private fun withFileOutputWriting(f: (outputFile: File) -> CalendarApiError?): Either<CalendarApiError, String> =
            try {
                createTempFile().toFile().let {
                    when (val result = f(it)) {
                        null -> right(it.readText().trim())
                        else -> left(result)
                    }
                }
            } catch (uoe: UnsupportedOperationException) {
                left(TemporaryFileCreationError(uoe))
            }

        private fun processLauncherFor(template: ScriptTemplate) =
            if (template.language == JAVASCRIPT) javaScriptProcessLauncher else appleScriptProcessLauncher

        private fun <T> executeScript(
            template: ScriptTemplate,
            tokensAndReplacements: Map<ScriptTokens, String> = emptyMap(),
            constructor: (String) -> T
        ): Either<CalendarApiError, T> = withFileOutputWriting { outputFile ->
            template
                .interpolate(tokensAndReplacements)
                .map { interpolatedScript ->
                    when (
                        val error =
                            processLauncherFor(template).spawn(interpolatedScript, maximumProcessDuration, outputFile)
                    ) {
                        null -> null
                        else -> ScriptProcessingError(error)
                    }
                }.fold({ it }) { it }
        }.flatMap { scriptOutput ->
            try {
                right(constructor(scriptOutput))
            } catch (t: Throwable) {
                left(ResultProcessingError(t))
            }
        }
    }

private enum class ScriptTemplate(relativeScriptFile: File, val language: LANGUAGE = JAVASCRIPT) {
    CREATE_CALENDAR(File("calendars/create.js")),
    RETRIEVE_CALENDARS(File("calendars/retrieve.js")),
    RETRIEVE_CALENDAR_BY_NAME(File("calendars/retrieveByName.js")),

    CREATE_EVENT(File("events/create.js")),
    DELETE_EVENT_BY_UID(File("events/deleteByUid.scpt"), APPLESCRIPT),
    RETRIEVE_EVENT_BY_UID(File("events/retrieveByUid.js")),
    FIND_EVENT_BY_SUMMARY(File("events/retrieveBySummary.js")),
    FIND_EVENTS_BY_SUMMARY_PREFIX(File("events/retrieveBySummaryPrefix.js"));

    enum class LANGUAGE {
        APPLESCRIPT,
        JAVASCRIPT
    }

    val load: Either<ScriptLoadingError, String> = loadResource(relativeScriptFile)

    fun loadResource(jsRelativeScriptFile: File): Either<ScriptLoadingError, String> = try {
        right(this::class.java.getResource("/templates/scripts/$jsRelativeScriptFile").readText())
    } catch (t: Throwable) {
        left(ScriptLoadingError(t))
    }

    fun interpolate(tokensAndReplacements: Map<ScriptTokens, String>): Either<ScriptLoadingError, String> =
        load.flatMap { scriptTemplate ->
            loadResource(File("_common.js")).map { commonJs ->
                (tokensAndReplacements + mapOf(COMMON to commonJs)).entries
                    .fold(scriptTemplate) { acc, (first, second) ->
                        acc.replace(
                            first.tokenize,
                            second
                        )
                    }
            }
        }
}

private enum class ScriptTokens(identifier: String) {
    CALENDAR("calendar"),
    CALENDAR_NAME("calendarName"),
    COMMON("common_functions"),
    EVENT("event"),
    EVENT_UID("eventUid"),
    SUMMARY("summary");

    val tokenize: String = "<<$identifier>>"
}

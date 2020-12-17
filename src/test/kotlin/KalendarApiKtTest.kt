import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.TestMethodOrder
import uk.co.ceilingcat.kalendarapi.CalendarName
import uk.co.ceilingcat.kalendarapi.EventAllday
import uk.co.ceilingcat.kalendarapi.EventDate
import uk.co.ceilingcat.kalendarapi.EventDescription
import uk.co.ceilingcat.kalendarapi.EventLocation
import uk.co.ceilingcat.kalendarapi.EventSummary
import uk.co.ceilingcat.kalendarapi.EventUrl
import uk.co.ceilingcat.kalendarapi.NewEvent
import uk.co.ceilingcat.kalendarapi.createDuration
import uk.co.ceilingcat.kalendarapi.createKalendarApi
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
internal class KalendarApiKtTest {

    companion object {
        private const val timeOutMagnitude = 60L
        private val timeOutUnits = TimeUnit.SECONDS
        private val testAutomationCalendarName = CalendarName("TEST_AUTOMATION")
        private val uuidString get() = UUID.randomUUID().toString()

        private val newAllDayEvent = NewEvent(alldayEvent = EventAllday(true), endDate = EventDate(Instant.now().toEpochMilli()))
        private val newEventWithProperties = NewEvent(
            summary = EventSummary("eventSummary"),
            location = EventLocation("eventLocation"),
            alldayEvent = EventAllday(false),
            startDate = EventDate(Instant.now().toEpochMilli()),
            endDate = EventDate(Instant.now().toEpochMilli() + TimeUnit.MINUTES.toMillis(30)),
            description = EventDescription("eventDescription"),
            url = EventUrl("http://domain.com")
        )
        private val newEventWithSummary = NewEvent(EventSummary(uuidString), endDate = EventDate(Instant.now().toEpochMilli()))
        private val eitherErrorOrApi
            get() = createDuration(timeOutMagnitude, timeOutUnits).map { timeOut ->
                createKalendarApi(timeOut)
            }
    }

    private fun <A, B> Either<A, B>.assertRight() {
        assert(this.isRight())
    }

    @Order(0)
    @Test
    fun `That we can retrieve all calendars`() {
        eitherErrorOrApi.flatMap { api -> api.retrieveCalendars }.assertRight()
    }

    @Order(1)
    @Test
    fun `That we have a TEST_AUTOMATION calendar available`() {
        eitherErrorOrApi
            .flatMap { api -> api.retrieveCalendar(testAutomationCalendarName) }
            .assertRight()
    }

    @Order(3)
    @Test
    fun `That we can create a new all day event and then delete it`() {
        eitherErrorOrApi
            .flatMap { api ->
                api
                    .createEvent(testAutomationCalendarName, newAllDayEvent)
                    .flatMap { eventIdentifier ->
                        api.deleteEvent(testAutomationCalendarName, eventIdentifier)
                    }
            }.assertRight()
    }

    @Order(4)
    @Test
    fun `That we can create a new event, fetch and then, delete it`() {
        eitherErrorOrApi
            .flatMap { api ->
                api
                    .createEvent(testAutomationCalendarName, newEventWithProperties)
                    .flatMap { eventIdentifier ->
                        api.retrieveEvent(testAutomationCalendarName, eventIdentifier).flatMap { (summary, location, alldayEvent, startDate, endDate, _, _, _, description, url) ->
                            newEventWithProperties.run {
                                api.deleteEvent(testAutomationCalendarName, eventIdentifier).also {
                                    assert(
                                        summary == summary &&
                                            location == location &&
                                            alldayEvent == alldayEvent &&
                                            startDate == startDate &&
                                            endDate == endDate &&
                                            description == description &&
                                            url == url
                                    )
                                }
                            }
                        }
                    }
            }.assertRight()
    }

    @Order(6)
    @Test
    fun `That we can locate events with a given summary, and delete them`() {
        eitherErrorOrApi
            .flatMap { api ->
                api.run {
                    newEventWithSummary
                        .let { newEvent ->
                            createEvent(testAutomationCalendarName, newEvent).flatMap { eventIdentifier ->
                                retrieveEventsBySummary(
                                    testAutomationCalendarName,
                                    newEvent.summary!!
                                ).flatMap {
                                    deleteEvent(testAutomationCalendarName, eventIdentifier)
                                }
                            }
                        }
                }
            }.assertRight()
    }

    @Order(7)
    @Test
    fun `That we can locate an event, by summary-prefix, and delete it`() {
        eitherErrorOrApi
            .flatMap { api ->
                api.run {
                    newEventWithSummary
                        .let { newEvent ->
                            createEvent(testAutomationCalendarName, newEvent).flatMap {
                                retrieveEventsBySummaryPrefix(
                                    testAutomationCalendarName,
                                    EventSummary(newEvent.summary!!.text.take(10))
                                ).flatMap { (list) ->
                                    when (list.size) {
                                        1 -> deleteEvent(
                                            testAutomationCalendarName,
                                            list[0].identifier
                                        )
                                        else -> false.left()
                                    }
                                }
                            }
                        }
                }
            }.assertRight()
    }
}

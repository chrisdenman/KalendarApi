package uk.co.ceilingcat.kalendarapi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An unordered list of preexisting calendars.
 *
 * @param list the calendars to group
 */
@Serializable
data class Calendars(val list: List<Calendar>)

/**
 *  A preexisting calendar.
 *
 *  @constructor
 *
 *  @param name the calendar's name (or title)
 *  @param description the calendar's description (or notes)
 */
@Serializable
data class Calendar(
    val name: CalendarName,
    val description: CalendarDescription
)

/**
 * A calendar to be created.
 *
 * @constructor
 *
 * @param name the desired name
 * @param description the optional description desired
 */
@Serializable
data class NewCalendar(
    val name: CalendarName,
    val description: CalendarDescription? = null
)

/**
 * A calendar's name, which is shown as its title, in `Calendar`.
 *
 * @param text blank or empty values are permitted
 */
@Serializable(with = CalendarNameSerializer::class)
data class CalendarName(override val text: String) : TypeWrappedString

/**
 * A calendar's description, which is shown as its notes, in `Calendar`.
 *
 * @constructor
 *
 * @param text blank or empty values are permitted
 */
@Serializable(with = CalendarDescriptionSerializer::class)
data class CalendarDescription(override val text: String) : TypeWrappedString

/**
 * A list of preexisting events, in no particular order.
 *
 * @constructor
 *
 * @param list the events to group
 */
@Serializable
data class Events(val list: List<Event>)

/**
 * A preexisting event.
 *
 * @constructor
 *
 * @param summary the event's summary, which is shown as its title
 * @param location the location associated with this event
 * @param alldayEvent whether or not, this event spans, one or more whole days
 * @param startDate the event's start date
 * @param endDate the event's end date
 * @param recurrence for recurring events, conforms to [RFC 2445](https://tools.ietf.org/html/rfc2445)
 * @param excludedDates a list of excluded dates, if this is a recurring event
 * @param status the event's participant confirmation status, only applies if the event has participants
 * @param description the event's description, which is shown as its notes
 * @param url the url associated with the event
 * @param identifier the unique identifier assigned when the event was made
 * @param sequence a versioning sequence-number
 * @param stampDate the event's last modification date
 */
@Serializable
data class Event(
    val summary: EventSummary,
    val location: EventLocation,
    val alldayEvent: EventAllday,
    val startDate: EventDate,
    val endDate: EventDate,
    val recurrence: EventRecurrence,
    val excludedDates: List<EventDate>,
    val status: EventStatus,
    val description: EventDescription,
    val url: EventUrl,
    @SerialName("uid") val identifier: Identifier,
    val sequence: EventSequence,
    val stampDate: EventDate
)

/**
 * An event to be created.
 *
 * Note that end date values are mandatory.
 *
 * @constructor
 *
 * @param summary the event's summary, which is shown as its title
 * @param location the location associated with this event
 * @param alldayEvent whether or not, this event spans, one or more whole days
 * @param startDate the event's start date
 * @param endDate the event's end date
 * @param recurrence for recurring events, conforms to [RFC 2445](https://tools.ietf.org/html/rfc2445)
 * @param description the event's description, which is shown as its notes
 * @param url the url associated with the event
 */
@Serializable
data class NewEvent(
    val summary: EventSummary? = null,
    val location: EventLocation? = null,
    val endDate: EventDate,
    val alldayEvent: EventAllday? = null,
    val startDate: EventDate? = null,
    val recurrence: EventRecurrence? = null,
    val description: EventDescription? = null,
    val url: EventUrl? = null
)

/**
 * An event's summary or 'title'.
 *
 * @constructor
 *
 * @param text unrestricted, blank and empty values permitted
 */
@Serializable(with = EventSummarySerializer::class)
data class EventSummary(override val text: String) : TypeWrappedString

/**
 * An event's associated location.
 *
 * @constructor
 *
 * @param text unrestricted, blank and empty values permitted
 */
@Serializable(with = EventLocationSerializer::class)
data class EventLocation(override val text: String) : TypeWrappedString

/**
 * Whether or not, this event spans one or more whole days.
 *
 * @constructor
 *
 * @param value `true` iff this event spans one, or more, whole days
 */
@Serializable(with = EventAlldayEventSerializer::class)
data class EventAllday(override val value: Boolean) : TypeWrappedBoolean

/**
 * A datatype used for representing all event-datetime properties.
 *
 * Values are: considered equals and, compared, using Second accuracy.
 *
 * @constructor
 *
 * @param number UTC milli-Seconds since the Unix epoch
 */
@Serializable(with = EventDateEventSerializer::class)
data class EventDate(override val number: Long) : TypeWrappedLong, Comparable<EventDate> {
    private val toSecondAccuracy: Long
        get() = (number / 1000) * 1000

    override fun compareTo(other: EventDate): Int = toSecondAccuracy.compareTo(other.toSecondAccuracy)

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> {
                true
            }
            javaClass != other?.javaClass -> {
                false
            }
            else -> (toSecondAccuracy == (other as EventDate).toSecondAccuracy)
        }

    override fun hashCode(): Int {
        return toSecondAccuracy.hashCode()
    }
}

/**
 * An event's recurrence details.
 *
 * @constructor
 *
 * @param text [RFC 2445](https://tools.ietf.org/html/rfc2445) conforming, recurrence text
 */
@Serializable(with = EventRecurrenceSerializer::class)
data class EventRecurrence(override val text: String) : TypeWrappedString

/**
 * An event's participant confirmation status.
 */
@Suppress("unused")
@Serializable
enum class EventStatus {

    /**
     * A cancelled event.
     */
    @SerialName("cancelled") CANCELLED,

    /**
     * A confirmed event.
     */
    @SerialName("confirmed") CONFIRMED,

    /**
     * An event to which a confirmation status does not apply.
     */
    @SerialName("none") NONE,

    /**
     * A tentative event.
     */
    @SerialName("tentative") TENTATIVE
}

/**
 * An event's description or 'notes'.
 *
 * @constructor
 *
 * @param text unrestricted, blank and empty values permitted
 */
@Serializable(with = EventDescriptionSerializer::class)
data class EventDescription(override val text: String) : TypeWrappedString

/**
 * An event's associated URL.
 *
 * @constructor
 *
 * @param text unrestricted, blank and empty values permitted
 */
@Serializable(with = EventUrlSerializer::class)
data class EventUrl(override val text: String) : TypeWrappedString

/**
 * An event's unique identifier which, is assigned when the event is made.
 *
 * @constructor
 *
 * @param text the event's unique identifier, which must match the regular-expression pattern
 * `"[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}"`
 */
@Serializable(with = UidSerializer::class)
data class Identifier(override val text: String) : TypeWrappedString {
    init {
        require(text.matches(REGEX))
    }
    companion object {
        private val REGEX = Regex("[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}")
    }
}

/**
 * An event's sequence-number which, is assigned when the event is made.
 *
 * @constructor
 *
 * @param number the event's version sequence-number
 */
@Serializable(with = EventSequenceSerializer::class)
data class EventSequence(override val number: Int) : TypeWrappedInt

package uk.co.ceilingcat.kalendarapi

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.BOOLEAN
import kotlinx.serialization.descriptors.PrimitiveKind.INT
import kotlinx.serialization.descriptors.PrimitiveKind.LONG
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal interface TypeWrappedString { val text: String }
internal open class TypeWrappedStringSerializer<T : TypeWrappedString>(
    typeName: String,
    val constructor: (String) -> T
) : KSerializer<T> {
    override fun deserialize(decoder: Decoder): T = constructor(decoder.decodeString())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(typeName, STRING)
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(value.text)
}

internal interface TypeWrappedInt { val number: Int }
internal open class TypeWrappedIntSerializer<T : TypeWrappedInt>(
    typeName: String,
    val constructor: (Int) -> T
) : KSerializer<T> {
    override fun deserialize(decoder: Decoder): T = constructor(decoder.decodeInt())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(typeName, INT)
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeInt(value.number)
}

internal interface TypeWrappedBoolean { val value: Boolean }
internal open class TypeWrappedBooleanSerializer<T : TypeWrappedBoolean>(
    typeName: String,
    val constructor: (Boolean) -> T
) : KSerializer<T> {
    override fun deserialize(decoder: Decoder): T = constructor(decoder.decodeBoolean())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(typeName, BOOLEAN)
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeBoolean(value.value)
}

internal interface TypeWrappedLong { val number: Long }
internal open class TypeWrappedLongSerializer<T : TypeWrappedLong>(
    typeName: String,
    val constructor: (Long) -> T
) : KSerializer<T> {
    override fun deserialize(decoder: Decoder): T = constructor(decoder.decodeLong())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(typeName, LONG)
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeLong(value.number)
}

internal object CalendarNameSerializer :
    TypeWrappedStringSerializer<CalendarName>(CalendarName::class.simpleName!!, { CalendarName(it) })

internal object CalendarDescriptionSerializer :
    TypeWrappedStringSerializer<CalendarDescription>(CalendarDescription::class.simpleName!!, { CalendarDescription(it) })

internal object UidSerializer :
    TypeWrappedStringSerializer<Identifier>(Identifier::class.simpleName!!, { Identifier(it) })

internal object EventSummarySerializer :
    TypeWrappedStringSerializer<EventSummary>(EventSummary::class.simpleName!!, { EventSummary(it) })

internal object EventRecurrenceSerializer :
    TypeWrappedStringSerializer<EventRecurrence>(EventRecurrence::class.simpleName!!, { EventRecurrence(it) })

internal object EventDescriptionSerializer :
    TypeWrappedStringSerializer<EventDescription>(EventDescription::class.simpleName!!, { EventDescription(it) })

internal object EventLocationSerializer :
    TypeWrappedStringSerializer<EventLocation>(EventLocation::class.simpleName!!, { EventLocation(it) })

internal object EventUrlSerializer :
    TypeWrappedStringSerializer<EventUrl>(EventUrl::class.simpleName!!, { EventUrl(it) })

internal object EventSequenceSerializer :
    TypeWrappedIntSerializer<EventSequence>(EventSequence::class.simpleName!!, { EventSequence(it) })

internal object EventAlldayEventSerializer :
    TypeWrappedBooleanSerializer<EventAllday>(EventAllday::class.simpleName!!, { EventAllday(it) })

internal object EventDateEventSerializer :
    TypeWrappedLongSerializer<EventDate>(EventDate::class.simpleName!!, { EventDate(it) })

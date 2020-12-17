package uk.co.ceilingcat.kalendarapi

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import uk.co.ceilingcat.kalendarapi.ProcessLauncherError.NegativeDurationError
import uk.co.ceilingcat.kalendarapi.ProcessLauncherError.ProcessError
import uk.co.ceilingcat.kalendarapi.ProcessLauncherError.ZeroDurationError
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempFile

/**
 * The errors that `ProcessLauncher` may return.
 */
sealed class ProcessLauncherError(cause: Throwable? = null) : Throwable(cause) {
    /**
     * Signals that, a zero-valued `Duration` was requested.
     */
    object ZeroDurationError : ProcessLauncherError()

    /**
     * Signals that, a negative-valued `Duration` was requested.
     */
    object NegativeDurationError : ProcessLauncherError()

    /**
     * Signals that, an exception was caught during the execution of a process.
     */
    data class ProcessError(override val cause: Throwable) : ProcessLauncherError(cause)
}

/**
 * Represents the ability to, spawn a process and block, whilst waiting for its completion.
 */
interface ProcessLauncher {
    /**
     * Launch a new process with the given input, maximum waiting time and, optional output file.
     *
     * @return `ProcessLauncherError` iff. an error stopped this function from returning
     */
    fun spawn(input: String, maximumDuration: Duration, outputFile: File? = null): ProcessLauncherError?
}

/**
 * Represents a duration of time.
 */
interface Duration {
    /**
     * The magnitude of this `Duration`, measured in `units`.
     */
    val magnitude: Long

    /**
     * The time-units that measure the magnitude of this `Duration`.
     */
    val units: TimeUnit
}

/**
 * Create a `Duration` with the given magnitude and units.
 *
 * @return `left<ProcessLauncherError>` iff. a non-positive magnitude is requested
 */
fun createDuration(magnitude: Long, units: TimeUnit): Either<ProcessLauncherError, Duration> =
    when {
        magnitude < 0L -> left(NegativeDurationError)
        magnitude == 0L -> left(ZeroDurationError)
        else -> right(DurationData(magnitude, units))
    }

private data class DurationData(override val magnitude: Long, override val units: TimeUnit) : Duration

internal data class CalendarProcessCommandTerms(val text: List<String>)

@ExperimentalPathApi
internal class OsaScriptProcessLauncher(
    private val calendarProcessCommandTerms: CalendarProcessCommandTerms
) : ProcessLauncher {

    override fun spawn(
        input: String,
        maximumDuration: Duration,
        outputFile: File?,
    ): ProcessLauncherError? = try {
        ProcessBuilder(
            calendarProcessCommandTerms.text + listOf(input),
        ).run {

            when (outputFile) {
                null -> redirectOutput(ProcessBuilder.Redirect.INHERIT)
                else -> redirectOutput(outputFile)
            }

            val errorFile = createTempFile().toFile()
            redirectError(errorFile)

            val process = start()

            if (!process.waitFor(maximumDuration.magnitude, maximumDuration.units)) {
                throw Throwable("TimeOut")
            }

            errorFile.readText().trim().let { errorOutput ->
                if (errorOutput != "") {
                    throw Throwable(errorOutput)
                }
            }

            if (process.exitValue() != 0) {
                throw Throwable("Non Success Exit-Value")
            }

            null
        }
    } catch (t: Throwable) {
        ProcessError(t)
    }
}

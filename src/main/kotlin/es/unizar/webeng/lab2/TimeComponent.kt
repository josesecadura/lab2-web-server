package es.unizar.webeng.lab2

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId

/** JSON body of `GET /time`: the local time in [zone]. */
data class TimeDTO(
    val time: LocalDateTime,
    val zone: String,
)

/** Source of the current time, so tests can replace the real clock. */
interface TimeProvider {
    /** Returns the current local time in [zone]. */
    fun now(zone: ZoneId): LocalDateTime
}

/** [TimeProvider] backed by the system clock. */
@Service
class TimeService : TimeProvider {
    override fun now(zone: ZoneId): LocalDateTime = LocalDateTime.now(zone)
}

/** Maps a time and its zone to the response DTO. */
fun LocalDateTime.toDTO(zone: ZoneId): TimeDTO = TimeDTO(time = this, zone = zone.id)

/** Exposes the server time as JSON. */
@RestController
class TimeController(
    private val service: TimeProvider,
) {
    /**
     * Returns the current time in the requested [zone], e.g. `?zone=Europe/Madrid`.
     * Without [zone] it uses the server default. An unknown zone answers `400`.
     */
    @GetMapping("/time")
    fun time(
        @RequestParam(required = false) zone: String?,
    ): TimeDTO {
        val zoneId =
            try {
                zone?.let { ZoneId.of(it) } ?: ZoneId.systemDefault()
            } catch (e: DateTimeException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown zone: $zone", e)
            }
        return service.now(zoneId).toDTO(zoneId)
    }
}

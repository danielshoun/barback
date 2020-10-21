package io.bartendr.barback.event

import io.bartendr.barback.event.form.AddEventForm
import io.bartendr.barback.event.form.EditAttendanceForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class EventController {

    @Autowired
    lateinit var eventService: EventService

    @PostMapping("/api/v1/organization/{organizationId}/events/add")
    fun addEvent(
            @PathVariable(name = "organizationId") organizationId: Long,
            @RequestBody addEventForm: AddEventForm,
            @CookieValue session: String
    ): ResponseEntity<String> {
        return eventService.addEvent(organizationId, addEventForm, session)
    }

    @PostMapping("/api/v1/organization/{organizationId}/events/{eventId}/approve")
    fun approveEvent(
            @PathVariable(name = "organizationId") organizationId: Long,
            @PathVariable(name = "eventId") eventId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return eventService.approveEvent(organizationId, eventId, session)
    }

    @PostMapping("/api/v1/organization/{organizationId}/events/{eventId}/sign-in")
    fun signIn(
            @PathVariable(name = "organizationId") organizationId: Long,
            @PathVariable(name = "eventId") eventId: Long,
            @RequestParam(name = "secret") secret: String,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return eventService.signIn(organizationId, eventId, secret, session)
    }

    @PostMapping("/api/v1/organization/{organizationId}/events/{eventId}/edit-attendance")
    fun editAttendance(
            @PathVariable(name = "organizationId") organizationId: Long,
            @PathVariable(name = "eventId") eventId: Long,
            @RequestBody editAttendanceForm: EditAttendanceForm,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return eventService.editAttendance(organizationId, eventId, editAttendanceForm, session)
    }

}
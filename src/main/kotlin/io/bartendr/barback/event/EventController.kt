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

    @PostMapping("/api/v1/organization/{organizationId}/events/{eventId}/deny")
    fun denyEvent(
            @PathVariable(name = "organizationId") organizationId: Long,
            @PathVariable(name = "eventId") eventId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return eventService.denyEvent(organizationId, eventId, session)
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

    @GetMapping("/api/v1/organization/{organizationId}/events/open")
    fun getOpenEvents(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<List<Event>> {
        return eventService.getOpenEvents(organizationId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/events/past")
    fun getPastEvents(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<List<Event>> {
        return eventService.getPastEvents(organizationId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/events/upcoming")
    fun getUpcomingEvents(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<List<Event>> {
        return eventService.getUpcomingEvents(organizationId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/events/unapproved")
    fun getUnapprovedEvents(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<List<Event>> {
        return eventService.getUnapprovedEvents(organizationId, session)
    }

}
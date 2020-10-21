package io.bartendr.barback.event

import io.bartendr.barback.event.form.AddEventForm
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

}
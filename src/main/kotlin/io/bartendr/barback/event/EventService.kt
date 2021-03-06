package io.bartendr.barback.event

import io.bartendr.barback.event.form.AddEventForm
import io.bartendr.barback.event.form.EditAttendanceForm
import io.bartendr.barback.organization.OrganizationRepository
import io.bartendr.barback.role.RoleRepository
import io.bartendr.barback.user.BarDetailsRepository
import io.bartendr.barback.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class EventService {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var barDetailsRepository: BarDetailsRepository

    @Autowired
    lateinit var organizationRepository: OrganizationRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var eventCategoryRepository: EventCategoryRepository


    fun addEvent(
            organizationId: Long,
            addEventForm: AddEventForm,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canSubmitEvents") && !requesterRole.permissions.contains("canManageEvents")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        if(addEventForm.startTime.before(Date()) && !(requesterRole.permissions.contains("SUPERADMIN") || requesterRole.permissions.contains("canManageEvents"))) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(addEventForm.closeTime.before(Date()) && !(requesterRole.permissions.contains("SUPERADMIN") || requesterRole.permissions.contains("canManageEvents"))) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(addEventForm.startTime.after(addEventForm.closeTime)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        val category = eventCategoryRepository.findByIdOrNull(addEventForm.categoryId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val newEvent = Event(
                name = addEventForm.name,
                value = addEventForm.value,
                startTime = addEventForm.startTime,
                closeTime = addEventForm.closeTime,
                requester = requester,
                organization = organization,
                category = category
        )

        if(requesterRole.permissions.contains("SUPERADMIN") || requesterRole.permissions.contains("canManageEvents")) {
            newEvent.approvedBy = requester
        }

        if(newEvent.closeTime.before(Date()) && (newEvent.category.requiredFor.size > 0 || newEvent.category.requiredForAll)) {
            val orgUsers = userRepository.findAllByOrganizations(organization)
            newEvent.notAttended.addAll(orgUsers)
            newEvent.closed = true

            if(newEvent.category.requiredForAll) {
                for(user in newEvent.notAttended) {
                    val barDetails = barDetailsRepository.findByUserAndOrganization(user, newEvent.organization)
                    barDetails.score -= newEvent.category.penalty
                    barDetailsRepository.save(barDetails)
                }
            }
            else {
                for (role in newEvent.category.requiredFor) {
                    for (user in role.users) {
                        val barDetails = barDetailsRepository.findByUserAndOrganization(user, newEvent.organization)
                        barDetails.score -= newEvent.category.penalty
                        barDetailsRepository.save(barDetails)
                    }
                }
            }
        }

        eventRepository.save(newEvent)

        return ResponseEntity(HttpStatus.OK)
    }

    fun approveEvent(
            organizationId: Long,
            eventId: Long,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val event = eventRepository.findByIdOrNull(eventId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageEvents")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        event.approvedBy = requester
        eventRepository.save(event)
        return ResponseEntity(HttpStatus.OK)
    }

    fun denyEvent(
            organizationId: Long,
            eventId: Long,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val event = eventRepository.findByIdOrNull(eventId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageEvents")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        eventRepository.delete(event)
        return ResponseEntity(HttpStatus.OK)
    }

    fun signIn(
            organizationId: Long,
            eventId: Long,
            secret: String,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if(requesterRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val event = eventRepository.findByIdOrNull(eventId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(event.closeTime.before(Date()) || event.startTime.after(Date())) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(event.attended.contains(requester)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(secret != event.secret) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        event.attended.add(requester)

        val barDetails = barDetailsRepository.findByUserAndOrganization(requester, organization)
        barDetails.score += event.value
        if(event.category.requiredFor.size == 0 && !event.category.requiredForAll) {
            for(flag in barDetails.flags) {
                if(flag.category == event.category) {
                    flag.completed = true
                }
            }
        }

        eventRepository.save(event)
        barDetailsRepository.save(barDetails)

        return ResponseEntity(HttpStatus.OK)

    }

    fun editAttendance(
            organizationId: Long,
            eventId: Long,
            editAttendanceForm: EditAttendanceForm,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageEvents")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val event = eventRepository.findByIdOrNull(eventId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(event.closeTime.after(Date())) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        val newAttended = userRepository.findAllById(editAttendanceForm.attendedIds)
        val newNotAttended = userRepository.findAllById(editAttendanceForm.notAttendedIds)

        if(!userRepository.findAllByOrganizations(event.organization).containsAll(newAttended + newNotAttended)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        newAttended.removeAll(event.attended)
        newNotAttended.removeAll(event.notAttended)

        for(user in newAttended) {
            val barDetails = barDetailsRepository.findByUserAndOrganization(user, organization)
            barDetails.score += event.value
            if(event.category.requiredForAll) {
                barDetails.score += event.category.penalty
            }
            else {
                for (role in event.category.requiredFor) {
                    if (role.users.contains(user)) {
                        barDetails.score += event.category.penalty
                    }
                }
            }
            if(event.category.requiredFor.size == 0 && !event.category.requiredForAll) {
                for (flag in barDetails.flags) {
                    if (flag.category == event.category) {
                        flag.completed = true
                    }
                }
            }
            barDetailsRepository.save(barDetails)
        }

        for(user in newNotAttended) {
            val barDetails = barDetailsRepository.findByUserAndOrganization(user, organization)
            barDetails.score -= event.value
            if(event.category.requiredForAll) {
                barDetails.score -= event.category.penalty
            }
            else {
                for (role in event.category.requiredFor) {
                    if (role.users.contains(user)) {
                        barDetails.score -= event.category.penalty
                    }
                }
            }
            if(event.category.requiredFor.size == 0 && !event.category.requiredForAll) {
                for (flag in barDetails.flags) {
                    if (flag.category == event.category) {
                        if (eventRepository.findAllByCategoryAndAttended(event.category, user).size < 2) {
                            flag.completed = false
                        }
                    }
                }
            }
            barDetailsRepository.save(barDetails)
        }

        event.attended.removeAll(newNotAttended)
        event.attended.addAll(newAttended)
        event.notAttended.removeAll(newAttended)
        event.notAttended.addAll(newNotAttended)

        eventRepository.save(event)

        return ResponseEntity(HttpStatus.OK)
    }

    fun getOpenEvents(
            organizationId: Long,
            session: String
    ): ResponseEntity<List<Event>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if(requesterRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val events = eventRepository.findAllByOrganizationAndStartTimeBeforeAndClosed(organization, Date(), false)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageEvents"))  {
            for(event in events) {
                event.secret = ""
            }
        }

        return ResponseEntity(events, HttpStatus.OK)
    }

    fun getPastEvents(
            organizationId: Long,
            session: String
    ): ResponseEntity<List<Event>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if(requesterRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val events = eventRepository.findAllByOrganizationAndClosed(organization, true)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageEvents"))  {
            for(event in events) {
                event.secret = ""
            }
        }

        return ResponseEntity(events, HttpStatus.OK)
    }

    fun getUpcomingEvents(
            organizationId: Long,
            session: String
    ): ResponseEntity<List<Event>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if(requesterRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val events = eventRepository.findAllByOrganizationAndStartTimeAfter(organization, Date())

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageEvents"))  {
            for(event in events) {
                event.secret = ""
            }
        }

        return ResponseEntity(events, HttpStatus.OK)
    }

    fun getUnapprovedEvents(
            organizationId: Long,
            session: String
    ): ResponseEntity<List<Event>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageEvents")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val events = eventRepository.findAllByOrganizationAndApprovedBy(organization, null)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageEvents"))  {
            for(event in events) {
                event.secret = ""
            }
        }

        return ResponseEntity(events, HttpStatus.OK)
    }

}
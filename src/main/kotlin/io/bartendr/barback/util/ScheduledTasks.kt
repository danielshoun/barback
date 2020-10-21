package io.bartendr.barback.util

import io.bartendr.barback.event.EventRepository
import io.bartendr.barback.role.RoleRepository
import io.bartendr.barback.user.BarDetailsRepository
import io.bartendr.barback.user.User
import io.bartendr.barback.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class ScheduledTasks {

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var barDetailsRepository: BarDetailsRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Scheduled(fixedRate = 60000)
    fun closeEvents() {
        var eventsToClose = eventRepository.findAllByCloseTimeBeforeAndClosed(Date(), false)

        for(event in eventsToClose) {
            event.isClosed = true
            var notAttended = userRepository.findAllByOrganizations(event.organization).toMutableList()
            notAttended.removeAll(event.attended)
            event.notAttended.addAll(notAttended)

            for(user in notAttended) {
                var barDetails = barDetailsRepository.findByUserAndOrganization(user, event.organization)
                var userRole = roleRepository.findByOrganizationAndUsers(event.organization, user)
                if(event.category.requiredFor.contains(userRole)) {
                    barDetails.score -= event.category.penalty
                }
                barDetailsRepository.save(barDetails)
            }
            eventRepository.save(event)
        }
    }

    @Scheduled(fixedRate = 60000)
    fun removeUnapprovedEvents() {
        var eventsToRemove = eventRepository.findAllByStartTimeBeforeAndApprovedBy(Date(), null)
        eventRepository.deleteAll(eventsToRemove)
    }

}
package io.bartendr.barback.util

import io.bartendr.barback.event.EventRepository
import io.bartendr.barback.role.RoleRepository
import io.bartendr.barback.user.BarDetailsRepository
import io.bartendr.barback.user.User
import io.bartendr.barback.user.UserRepository
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
@Transactional
class ScheduledTasks {

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var barDetailsRepository: BarDetailsRepository

    @Scheduled(fixedRate = 60000)
    fun closeEvents() {
        val eventsToClose = eventRepository.findAllByCloseTimeBeforeAndClosed(Date(), false)

        for(event in eventsToClose) {
            event.closed = true
            val notAttended = userRepository.findAllByOrganizations(event.organization).toMutableList()
            notAttended.removeAll(event.attended)
            event.notAttended.addAll(notAttended)

            for(requiredRole in event.category.requiredFor) {
                for(user in requiredRole.users) {
                    if(notAttended.contains(user)) {
                        val barDetails = barDetailsRepository.findByUserAndOrganization(user, event.organization)
                        barDetails.score -= event.category.penalty
                        barDetailsRepository.save(barDetails)
                    }
                }
            }

            eventRepository.save(event)
        }
    }

    @Scheduled(fixedRate = 60000)
    fun removeUnapprovedEvents() {
        val eventsToRemove = eventRepository.findAllByStartTimeBeforeAndApprovedBy(Date(), null)
        eventRepository.deleteAll(eventsToRemove)
    }

}
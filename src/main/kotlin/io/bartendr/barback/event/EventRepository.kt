package io.bartendr.barback.event

import io.bartendr.barback.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EventRepository : JpaRepository<Event, Long> {

    fun findByCategoryAndAttended(category: EventCategory, attended: User): List<Event>
    fun findAllByCloseTimeBeforeAndClosed(closeTime: Date, closed: Boolean): List<Event>
    fun findAllByStartTimeBeforeAndApprovedBy(startTime: Date, approvedBy: User?): List<Event>

}
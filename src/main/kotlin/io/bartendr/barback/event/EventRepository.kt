package io.bartendr.barback.event

import io.bartendr.barback.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository : JpaRepository<Event, Long> {

    fun findByCategoryAndAttended(category: EventCategory, attended: User): List<Event>

}
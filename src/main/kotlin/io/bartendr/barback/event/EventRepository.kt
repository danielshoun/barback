package io.bartendr.barback.event

import io.bartendr.barback.organization.Organization
import io.bartendr.barback.role.Role
import io.bartendr.barback.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EventRepository : JpaRepository<Event, Long> {

    fun findAllByOrganizationAndStartTimeBeforeAndClosed(organization: Organization, startTime: Date, closed: Boolean): List<Event>
    fun findAllByOrganizationAndStartTimeAfter(organization: Organization, startTime: Date): List<Event>
    fun findAllByOrganizationAndClosed(organization: Organization, closed: Boolean): List<Event>
    fun findAllByOrganizationAndApprovedBy(organization: Organization, approvedBy: User?): List<Event>
    fun findAllByCategory(category: EventCategory): List<Event>
    fun findAllByCategoryAndAttended(category: EventCategory, attended: User): List<Event>
    fun findAllByCloseTimeBeforeAndClosed(closeTime: Date, closed: Boolean): List<Event>
    fun findAllByStartTimeBeforeAndApprovedBy(startTime: Date, approvedBy: User?): List<Event>
    fun findAllByOrganizationAndCategory_RequiredFor(organization: Organization, requiredFor: Role): List<Event>
    fun findAllByOrganizationAndCategory_RequiredForAll(organization: Organization, requiredForAll: Boolean): List<Event>

}
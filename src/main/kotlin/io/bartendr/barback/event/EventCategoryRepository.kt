package io.bartendr.barback.event

import io.bartendr.barback.organization.Organization
import org.springframework.data.jpa.repository.JpaRepository

interface EventCategoryRepository : JpaRepository<EventCategory, Long> {

    fun findAllByOrganization(organization: Organization): List<EventCategory>

}
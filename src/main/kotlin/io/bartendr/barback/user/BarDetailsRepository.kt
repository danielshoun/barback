package io.bartendr.barback.user

import io.bartendr.barback.organization.Organization
import org.springframework.data.jpa.repository.JpaRepository

interface BarDetailsRepository : JpaRepository<BarDetails, Long> {

    fun findByUserAndOrganization(user: User, organization: Organization): BarDetails
    fun findAllByOrganization(organization: Organization): MutableList<BarDetails>

}
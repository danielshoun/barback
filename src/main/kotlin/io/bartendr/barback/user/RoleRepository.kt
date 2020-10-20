package io.bartendr.barback.user

import io.bartendr.barback.organization.Organization
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long> {

    fun findByOrganizationAndUsers(organization: Organization, user: User): Role?
    fun findAllByOrganization(organization: Organization): List<Role>
    fun findByOrganizationAndPermissions(organization: Organization, permission: String): Role

}
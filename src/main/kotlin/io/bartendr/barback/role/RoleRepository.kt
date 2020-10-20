package io.bartendr.barback.role

import io.bartendr.barback.organization.Organization
import io.bartendr.barback.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long> {

    fun findByOrganizationAndUsers(organization: Organization, user: User): Role?
    fun findAllByOrganization(organization: Organization): List<Role>
    fun findByOrganizationAndPermissions(organization: Organization, permission: String): Role

}
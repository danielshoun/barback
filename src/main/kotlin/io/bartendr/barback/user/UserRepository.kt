package io.bartendr.barback.user

import io.bartendr.barback.organization.Organization
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findBySessions_Key(session: String): User?
    fun findByEmailAddress(emailAddress: String): User?
    fun findByEmailVerificationToken(emailVerificationToken: String): User?
    fun findByForgottenPasswordToken(forgottenPasswordToken: String): User?
    fun findAllByOrganizationsContaining(organization: Organization): List<User>

}
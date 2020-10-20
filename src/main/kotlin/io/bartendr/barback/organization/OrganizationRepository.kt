package io.bartendr.barback.organization

import io.bartendr.barback.school.School
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizationRepository : JpaRepository<Organization, Long> {

    fun findAllBySchool(school: School): List<Organization>
    fun findByName(name: String): Organization?
    fun findByRef(ref: String): Organization?
    fun findByNameAndSchool(name: String, school: School): Organization?

}
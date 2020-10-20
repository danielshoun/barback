package io.bartendr.barback.school

import org.springframework.data.jpa.repository.JpaRepository

interface SchoolRepository : JpaRepository<School, Long> {

    fun findByName(name: String): School?
    fun findByRef(ref: String): School?

}
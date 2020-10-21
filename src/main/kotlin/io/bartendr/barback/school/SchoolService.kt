package io.bartendr.barback.school

import io.bartendr.barback.organization.Organization
import io.bartendr.barback.organization.OrganizationRepository
import io.bartendr.barback.school.form.AddSchoolForm
import io.bartendr.barback.school.form.EditSchoolForm
import io.bartendr.barback.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class SchoolService {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var schoolRepository: SchoolRepository

    @Autowired
    lateinit var organizationRepository: OrganizationRepository

    fun addSchool(
            addSchoolForm: AddSchoolForm,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if (!requester.isWebsiteAdmin) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        if (schoolRepository.findByName(addSchoolForm.name) != null) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if (schoolRepository.findByRef(addSchoolForm.ref) != null) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val newSchool = School(
                name = addSchoolForm.name,
                ref = addSchoolForm.ref
        )
        schoolRepository.save(newSchool)
        return ResponseEntity(HttpStatus.CREATED)
    }

    fun editSchool(
            editSchoolForm: EditSchoolForm,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if (!requester.isWebsiteAdmin) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val school = schoolRepository.findByIdOrNull(editSchoolForm.id)?:
                return ResponseEntity(HttpStatus.NOT_FOUND)

        if (schoolRepository.findByName(editSchoolForm.name) != null) {
            if(schoolRepository.findByName(editSchoolForm.name)!!.id != school.id) {
                return ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        }

        if (schoolRepository.findByRef(editSchoolForm.ref) != null) {
            if (schoolRepository.findByRef(editSchoolForm.ref)!!.id != school.id) {
                return ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        }

        school.name = editSchoolForm.name
        school.ref = editSchoolForm.ref
        schoolRepository.save(school)
        return ResponseEntity(HttpStatus.OK)
    }

    fun deleteSchool(
            schoolId: Long,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if (!requester.isWebsiteAdmin) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val schoolToDelete = schoolRepository.findByIdOrNull(schoolId)?:
                return ResponseEntity(HttpStatus.NOT_FOUND)

        val orgsInSchool = organizationRepository.findAllBySchool(schoolToDelete)
        if (orgsInSchool.isNotEmpty()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        schoolRepository.delete(schoolToDelete)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    fun joinSchool(
            ref: String,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val school = schoolRepository.findByRef(ref)?:
                return ResponseEntity(HttpStatus.NOT_FOUND)

        if (requester.school != null && requester.organizations.isNotEmpty()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        requester.school = school
        userRepository.save(requester)
        return ResponseEntity(HttpStatus.ACCEPTED)

    }

    fun getAllSchools(
            session: String
    ): ResponseEntity<List<School>> {
        val requester = userRepository.findBySessions_Key(session)?:
            return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if (!requester.isWebsiteAdmin) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val schools: List<School> = schoolRepository.findAll()
        return ResponseEntity(schools, HttpStatus.OK)
    }

    fun getOrgsBySchool(
            schoolId: Long,
            session: String
    ): ResponseEntity<List<Organization>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val school: School = schoolRepository.findByIdOrNull(schoolId)?:
                return ResponseEntity(HttpStatus.NOT_FOUND)

        if (requester.school != school) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
        return ResponseEntity(organizationRepository.findAllBySchool(school), HttpStatus.OK)
    }

}
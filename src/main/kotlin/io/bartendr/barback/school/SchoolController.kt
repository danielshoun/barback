package io.bartendr.barback.school

import io.bartendr.barback.school.form.AddSchoolForm
import io.bartendr.barback.school.form.EditSchoolForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class SchoolController {

    @Autowired
    lateinit var schoolService: SchoolService

    @PostMapping("/api/v1/school/add")
    fun addSchool(@RequestBody addSchoolForm: AddSchoolForm,
                  @CookieValue(value = "session") session: String): ResponseEntity<String> {
        return schoolService.addSchool(addSchoolForm, session)
    }

    @PostMapping("/api/v1/school/edit")
    fun editSchool(@RequestBody editSchoolForm: EditSchoolForm,
                   @CookieValue(value = "session") session: String): ResponseEntity<String> {
        return schoolService.editSchool(editSchoolForm, session)
    }

    @PostMapping("/api/v1/school/{schoolId}/delete")
    fun deleteSchool(@PathVariable schoolId: Long,
                     @CookieValue(value = "session") session: String): ResponseEntity<String> {
        return schoolService.deleteSchool(schoolId, session)
    }

    @GetMapping("/api/v1/school/get-all")
    fun getAllSchools(@CookieValue(value = "session") session: String): ResponseEntity<List<School>> {
        return schoolService.getAllSchools(session)
    }

    @PostMapping("/api/v1/school/join")
    fun joinSchool(@RequestParam(name = "ref", required = true) ref: String,
                   @CookieValue(value = "session") session: String): ResponseEntity<String> {
        return schoolService.joinSchool(ref, session)
    }

    @GetMapping("/api/v1/school/{schoolId}/organizations")
    fun getOrgsBySchool(@PathVariable schoolId: Long,
                        @CookieValue(value = "session") session: String) {
        schoolService.getOrgsBySchool(schoolId, session)
    }

}
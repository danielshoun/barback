package io.bartendr.barback.organization

import io.bartendr.barback.event.EventCategory
import io.bartendr.barback.organization.form.*
import io.bartendr.barback.user.BarDetails
import io.bartendr.barback.role.Role
import io.bartendr.barback.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class OrganizationController {

    @Autowired
    lateinit var organizationService: OrganizationService

    @PostMapping("/api/v1/organization/add")
    fun addOrganization(
            @RequestBody addOrganizationForm: AddOrganizationForm,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return organizationService.addOrganization(addOrganizationForm, session)
    }

    @PostMapping("/api/v1/organization/join")
    fun joinOrganization(
            @RequestBody joinOrganizationForm: JoinOrganizationForm,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return organizationService.joinOrganization(joinOrganizationForm, session)
    }

    @PostMapping("/api/v1/organization/{organizationId}/users/{userId}/approve")
    fun approveUser(
            @PathVariable(name = "organizationId") organizationId: Long,
            @PathVariable(name = "userId") userId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return organizationService.approveUser(organizationId, userId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}")
    fun getOrganization(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<Organization> {
        return organizationService.getOrganization(organizationId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/users")
    fun getOrgUsers(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<MutableList<User>> {
        return organizationService.getOrgUsers(organizationId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/users/unapproved")
    fun getUnapprovedUsers(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<MutableList<User>> {
        return organizationService.getUnapprovedUsers(organizationId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/users/details")
    fun getOrgBarDetails(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<List<BarDetails>> {
        return organizationService.getOrgBarDetails(organizationId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/categories")
    fun getEventCategories(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<List<EventCategory>> {
        return organizationService.getEventCategories(organizationId, session)
    }

    @PostMapping("/api/v1/organization/{organizationId}/categories/add")
    fun addEventCategory(
            @PathVariable(name = "organizationId") organizationId: Long,
            @RequestBody addCategoryForm: AddCategoryForm,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return organizationService.addEventCategory(organizationId, addCategoryForm, session)
    }

}
package io.bartendr.barback.organization

import io.bartendr.barback.event.EventCategory
import io.bartendr.barback.event.EventCategoryRepository
import io.bartendr.barback.organization.form.*
import io.bartendr.barback.role.Role
import io.bartendr.barback.role.RoleRepository
import io.bartendr.barback.school.SchoolRepository
import io.bartendr.barback.user.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class OrganizationService {

    @Autowired
    lateinit var schoolRepository: SchoolRepository

    @Autowired
    lateinit var organizationRepository: OrganizationRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var barDetailsRepository: BarDetailsRepository

    @Autowired
    lateinit var eventCategoryRepository: EventCategoryRepository

    val bCryptPasswordEncoder = BCryptPasswordEncoder()

    fun addOrganization(
            addOrganizationForm: AddOrganizationForm,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val school = requester.school?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(organizationRepository.findByRef(addOrganizationForm.ref) != null) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(organizationRepository.findByNameAndSchool(addOrganizationForm.name, school) != null) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(addOrganizationForm.name == "") {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(addOrganizationForm.ref == "") {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        if(addOrganizationForm.secret == "") {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        val newOrg = Organization(
                name = addOrganizationForm.name,
                ref = addOrganizationForm.ref,
                secret = bCryptPasswordEncoder.encode(addOrganizationForm.secret),
                school = school
        )

        val superRole = Role(
                name = "Admin",
                organization = newOrg
        )
        superRole.permissions.add("SUPERADMIN")

        val defaultRole = Role(
                name = "User",
                organization = newOrg
        )

        defaultRole.permissions.add("DEFAULT")

        val unapprovedRole = Role(
                name = "Unapproved",
                organization = newOrg
        )
        unapprovedRole.permissions.add("UNAPPROVED")

        organizationRepository.save(newOrg)
        roleRepository.save(superRole)
        roleRepository.save(defaultRole)
        roleRepository.save(unapprovedRole)

        requester.organizations.add(newOrg)
        superRole.users.add(requester)

        userRepository.save(requester)

        val barDetails = BarDetails(
                user = requester,
                organization = newOrg
        )

        barDetailsRepository.save(barDetails)

        return ResponseEntity(HttpStatus.CREATED)

    }

    fun joinOrganization(
            joinOrganizationForm: JoinOrganizationForm,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByRef(joinOrganizationForm.ref)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(organization.school != requester.school) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        return if(bCryptPasswordEncoder.matches(joinOrganizationForm.secret, organization.secret)) {
            requester.organizations.add(organization)

            if(organization.organizationSettings.requireUserApproval) {
                val unapprovedRole = roleRepository.findByOrganizationAndPermissions(organization, "UNAPPROVED")
                unapprovedRole.users.add(requester)
                roleRepository.save(unapprovedRole)
            }
            else {
                val defaultRole = roleRepository.findByOrganizationAndPermissions(organization, "DEFAULT")
                defaultRole.users.add(requester)
                roleRepository.save(defaultRole)
            }

            organizationRepository.save(organization)
            userRepository.save(requester)

            val barDetails = BarDetails(
                    user = requester,
                    organization = organization
            )

            val eventCategories: List<EventCategory> = eventCategoryRepository.findAllByOrganization(organization)

            for(category in eventCategories) {
                if(category.requiredFor.size == 0) {
                    barDetails.flags.add(Flag(
                            category = category,
                            completed = false
                    ))
                }
            }

            barDetailsRepository.save(barDetails)

            ResponseEntity(HttpStatus.OK)
        }
        else {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }

    fun approveUser(
            organizationId: Long,
            userId: Long,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageUsers")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val unapprovedUser = userRepository.findByIdOrNull(userId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val unapprovedUserRole = roleRepository.findByOrganizationAndUsers(organization, unapprovedUser)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!unapprovedUserRole.permissions.contains("UNAPPROVED")) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        val unapprovedRole = roleRepository.findByOrganizationAndPermissions(organization, "UNAPPROVED")
        val defaultRole = roleRepository.findByOrganizationAndPermissions(organization, "DEFAULT")

        unapprovedRole.users.remove(unapprovedUser)
        defaultRole.users.add(unapprovedUser)

        roleRepository.save(unapprovedRole)
        roleRepository.save(defaultRole)

        return ResponseEntity(HttpStatus.OK)
    }

    fun getOrganization(
            organizationId: Long,
            session: String
    ): ResponseEntity<Organization> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requester.organizations.contains(organization)) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        return ResponseEntity(organization, HttpStatus.OK)
    }

    fun getOrgUsers(
            organizationId: Long,
            session: String
    ): ResponseEntity<MutableList<User>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requester.organizations.contains(organization)) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val users = userRepository.findAllByOrganizations(organization).toMutableList()
        val unapprovedUsers: MutableList<User> = mutableListOf()

        for (user in users) {
            val role = roleRepository.findByOrganizationAndUsers(organization, user)?:
                    continue
            if(role.permissions.contains("UNAPPROVED")) {
                unapprovedUsers.add(user)
            }
        }

        users.removeAll(unapprovedUsers)

        return ResponseEntity(users, HttpStatus.OK)
    }

    fun getUnapprovedUsers(
            organizationId: Long,
            session: String
    ): ResponseEntity<MutableList<User>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageUsers")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val unapprovedRole = roleRepository.findByOrganizationAndPermissions(organization, "UNAPPROVED")

        return ResponseEntity(unapprovedRole.users, HttpStatus.OK)
    }
    
    fun getOrgBarDetails(
            organizationId: Long,
            session: String
    ): ResponseEntity<List<BarDetails>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requester.organizations.contains(organization)) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val barDetailsList = barDetailsRepository.findAllByOrganization(organization)
        var barDetailsToRemove = mutableListOf<BarDetails>()

        for (barDetails in barDetailsList) {
            if(roleRepository.findByOrganizationAndUsers(organization, barDetails.user)!!.permissions.contains("UNAPPROVED")) {
                        barDetailsToRemove.add(barDetails)
                    }
        }

        barDetailsList.removeAll(barDetailsToRemove)

        return ResponseEntity(barDetailsList, HttpStatus.OK)
    }

    fun getEventCategories(
            organizationId: Long,
            session: String
    ): ResponseEntity<List<EventCategory>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requester.organizations.contains(organization)) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val eventCategories = eventCategoryRepository.findAllByOrganization(organization)

        return ResponseEntity(eventCategories, HttpStatus.OK)
    }

    fun addEventCategory(
            organizationId: Long,
            addCategoryForm: AddCategoryForm,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val role = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        return if(role.permissions.contains("SUPERADMIN") || role.permissions.contains("canAddEventCategories")) {
            val requiredRoles: MutableList<Role> = roleRepository.findAllById(addCategoryForm.requiredRoleIds)

            val eventCategory = EventCategory(
                    name = addCategoryForm.name,
                    penalty = addCategoryForm.penalty,
                    requiredFor = requiredRoles,
                    organization = organization
            )

            eventCategoryRepository.save(eventCategory)

            for (user in userRepository.findAllByOrganizations(organization)) {
                val barDetails = barDetailsRepository.findByUserAndOrganization(user, organization)
                if(eventCategory.requiredFor.size == 0) {
                    barDetails.flags.add(Flag(
                            category = eventCategory,
                            completed = false
                    ))
                }
                barDetailsRepository.save(barDetails)
            }

            ResponseEntity(HttpStatus.CREATED)
        }
        else {
            ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
    }

}
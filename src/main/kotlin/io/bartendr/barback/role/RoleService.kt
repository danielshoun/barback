package io.bartendr.barback.role

import io.bartendr.barback.organization.OrganizationRepository
import io.bartendr.barback.role.form.AddRoleForm
import io.bartendr.barback.role.form.GiveRoleForm
import io.bartendr.barback.user.User
import io.bartendr.barback.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class RoleService {

    @Autowired
    lateinit var organizationRepository: OrganizationRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    fun addRole(
            organizationId: Long,
            addRoleForm: AddRoleForm,
            session: String
    ): ResponseEntity<String> {

        val possiblePermissions: List<String> = mutableListOf(
                "SUPERADMIN",
                "UNAPPROVED",
                "DEFAULT",
                "canEditOrganization",
                "canManageUsers",
                "canManageRoles",
                "canSubmitEvents",
                "canApproveEvents",
                "canAddEventCategories",
                "canManagePolls")

        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageRoles")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        if(!possiblePermissions.containsAll(addRoleForm.permissions)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        val newRole = Role(name = addRoleForm.name, organization = organization, permissions = addRoleForm.permissions)

        roleRepository.save(newRole)
        return ResponseEntity(HttpStatus.OK)

    }

    fun giveUsersRole(
            organizationId: Long,
            roleId: Long,
            giveRoleForm: GiveRoleForm,
            session: String
    ): ResponseEntity<String> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val requesterRole = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        if(!requesterRole.permissions.contains("SUPERADMIN") && !requesterRole.permissions.contains("canManageRoles")) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val usersToGive: List<User> = userRepository.findAllById(giveRoleForm.userIds)
        val roleToGive: Role = roleRepository.findByIdOrNull(roleId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)


        for (user in usersToGive) {
            val oldRole = roleRepository.findByOrganizationAndUsers(organization, user)?:
                    return ResponseEntity(HttpStatus.BAD_REQUEST)

            oldRole.users.remove(user)
            roleToGive.users.add(user)
            roleRepository.saveAll(listOf(oldRole, roleToGive))
        }

        return ResponseEntity(HttpStatus.OK)
    }

    fun getOrgRoles(
            organizationId: Long,
            session: String
    ): ResponseEntity<MutableList<Role>> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        if(!requester.organizations.contains(organization)) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val roles = roleRepository.findAllByOrganization(organization).toMutableList()

        val unapprovedRole = roleRepository.findByOrganizationAndPermissions(organization, "UNAPPROVED")

        roles.remove(unapprovedRole)

        return ResponseEntity(roles, HttpStatus.OK)
    }

    fun getRoleForOrg(
            organizationId: Long,
            session: String): ResponseEntity<Role> {
        val requester = userRepository.findBySessions_Key(session)?:
                return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val organization = organizationRepository.findByIdOrNull(organizationId)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        val role = roleRepository.findByOrganizationAndUsers(organization, requester)?:
                return ResponseEntity(HttpStatus.BAD_REQUEST)

        return ResponseEntity(role, HttpStatus.OK)
    }

}
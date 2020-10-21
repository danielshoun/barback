package io.bartendr.barback.role

import io.bartendr.barback.role.form.AddRoleForm
import io.bartendr.barback.role.form.GiveRoleForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class RoleController {

    @Autowired
    lateinit var roleService: RoleService

    @PostMapping("/api/v1/organization/{organizationId}/roles/add")
    fun addRole(
            @PathVariable(name = "organizationId") organizationId: Long,
            @RequestBody addRoleForm: AddRoleForm,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return roleService.addRole(organizationId, addRoleForm, session)
    }

    @PostMapping("/api/v1/organization/{organizationId}/roles/{roleId}/give")
    fun giveUsersRole(
            @PathVariable(name = "organizationId") organizationId: Long,
            @PathVariable(name = "roleId") roleId: Long,
            @RequestBody giveRoleForm: GiveRoleForm,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<String> {
        return roleService.giveUsersRole(organizationId, roleId, giveRoleForm, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/roles")
    fun getOrgRoles(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<MutableList<Role>> {
        return roleService.getOrgRoles(organizationId, session)
    }

    @GetMapping("/api/v1/organization/{organizationId}/role-self")
    fun getRoleForOrg(
            @PathVariable(name = "organizationId") organizationId: Long,
            @CookieValue(value = "session") session: String
    ): ResponseEntity<Role> {
        return roleService.getRoleForOrg(organizationId, session)
    }

}
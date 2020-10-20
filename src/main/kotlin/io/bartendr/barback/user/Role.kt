package io.bartendr.barback.user

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIgnore
import io.bartendr.barback.model.BaseEntity
import io.bartendr.barback.organization.Organization
import java.security.Permissions
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne

/*
Possible permissions:
SUPERADMIN
UNAPPROVED
DEFAULT
canEditOrganization
canManageUsers
canManageRoles
canSubmitEvents
canApproveEvents
canAddEventCategories
canManagePolls
 */

@Entity
class Role(
        var name: String,
        @JsonIgnore @ManyToOne var organization: Organization,
        @JsonIgnore @ManyToMany var users: MutableList<User> = mutableListOf(),
        @ElementCollection var permissions: MutableList<String> = mutableListOf()
) : BaseEntity<Long>()
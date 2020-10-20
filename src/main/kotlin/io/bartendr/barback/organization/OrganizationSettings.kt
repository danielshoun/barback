package io.bartendr.barback.organization

import io.bartendr.barback.model.BaseEntity
import io.bartendr.barback.user.Role
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Embeddable
import javax.persistence.OneToOne

@Embeddable
class OrganizationSettings(
    var isActive: Boolean = true,
    var requireUserApproval: Boolean = true,
    var nextResetDate: Date? = null
)
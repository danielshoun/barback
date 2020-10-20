package io.bartendr.barback.organization

import java.util.*
import javax.persistence.Embeddable

@Embeddable
class OrganizationSettings(
    var isActive: Boolean = true,
    var requireUserApproval: Boolean = true,
    var nextResetDate: Date? = null
)
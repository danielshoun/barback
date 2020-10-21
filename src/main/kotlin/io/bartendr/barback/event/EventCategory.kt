package io.bartendr.barback.event

import io.bartendr.barback.model.BaseEntity
import io.bartendr.barback.organization.Organization
import io.bartendr.barback.role.Role
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne

@Entity
class EventCategory(
        var name: String,
        var penalty: Int,
        @ManyToMany var requiredFor: MutableList<Role> = mutableListOf(),
        @ManyToOne(cascade = [CascadeType.ALL]) var organization: Organization
) : BaseEntity<Long>()
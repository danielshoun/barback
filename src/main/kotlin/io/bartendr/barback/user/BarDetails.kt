package io.bartendr.barback.user

import io.bartendr.barback.model.BaseEntity
import io.bartendr.barback.organization.Organization
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class BarDetails(
        @ManyToOne var user: User,
        @ManyToOne var organization: Organization,
        var score: Int = 0,
        @OneToMany(cascade = [CascadeType.ALL]) var flags: MutableList<Flag> = mutableListOf()
): BaseEntity<Long>()
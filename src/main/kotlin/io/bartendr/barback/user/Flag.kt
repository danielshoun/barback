package io.bartendr.barback.user

import io.bartendr.barback.event.EventCategory
import io.bartendr.barback.model.BaseEntity
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class Flag(
        @ManyToOne var category: EventCategory,
        var completed: Boolean
) : BaseEntity<Long>()
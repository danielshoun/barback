package io.bartendr.barback.school

import io.bartendr.barback.model.BaseEntity
import javax.persistence.Entity

@Entity
class School(
        var name: String,
        var ref: String
) : BaseEntity<Long>()
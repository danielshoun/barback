package io.bartendr.barback.user

import io.bartendr.barback.model.BaseEntity
import java.util.*
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class Session(
        val key: String = UUID.randomUUID().toString()
) : BaseEntity<Long>()
package io.bartendr.barback.poll

import com.fasterxml.jackson.annotation.JsonIgnore
import io.bartendr.barback.model.BaseEntity
import javax.persistence.ElementCollection
import javax.persistence.ManyToOne

class ChoiceResult(
        var poll: Poll,
        var text: String,
        var total: Int
) : BaseEntity<Long>()
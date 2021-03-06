package io.bartendr.barback.poll

import com.fasterxml.jackson.annotation.JsonIgnore
import io.bartendr.barback.model.BaseEntity
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class PollChoice(
        @ManyToOne var poll: Poll,
        var text: String,
        @Column(unique=true) @JsonIgnore @ElementCollection var hashes: MutableList<String> = mutableListOf()
) : BaseEntity<Long>()
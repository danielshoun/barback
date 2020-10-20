package io.bartendr.barback.poll

import io.bartendr.barback.model.BaseEntity
import io.bartendr.barback.user.User
import java.util.*
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.ManyToMany
import javax.persistence.OneToMany

@Entity
class Poll(
        var question: String,
        @ManyToMany var usersVotedIn: MutableList<User> = mutableListOf(),
        var startTime: Date,
        var endTime: Date
) : BaseEntity<Long>()
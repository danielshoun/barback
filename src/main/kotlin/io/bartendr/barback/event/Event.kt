package io.bartendr.barback.event

import com.fasterxml.jackson.annotation.JsonIgnore
import io.bartendr.barback.model.BaseEntity
import io.bartendr.barback.organization.Organization
import io.bartendr.barback.user.User
import java.util.*
import javax.persistence.Entity
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import kotlin.streams.asSequence

@Entity
class Event(
        var name: String,
        var value: Int,
        var startTime: Date,
        var closeTime: Date,
        var secret: String = generateSecret(),
        @ManyToOne var requester: User,
        @ManyToOne var approvedBy: User? = null,
        @ManyToOne var organization: Organization,
        @ManyToOne var category: EventCategory,
        @ManyToMany(targetEntity = User::class) var attended: MutableList<User> = mutableListOf(),
        @ManyToMany(targetEntity = User::class) var notAttended: MutableList<User> = mutableListOf(),
        var closed: Boolean = false
) : BaseEntity<Long>() {

    companion object {
        fun generateSecret(): String {
            val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            return Random().ints(5, 0, source.length)
            .asSequence()
            .map(source::get)
            .joinToString("")
        }
    }

}
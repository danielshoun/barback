package io.bartendr.barback.user

import com.fasterxml.jackson.annotation.JsonIgnore
import io.bartendr.barback.event.Event
import io.bartendr.barback.model.BaseEntity
import io.bartendr.barback.organization.Organization
import io.bartendr.barback.school.School
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "users")
class User(
        var firstName: String,
        var lastName: String,
        var emailAddress: String,
        @JsonIgnore var emailVerified: Boolean = false,
        @JsonIgnore var emailVerificationToken: String = UUID.randomUUID().toString(),
        @JsonIgnore var hashedPassword: String,
        @JsonIgnore var forgottenPassword: Boolean = false,
        @JsonIgnore var forgottenPasswordToken: String? = null,
        var dateOfBirth: Date,
        @ManyToOne var school: School? = null,
        @JsonIgnore @OneToMany(cascade = [CascadeType.ALL]) var sessions: MutableList<Session> = mutableListOf(),
        @ManyToMany var organizations: MutableList<Organization> = mutableListOf(),
        var isWebsiteAdmin: Boolean = false
) : BaseEntity<Long>()
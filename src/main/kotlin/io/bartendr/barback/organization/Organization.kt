package io.bartendr.barback.organization

import com.fasterxml.jackson.annotation.JsonIgnore
import io.bartendr.barback.event.EventCategory
import io.bartendr.barback.model.BaseEntity
import io.bartendr.barback.school.School
import io.bartendr.barback.user.Role
import io.bartendr.barback.user.User
import javax.persistence.*

@Entity
class Organization(
        var name: String,
        var ref: String,
        @JsonIgnore var secret: String,
        @ManyToOne var school: School,
        @JsonIgnore @Embedded var organizationSettings: OrganizationSettings = OrganizationSettings()
        ) : BaseEntity<Long>()
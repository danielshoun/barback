package io.bartendr.barback.model

import org.springframework.data.domain.Persistable
import org.springframework.data.util.ProxyUtils
import java.io.Serializable
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.Transient

@MappedSuperclass
abstract class BaseEntity<T : Serializable> : Persistable<T> {

    companion object {
        private val serialVersionUID = -5554308939380869754L
    }

    @Id
    @GeneratedValue
    private var id: T? = null

    override fun getId(): T? {
        return id
    }

    @Transient
    override fun isNew() = null == getId()

    override fun toString() = "Entity of type ${this.javaClass.name} with id: $id"

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if(this === other) return true

        if(javaClass != ProxyUtils.getUserClass(other)) return false

        other as BaseEntity<*>

        return if(null == this.getId()) false else this.getId() == other.getId()
    }

    override fun hashCode(): Int {
        return 31
    }

}
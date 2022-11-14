package com.prosstobro.reviewbot.domain

import javax.persistence.*

@Entity(name = "usr")
class User(val tgLogin: String, val chatId: Long, val firstName: String, val lastName: String ) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1

    @Enumerated(value = EnumType.STRING)
    @ElementCollection(targetClass = Role::class, fetch = FetchType.EAGER)
    val roles: MutableSet<Role> = mutableSetOf()

    override fun toString(): String {
        return "User(tgLogin='$tgLogin', chatId=$chatId, firstName='$firstName', lastName='$lastName', id=$id, roles=$roles)"
    }

}




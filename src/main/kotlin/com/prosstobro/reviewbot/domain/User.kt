package com.prosstobro.reviewbot.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class User(
    var tgLogin: String,
    var chatId: Long,
    var firstName: String,
    var lastName: String,
) {
    @Id
    var id: Long = -1
    var roles: MutableSet<Role> = mutableSetOf()

    companion object {
        const val SEQUENCE_NAME = "USER_SEQUENCE"
    }

    override fun toString(): String {
        return "User(tgLogin='$tgLogin', chatId=$chatId, firstName='$firstName', lastName='$lastName', id=$id, roles=$roles)"
    }
}




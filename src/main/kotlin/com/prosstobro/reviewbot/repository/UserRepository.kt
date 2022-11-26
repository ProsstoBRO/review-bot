package com.prosstobro.reviewbot.repository

import com.prosstobro.reviewbot.domain.Role
import com.prosstobro.reviewbot.domain.User
import org.springframework.data.mongodb.repository.MongoRepository
import java.math.BigInteger

interface UserRepository : MongoRepository<User, Long> {
    fun findByChatId(chatId: Long): User?
    fun findAllByRoles(roles: Role): Set<User>
    fun findAllByRolesAndIdNot(roles: Role, userId: Long): Set<User>
    fun findByTgLogin(tgLogin: String): User?
}
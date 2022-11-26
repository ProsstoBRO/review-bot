package com.prosstobro.reviewbot.service

import com.prosstobro.reviewbot.domain.Role
import com.prosstobro.reviewbot.domain.Role.DEVELOPER
import com.prosstobro.reviewbot.domain.Role.REVIEWER
import com.prosstobro.reviewbot.domain.User
import com.prosstobro.reviewbot.repository.UserRepository
import com.prosstobro.reviewbot.utils.DbSequenceGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(val userRepository: UserRepository, val dbSequenceGenerator: DbSequenceGenerator) {
    @Transactional
    fun changeUserRoles(tgLogin: String, roles: Set<Role>) {
        val user = userRepository.findByTgLogin(tgLogin)
        if (user != null) {
            user.roles.clear()
            user.roles.addAll(roles)
        }
    }

    fun register(user: User) {
        user.roles.addAll(listOf(REVIEWER, DEVELOPER))
        user.id = dbSequenceGenerator.getNextSequence(User.SEQUENCE_NAME)
        userRepository.save(user)
    }
}
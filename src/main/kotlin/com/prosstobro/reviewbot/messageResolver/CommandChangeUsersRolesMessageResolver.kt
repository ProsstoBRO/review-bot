package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.repository.UserRepository
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class CommandChangeUsersRolesMessageResolver(val usersRepository: UserRepository, val keyboardUtils: KeyboardUtils) :
    MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request == "/change_users_roles"
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val tgResponses: ArrayList<TgResponse> = arrayListOf()
        val users = usersRepository.findAll().stream().map { user -> user.tgLogin + "(${user.roles})" }
            .collect(Collectors.joining(","))
        tgResponses.add(
            TgResponse(
                request.chatId,
                "Пользователи и роли:\n$users\n\nЧтобы изменить роли, отправьте логин пользователя и массив его ролей по аналогии:\nprosstobro([DEVELOPER, REVIEWER])",
                null
            )
        )
        return tgResponses
    }
}
package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.Role
import com.prosstobro.reviewbot.domain.Role.ADMIN
import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.UserService
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class ChangeUsersRolesMessageResolver(val userService: UserService, val keyboardUtils: KeyboardUtils) :
    MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request.contains(Regex("[A-Za-z0-9]+\\([^)]*\\)"))
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val tgResponses: ArrayList<TgResponse> = arrayListOf()

        if(request.user?.roles?.contains(ADMIN) == true) {
            val login = request.data.substring(0, request.data.indexOf("("))
            val roles = request.data.replace(" ", "").substring(request.data.indexOf("["), request.data.indexOf("]"))
                .replace("[", "").replace("]", "")
                .replace("(", "").replace(")", "")
                .split(",")
                .stream()
                .map { role -> Role.valueOf(role) }.collect(Collectors.toSet())

            userService.changeUserRoles(login, roles)

            tgResponses.add(TgResponse(request.chatId, "Роли ($roles) для пользователя $login успешно изменены", null))
        } else {
            tgResponses.add(TgResponse(request.chatId, "Недостаточно прав", null))
        }
        return tgResponses
    }
}
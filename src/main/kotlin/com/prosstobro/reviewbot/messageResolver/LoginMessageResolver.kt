package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class LoginMessageResolver(val userService: UserService) : MessageResolver {

    @Value("\${bot.password}")
    private val botPassword: String = ""

    override fun requestTypeIsMatched(request: TgRequest): Boolean {
        return false
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        if ((request.user?.id == -1L) && request.data == botPassword) {
            userService.register(request.user)
            return listOf(TgResponse(request.chatId, "Вы успешно зарегистрировались!", null))
        } else if ((request.user?.id == -1L) && (request.data != botPassword)) {
            return listOf(
                TgResponse(request.chatId, "Вы не зарегистрированы! Обратитесь к администратору бота", null)
            )
        } else if ((request.user?.id == -1L) && (request.user.chatId != request.chatId)) {
            return listOf(
                TgResponse(request.chatId, "Вы не зарегистрированы! Обратитесь к администратору бота", null)
            )
        }
        return listOf()
    }

}
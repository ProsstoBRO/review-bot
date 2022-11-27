package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import org.springframework.stereotype.Component

@Component
class UnknownRequestMessageResolver : MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return true
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        return listOf(TgResponse(request.chatId, "Ошибка! Неизвестный тип сообщения.", null))
    }
}
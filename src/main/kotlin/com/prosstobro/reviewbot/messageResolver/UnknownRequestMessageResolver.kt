package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.domain.TgRequest
import org.springframework.stereotype.Component

@Component
class UnknownRequestMessageResolver : MessageResolver {

    override fun requestTypeIsMatched(request: TgRequest): Boolean {
        return true
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        return listOf(TgResponse(request.chatId, "Ошибка! Неизвестный тип сообщения.", null))
    }
}
package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component

@Component
class CommandStartMessageResolver(val keyboardUtils: KeyboardUtils) : MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request == "/start"
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        return listOf(
            TgResponse(request.chatId, "Выберете действие:", keyboardUtils.createStartCommandKeyboard(request.user!!.roles))
        )
    }

}
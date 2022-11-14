package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component

@Component
class CommandStartMessageResolver(val keyboardUtils: KeyboardUtils) : MessageResolver {

    override fun requestTypeIsMatched(request: TgRequest): Boolean {
        return request.data == "/start"
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val tgResponses: ArrayList<TgResponse> = arrayListOf()
        val keyboard = keyboardUtils.createStartCommandKeyboard(request.user!!.roles)
        tgResponses.add(TgResponse(request.chatId, "Выберете действие:", keyboard))
        return tgResponses
    }

}
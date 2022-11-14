package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.domain.TgRequest
import org.springframework.stereotype.Component

@Component
class CommandCreateMessageResolver : MessageResolver {

    override fun requestTypeIsMatched(request: TgRequest): Boolean {
        return request.data == "/create"
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val tgResponses: ArrayList<TgResponse> = arrayListOf()
        tgResponses.add(TgResponse(request.chatId, "Отправь ссылку на задачу", null))
        return tgResponses
    }
}
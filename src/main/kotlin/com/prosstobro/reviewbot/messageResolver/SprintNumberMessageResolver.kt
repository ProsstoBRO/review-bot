package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.utils.SprintNumberCalculator
import org.springframework.stereotype.Component

@Component
class SprintNumberMessageResolver(
    val sprintNumberCalculator: SprintNumberCalculator
) : MessageResolver {
    override fun requestTypeIsMatched(request: String): Boolean {
        return request == "/sprint"
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        return listOf(
            TgResponse(
                request.chatId,
                sprintNumberCalculator.getSprintNumber(),
                null
            )
        )
    }
}
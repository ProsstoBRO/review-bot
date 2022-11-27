package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse

interface MessageResolver {
    fun requestTypeIsMatched(request: String): Boolean
    fun processAndCreateAnswer(request: TgRequest): List<TgResponse>
}
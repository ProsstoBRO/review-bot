package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.domain.TgRequest

interface MessageResolver {
    fun requestTypeIsMatched(request: TgRequest): Boolean
    fun processAndCreateAnswer(request: TgRequest): List<TgResponse>
}
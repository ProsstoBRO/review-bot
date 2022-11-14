package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.JiraTaskService
import org.springframework.stereotype.Component

@Component
class StartReviewMessageResolver(val jiraTaskService: JiraTaskService) : MessageResolver {

    override fun requestTypeIsMatched(request: TgRequest): Boolean {
        return request.data.contains(Regex("/start_review_(\\d+)"))
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val jiraTaskId = request.data.split("_").last()
        val changedTask = jiraTaskService.startReview(jiraTaskId.toLong())
        return listOf(
            TgResponse(request.chatId, "Задача ${changedTask.url} взята в ревью. ", null),
            TgResponse(changedTask.developer.chatId, "@${changedTask.reviewer?.tgLogin} начал ревью", null)
        )
    }
}
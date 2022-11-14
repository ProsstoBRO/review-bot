package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.JiraTaskService
import org.springframework.stereotype.Component

@Component
class ReturnReviewToWorkMessageResolver(
    val jiraTaskService: JiraTaskService
) : MessageResolver {

    override fun requestTypeIsMatched(request: TgRequest): Boolean {
        return request.data.contains(Regex("/return_review_to_work_(\\d+)"))
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val jiraTaskId = request.data.split("_").last()
        val changedTask = jiraTaskService.returnTask(jiraTaskId.toLong())
        return listOf(
            TgResponse(changedTask.developer.chatId, "Задача ${changedTask.name} вернулась с ревью", null),
            TgResponse(request.chatId, "Задача ${changedTask.name} отправлена на доработку", null)
        )
    }
}
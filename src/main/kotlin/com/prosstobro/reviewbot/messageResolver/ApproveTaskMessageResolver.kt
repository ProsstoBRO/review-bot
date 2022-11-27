package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.JiraTaskService
import org.springframework.stereotype.Component

@Component
class ApproveTaskMessageResolver(val jiraTaskService: JiraTaskService) : MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request.contains(Regex("/approve_review_(\\d+)"))
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val changedTask = jiraTaskService.approveTask(request.data.split("_").last().toLong())
        return listOf(
            TgResponse(request.chatId, "Задача ${changedTask.name} одобрена!", null),
            TgResponse(
                changedTask.developer.chatId,
                "Задача ${changedTask.name} одобрена! Не забудь протестить на ДСО и закрыть потом в Jira!",
                null
            )
        )
    }
}
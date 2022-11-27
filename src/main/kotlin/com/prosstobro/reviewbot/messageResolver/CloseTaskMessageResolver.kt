package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.JiraTaskService
import org.springframework.stereotype.Component

@Component
class CloseTaskMessageResolver(val jiraTaskService: JiraTaskService): MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request.contains(Regex("/close_task_(\\d+)"))
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val taskId = request.data.split("_").last().toLong()
        val closedTask = jiraTaskService.closeTask(taskId)
        return listOf(TgResponse(request.chatId, "Задача ${closedTask.name} успешно закрыта!", null))
    }
}
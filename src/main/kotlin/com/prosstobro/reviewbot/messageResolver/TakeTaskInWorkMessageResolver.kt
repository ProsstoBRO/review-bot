package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.JiraTaskService
import org.springframework.stereotype.Component

@Component
class TakeTaskInWorkMessageResolver (val jiraTaskService: JiraTaskService) : MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request.contains(Regex("/take_task_in_work_(\\d+)"))
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val taskId = request.data.split("_").last().toLong()
        jiraTaskService.takeTaskInWork(taskId)
        return listOf(TgResponse(request.chatId, "Вы взяли задачу на доработку", null))
    }
}
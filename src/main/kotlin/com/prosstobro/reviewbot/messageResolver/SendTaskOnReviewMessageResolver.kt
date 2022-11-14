package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.JiraTaskService
import org.springframework.stereotype.Component

@Component
class SendTaskOnReviewMessageResolver(val jiraTaskService: JiraTaskService) : MessageResolver {

    override fun requestTypeIsMatched(request: TgRequest): Boolean {
        return request.data.contains(Regex("/send_task_on_review_(\\d+)"))
    }


    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val taskId = request.data.split("_").last().toLong()
        val changedTask = jiraTaskService.sendTaskToReview(taskId)
        return listOf(
            TgResponse(request.chatId, "Задача ${changedTask.name} отправлена на ревью", null),
            TgResponse(
                changedTask.reviewer!!.chatId,
                "Задача ${changedTask.name} готова к ревью",
                null
            )
        )
    }
}
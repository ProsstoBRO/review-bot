package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.JiraTaskService
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component

@Component
class CommandMyReviewListMessageResolver(val jiraTaskService: JiraTaskService, val keyboardUtils: KeyboardUtils) :
    MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request == "/tasks_in_review"
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val tasksForReview = jiraTaskService.getTasksForReview(request.user!!.id)
        return when (tasksForReview.isNotEmpty()) {
            true -> listOf(
                TgResponse(
                    request.chatId,
                    "Задачин на ревью",
                    keyboardUtils.createTasksForReviewKeyboard(tasksForReview)
                )
            )
            false -> listOf(
                TgResponse(request.chatId,"Задач на ревью нет",null)
            )
        }
    }
}
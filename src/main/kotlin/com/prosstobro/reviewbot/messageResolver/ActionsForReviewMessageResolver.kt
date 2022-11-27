package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.JiraTaskStatus.IN_REVIEW
import com.prosstobro.reviewbot.domain.JiraTaskStatus.WAITING_FOR_REVIEW
import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.repository.JiraTaskRepository
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component

@Component
class ActionsForReviewMessageResolver(val jiraTaskRepository: JiraTaskRepository, val keyboardUtils: KeyboardUtils): MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request.contains(Regex("/actions_for_review_(\\d+)"))
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val taskId = request.data.split("_").last().toLong()
        val task = jiraTaskRepository.findById(taskId).get()

        return if (WAITING_FOR_REVIEW == task.status)
            listOf(TgResponse(request.chatId, "Выберите действие", keyboardUtils.createActionsForTaskWaitingForReview(task.id.toString())))
        else if(IN_REVIEW == task.status)
            listOf(TgResponse(request.chatId, "Выберите действие", keyboardUtils.createActionsForTaskInReview(task.id.toString())))
        else
            listOf(TgResponse(request.chatId, "Для данного статуса(${task.status.name}) нет действий", null))
    }
}
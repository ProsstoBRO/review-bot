package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.JiraTaskStatus.*
import com.prosstobro.reviewbot.domain.JiraTaskType.DEFECT
import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.repository.JiraTaskRepository
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component

@Component
class ActionsForTaskMessageResolver(val jiraTaskRepository: JiraTaskRepository, val keyboardUtils: KeyboardUtils) :
    MessageResolver {

    override fun requestTypeIsMatched(request: TgRequest): Boolean {
        return request.data.contains(Regex("/actions_for_task_(\\d+)"))
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val taskId = request.data.split("_").last().toLong()
        val task = jiraTaskRepository.findById(taskId).get()


        return if (CREATED == task.status && DEFECT == task.type)
            listOf(TgResponse(request.chatId, "Выберите действие", keyboardUtils.createActionsForCreatedDefect(task)))
        else if (NOT_APPROVED == task.status)
            listOf(TgResponse(request.chatId, "Выберите действие", keyboardUtils.createActionsForTaskNotApproved(task)))
        else if (APPROVED == task.status)
            listOf(TgResponse(request.chatId, "Выберите действие", keyboardUtils.createActionsForTaskApproved(task)))
        else if (IN_WORK == task.status)
            listOf(TgResponse(request.chatId, "Выберите действие", keyboardUtils.createActionsForTaskInWork(task)))
        else
            listOf(TgResponse(request.chatId, "Для данного статуса(${task.status.name}) нет действий", keyboardUtils.createChangeReviewer(task)))
    }
}
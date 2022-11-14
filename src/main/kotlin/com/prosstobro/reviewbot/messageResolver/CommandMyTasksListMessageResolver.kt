package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.JiraTaskService
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component

@Component
class CommandMyTasksListMessageResolver(
    val jiraTaskService: JiraTaskService,
    val keyboardUtils: KeyboardUtils
) : MessageResolver {

    override fun requestTypeIsMatched(request: TgRequest): Boolean {
        return request.data == "/my_tasks"
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val developer = request.user
        if (developer != null) {
            val jiraTasks = jiraTaskService.getTasksForDeveloper(developer)
            val jiraTasksInfo = jiraTasks.joinToString(transform = { "${it.name}(${it.status})" })
            return if (jiraTasksInfo.isEmpty())
                listOf(TgResponse(request.chatId, "У вас нет активных задач", null))
            else
                listOf(TgResponse(request.chatId, "Ваши активные задачи", keyboardUtils.createTasksForDeveloperKeyboard(jiraTasks)))
        }
        return listOf(TgResponse(request.chatId, "Что-то пошло не так", null))
    }
}
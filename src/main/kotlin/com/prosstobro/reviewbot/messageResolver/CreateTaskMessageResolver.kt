package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.Role.REVIEWER
import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.repository.UserRepository
import com.prosstobro.reviewbot.service.JiraTaskService
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component

@Component
class CreateTaskMessageResolver(
    val userRepository: UserRepository,
    val jiraTaskService: JiraTaskService,
    val keyboardUtils: KeyboardUtils
) : MessageResolver {

    override fun requestTypeIsMatched(request: TgRequest): Boolean {
        return request.data.startsWith("https://task.corp.dev")
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val tgResponses: ArrayList<TgResponse> = arrayListOf()
        val developer = userRepository.findByChatId(request.chatId)
        if (developer != null) {
            val jiraTask = jiraTaskService.createJiraTask(request.data, request.data.split("/").last(), developer)
            tgResponses.add(
                TgResponse(
                    developer.chatId,
                    "Задача ${request.data.split("/").last()} создана! Выберите ревьюера:",
                    keyboardUtils.createReviewersListKeyboard(userRepository.findAllByRoles(REVIEWER), jiraTask.id.toString())
                )
            )
        }
        return tgResponses
    }
}
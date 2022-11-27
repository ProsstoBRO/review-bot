package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.JiraTaskType
import com.prosstobro.reviewbot.domain.JiraTaskType.STORY
import com.prosstobro.reviewbot.domain.Role.REVIEWER
import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.repository.UserRepository
import com.prosstobro.reviewbot.service.JiraTaskService
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class ChangeTaskTypeMessageResolver(
    val jiraTaskService: JiraTaskService,
    val keyboardUtils: KeyboardUtils,
    val userRepository: UserRepository
) : MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request.startsWith("/type_")
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val taskId = request.data.split("_").last()
        val newTaskType = if (request.data.contains("defect")) JiraTaskType.DEFECT else STORY
        jiraTaskService.changeTaskType(taskId.toLong(), newTaskType)
        return if (STORY == newTaskType) {
            listOf(
                TgResponse(
                    request.chatId,
                    "Выберите ревьюера",
                    keyboardUtils.createReviewersListKeyboard(userRepository.findAllByRoles(REVIEWER), taskId)
                )
            )
        } else {
            createTgResponsesForReviewers(request.chatId).plus(
                TgResponse(
                    request.chatId,
                    "Задача успешно создана! Все ревьюеры будут оповещены",
                    null
                )
            )
        }
    }

    private fun createTgResponsesForReviewers(chatId: Long): List<TgResponse> {
        return userRepository.findAllByRoles(REVIEWER).stream()
            .filter { user -> user.chatId != chatId }
            .map { user -> TgResponse(user.chatId, "НОВЫЙ ДЕФЕКТ на ревью", null) }
            .collect(Collectors.toList())
    }
}
package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.JiraTaskStatus.CREATED
import com.prosstobro.reviewbot.domain.JiraTaskType.DEFECT
import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.repository.JiraTaskRepository
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component

@Component
class CommandDefectsOnReviewListMessageResolver(
    val jiraTaskRepository: JiraTaskRepository,
    val keyboardUtils: KeyboardUtils
) : MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request == "/defects_on_review"
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val defects = jiraTaskRepository.findAllByTypeAndStatus(DEFECT, CREATED)
        return if (defects.isNotEmpty()) {
            listOf(
                TgResponse(
                    request.chatId,
                    "Выберите дефект:",
                    keyboardUtils.createTasksForReviewKeyboard(defects)
                )
            )
        } else {
            listOf(
                TgResponse(
                    request.chatId,
                    "Сейчас нет дефектов для ревью",
                    null
                )
            )
        }
    }
}
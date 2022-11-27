package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.Role.REVIEWER
import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.repository.UserRepository
import com.prosstobro.reviewbot.utils.KeyboardUtils
import org.springframework.stereotype.Component

@Component
class ChangeReviewerMessageResolver(val userRepository: UserRepository, val keyboardUtils: KeyboardUtils) :
    MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request.contains(Regex("/change_reviewer_for_task_(\\d+)"))
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        val taskId = request.data.split("_").last()
        return listOf(
            TgResponse(
                request.chatId,
                "Выберите ревьюера:",
                keyboardUtils.createReviewersListKeyboard(userRepository.findAllByRoles(REVIEWER), taskId)
            )
        )
    }
}
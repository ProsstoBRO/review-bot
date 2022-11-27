package com.prosstobro.reviewbot.messageResolver

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.service.JiraTaskService
import org.springframework.stereotype.Component

@Component
class SetReviewerForTaskMessageResolver(val jiraTaskService: JiraTaskService) : MessageResolver {

    override fun requestTypeIsMatched(request: String): Boolean {
        return request.contains(Regex("/set_reviewer_(\\d+)_for_task_(\\d+)"))
    }

    override fun processAndCreateAnswer(request: TgRequest): List<TgResponse> {
        var tgResponses: ArrayList<TgResponse> = arrayListOf()
        val taskId: Long = request.data.split("_")[5].toLong()
        val reviewerId: Long = request.data.split("_")[2].toLong()

        val changedTask = jiraTaskService.setReviewerForTask(taskId, reviewerId)
        if (changedTask.reviewer != null) {
            tgResponses.add(
                TgResponse(
                    changedTask.reviewer!!.chatId,
                    "Тебе назначена задача на ревью ${changedTask.name}",
                    null
                )
            )
        }
        tgResponses.add(TgResponse(request.chatId, "Ревьюер был успешно назначен", null))
        return tgResponses
    }
}
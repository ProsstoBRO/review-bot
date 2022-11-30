package com.prosstobro.reviewbot.utils

import com.prosstobro.reviewbot.domain.JiraTask
import com.prosstobro.reviewbot.domain.Role
import com.prosstobro.reviewbot.domain.Role.*
import com.prosstobro.reviewbot.domain.User
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.stream.Collectors

@Component
class KeyboardUtils {

    fun createStartCommandKeyboard(roles: Set<Role>): InlineKeyboardMarkup {
        val buttons = mutableListOf<List<InlineKeyboardButton>>()
        if (roles.contains(DEVELOPER))
            buttons.add(createButton("Создать задачу", "/create"))
        if (roles.contains(DEVELOPER))
            buttons.add(createButton("Список моих задач", "/my_tasks"))
        if (roles.contains(REVIEWER))
            buttons.add(createButton("Список моих ревью", "/tasks_in_review"))
        if (roles.contains(REVIEWER))
            buttons.add(createButton("Список дефектов на ревью", "/defects_on_review"))
        if (roles.contains(ADMIN))
            buttons.add(createButton("Статус задач", "/tasks_status"))
        if (roles.contains(ADMIN))
            buttons.add(createButton("Изменить роли", "/change_users_roles"))

        return InlineKeyboardMarkup(buttons)
    }

    fun createTaskTypesListKeyboard(taskId: String): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(
            listOf(
                createButton("Реализация", "/type_story_${taskId}"),
                createButton("Дефект", "/type_defect_${taskId}")
            )
        )
    }

    fun createReviewersListKeyboard(reviewers: Set<User>, taskId: String): InlineKeyboardMarkup {
        val reviewersButtons = reviewers.stream()
            .map { reviewer ->
                createButton(
                    "@${reviewer.tgLogin}",
                    "/set_reviewer_${reviewer.id}_for_task_${taskId}"
                )
            }.collect(Collectors.toList())
        return InlineKeyboardMarkup(reviewersButtons)
    }

    fun createTasksForReviewKeyboard(jiraTasks: List<JiraTask>): InlineKeyboardMarkup {
        val tasksButtons = jiraTasks.stream()
            .map { jiraTask ->
                createButton("${jiraTask.name} (${jiraTask.status.description})", "/actions_for_review_${jiraTask.id}")
            }
            .collect(Collectors.toList())
        return InlineKeyboardMarkup(tasksButtons)
    }

    fun createActionsForTaskInReview(taskId: String): InlineKeyboardMarkup {
        val actionsButtons = listOf(
            createButton("Одобрить", "/approve_review_${taskId}"),
            createButton("Вернуть на доработку", "/return_review_to_work_${taskId}")
        )
        return InlineKeyboardMarkup(actionsButtons)
    }

    fun createActionsForTaskWaitingForReview(taskId: String): InlineKeyboardMarkup? {
        val actionsButtons = listOf(
            createButton("Взять в ревью", "/start_review_${taskId}")
        )
        return InlineKeyboardMarkup(actionsButtons)
    }

    fun createTasksForDeveloperKeyboard(jiraTasks: List<JiraTask>): InlineKeyboardMarkup {
        val tasksButtons = jiraTasks.stream()
            .map { jiraTask ->
                createButton(
                    "${jiraTask.name}: ${jiraTask.status.description}",
                    "/actions_for_task_${jiraTask.id}"
                )
            }
            .collect(Collectors.toList())
        return InlineKeyboardMarkup(tasksButtons)
    }

    fun createActionsForTaskNotApproved(jiraTask: JiraTask): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(
            listOf(
                createButton("Взять в доработку", "/take_task_in_work_${jiraTask.id}"),
                createButton("Изменить ревьюера", "/change_reviewer_for_task_${jiraTask.id}")
            )
        )
    }

    fun createActionsForCreatedDefect(jiraTask: JiraTask): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(
            listOf(
                createButton("Взять в ревью", "/start_review_${jiraTask.id}")
            )
        )
    }

    fun createActionsForTaskApproved(jiraTask: JiraTask): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(
            listOf(
                createButton("Закрыть", "/close_task_${jiraTask.id}"),
                createButton("Изменить ревьюера", "/change_reviewer_for_task_${jiraTask.id}")
            )
        )
    }

    fun createActionsForTaskInWork(jiraTask: JiraTask): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(
            listOf(
                createButton("Отправить на ревью", "/send_task_on_review_${jiraTask.id}"),
                createButton("Изменить ревьюера", "/change_reviewer_for_task_${jiraTask.id}")
            )
        )
    }

    fun createButton(text: String, callbackData: String): List<InlineKeyboardButton> {
        val button = InlineKeyboardButton(text)
        button.callbackData = callbackData
        return listOf(button)
    }

    fun createChangeReviewer(jiraTask: JiraTask): InlineKeyboardMarkup? {
        return InlineKeyboardMarkup(
            listOf(
                createButton("Изменить ревьюера", "/change_reviewer_for_task_${jiraTask.id}")
            )
        )
    }
}
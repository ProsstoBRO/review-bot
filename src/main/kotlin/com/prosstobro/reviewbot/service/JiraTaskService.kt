package com.prosstobro.reviewbot.service

import com.prosstobro.reviewbot.domain.JiraTask
import com.prosstobro.reviewbot.domain.JiraTaskStatus.*
import com.prosstobro.reviewbot.domain.JiraTaskType
import com.prosstobro.reviewbot.domain.JiraTaskType.UNKNOWN
import com.prosstobro.reviewbot.domain.User
import com.prosstobro.reviewbot.repository.JiraTaskRepository
import com.prosstobro.reviewbot.repository.UserRepository
import com.prosstobro.reviewbot.utils.DbSequenceGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class JiraTaskService(
    val dbSequenceGenerator: DbSequenceGenerator,
    val jiraTaskRepository: JiraTaskRepository,
    val userRepository: UserRepository
) {

    @Transactional
    fun createJiraTask(url: String, name: String, developer: User): JiraTask {
        val jiraTask = JiraTask(url, name, developer, null, CREATED, UNKNOWN)
        jiraTask.id = dbSequenceGenerator.getNextSequence(JiraTask.SEQUENCE_NAME)
        return jiraTaskRepository.save(jiraTask)
    }

    @Transactional
    fun setReviewerForTask(taskId: Long, reviewerId: Long): JiraTask {
        val task = jiraTaskRepository.findById(taskId).get()
        val reviewer = userRepository.findById(reviewerId).get()

        task.reviewer = reviewer
        task.status = WAITING_FOR_REVIEW

        return jiraTaskRepository.save(task)
    }

    @Transactional
    fun getTasksForReview(reviewerId: Long): List<JiraTask> {
        val reviewer = userRepository.findById(reviewerId).get()
        return jiraTaskRepository.findAllByReviewerAndStatusIn(reviewer, listOf(WAITING_FOR_REVIEW, IN_REVIEW))
    }

    @Transactional
    fun startReview(taskId: Long, reviewerId: Long): JiraTask {
        val task = jiraTaskRepository.findById(taskId).get()
        if (task.reviewer == null) {
            setReviewerForTask(taskId, reviewerId)
        }
        task.status = IN_REVIEW
        return jiraTaskRepository.save(task)
    }

    @Transactional
    fun approveTask(taskId: Long): JiraTask {
        val task = jiraTaskRepository.findById(taskId).get()
        task.status = APPROVED
        return jiraTaskRepository.save(task)
    }

    @Transactional
    fun returnTask(taskId: Long): JiraTask {
        val task = jiraTaskRepository.findById(taskId).get()
        task.status = NOT_APPROVED
        return jiraTaskRepository.save(task)
    }

    @Transactional
    fun getTasksForDeveloper(developer: User): List<JiraTask> {
        return jiraTaskRepository.findAllByDeveloperAndStatusNotIn(developer, listOf(CLOSED))
    }

    @Transactional
    fun takeTaskInWork(taskId: Long) {
        val task = jiraTaskRepository.findById(taskId).get()
        task.status = IN_WORK
        jiraTaskRepository.save(task)
    }

    @Transactional
    fun sendTaskToReview(taskId: Long): JiraTask {
        val task = jiraTaskRepository.findById(taskId).get()
        task.status = WAITING_FOR_REVIEW
        return jiraTaskRepository.save(task)
    }

    @Transactional
    fun closeTask(taskId: Long): JiraTask {
        val task = jiraTaskRepository.findById(taskId).get()
        task.status = CLOSED
        return jiraTaskRepository.save(task)
    }

    @Transactional
    fun changeTaskType(taskId: Long, newTaskType: JiraTaskType): JiraTask {
        val task = jiraTaskRepository.findById(taskId).get()
        task.type = newTaskType
        return jiraTaskRepository.save(task)
    }
}
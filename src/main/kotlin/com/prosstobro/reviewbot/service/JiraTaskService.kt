package com.prosstobro.reviewbot.service

import com.prosstobro.reviewbot.domain.JiraTask
import com.prosstobro.reviewbot.domain.JiraTaskStatus.*
import com.prosstobro.reviewbot.domain.Role
import com.prosstobro.reviewbot.domain.User
import com.prosstobro.reviewbot.repository.JiraTaskRepository
import com.prosstobro.reviewbot.repository.UserRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class JiraTaskService(val jiraTaskRepository: JiraTaskRepository, val userRepository: UserRepository) {

    @Transactional
    fun createJiraTask(url: String, name: String, developer: User): JiraTask {
        val jiraTask = JiraTask(url, name, developer, null, CREATED)
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
    fun startReview(taskId: Long): JiraTask {
        val task = jiraTaskRepository.findById(taskId).get()
        task.status = IN_REVIEW
        return jiraTaskRepository.save(task)
    }

    @Transactional
    fun getReviewersForTask(taskId: Long): Set<User> {
        val developer = jiraTaskRepository.findById(taskId).get().developer
        return userRepository.findAllByRolesAndIdNot(Role.REVIEWER, developer.id)
    }

    @Transactional
    fun getTasksInReview(reviewerId: Long): List<JiraTask> {
        val reviewer = userRepository.findById(reviewerId).get()
        return jiraTaskRepository.findAllByReviewerAndStatusIn(reviewer, listOf(IN_REVIEW))
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

}
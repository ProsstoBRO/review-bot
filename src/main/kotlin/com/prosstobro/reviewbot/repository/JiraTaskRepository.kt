package com.prosstobro.reviewbot.repository

import com.prosstobro.reviewbot.domain.JiraTask
import com.prosstobro.reviewbot.domain.JiraTaskStatus
import com.prosstobro.reviewbot.domain.JiraTaskType
import com.prosstobro.reviewbot.domain.User
import org.springframework.data.mongodb.repository.MongoRepository

interface JiraTaskRepository : MongoRepository<JiraTask, Long> {
    fun findAllByReviewerAndStatusIn(reviewer: User, statuses: List<JiraTaskStatus>): List<JiraTask>
    fun findAllByDeveloperAndStatusNotIn(developer: User, statuses: List<JiraTaskStatus>): List<JiraTask>
    fun findAllByTypeAndStatus(jiraTaskType: JiraTaskType, jiraTaskStatus: JiraTaskStatus): List<JiraTask>
}
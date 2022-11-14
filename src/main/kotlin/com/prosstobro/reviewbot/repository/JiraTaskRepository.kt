package com.prosstobro.reviewbot.repository

import com.prosstobro.reviewbot.domain.JiraTask
import com.prosstobro.reviewbot.domain.JiraTaskStatus
import com.prosstobro.reviewbot.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface JiraTaskRepository : JpaRepository<JiraTask, Long> {
    fun findAllByReviewerAndStatusIn(reviewer: User, statuses: List<JiraTaskStatus>): List<JiraTask>
    fun findAllByDeveloperAndStatusNotIn(developer: User, statuses: List<JiraTaskStatus>): List<JiraTask>
    fun findAllByStatusIn(statuses: List<JiraTaskStatus>): List<JiraTask>
}
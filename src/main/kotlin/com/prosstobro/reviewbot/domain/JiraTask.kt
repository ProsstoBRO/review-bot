package com.prosstobro.reviewbot.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class JiraTask(
    val url: String,
    val name: String,
    val developer: User,
    var reviewer: User?,
    var status: JiraTaskStatus,
    var type: JiraTaskType
) {
    @Id
    var id: Long = -1

    companion object {
        const val SEQUENCE_NAME = "TASK_SEQUENCE"
    }
}
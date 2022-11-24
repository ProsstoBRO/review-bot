package com.prosstobro.reviewbot.domain

import javax.persistence.*
import javax.persistence.EnumType.STRING

@Entity
class JiraTask(
    val url: String,
    val name: String,
    @OneToOne
    val developer: User,
    @OneToOne
    var reviewer: User?,
    @Enumerated(value = STRING)
    var status: JiraTaskStatus,
    @Enumerated(value = STRING)
    var type: JiraTaskType
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1
}
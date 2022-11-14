package com.prosstobro.reviewbot.domain

import javax.persistence.*

@Entity
class JiraTask(
    val url: String,
    val name: String,
    @OneToOne
    val developer: User,
    @OneToOne
    var reviewer: User?,
    @Enumerated(value = EnumType.STRING)
    var status: JiraTaskStatus
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1
}
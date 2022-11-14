package com.prosstobro.reviewbot.domain

enum class JiraTaskStatus(val description: String) {
    CREATED("Создана"),
    IN_WORK("В разработке"),
    WAITING_FOR_REVIEW("Ожидает ревью"),
    IN_REVIEW("В ревью"),
    APPROVED("Одобрена"),
    NOT_APPROVED("Вернулась с ревью"),
    CLOSED("Закрыта")
}
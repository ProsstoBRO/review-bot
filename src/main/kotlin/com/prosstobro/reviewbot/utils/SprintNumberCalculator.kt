package com.prosstobro.reviewbot.utils

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Component
class SprintNumberCalculator {
    private val SPRINT_32_START = LocalDate.of(2021, 8, 4)

    fun getSprintNumber(): String {
        val today = LocalDate.now()
        val sprintNum = ChronoUnit.DAYS.between(SPRINT_32_START, today) / 14 + 32
        return "Сейчас спринт №${sprintNum}."
    }
}
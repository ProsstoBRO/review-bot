package com.prosstobro.reviewbot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.prosstobro.reviewbot.domain.JiraTask
import com.prosstobro.reviewbot.domain.User
import com.prosstobro.reviewbot.repository.JiraTaskRepository
import com.prosstobro.reviewbot.repository.UserRepository
import com.prosstobro.reviewbot.service.MigrationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Testcontainers
import utils.DatabaseContainerConfiguration
import utils.PrettyPrinter
import java.io.File

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class MigrationDataFromH2Test : DatabaseContainerConfiguration() {

    @Autowired
    lateinit var migrationService: MigrationService

    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var jiraTaskRepository: JiraTaskRepository

    final var objectMapper = ObjectMapper()

    init {
        objectMapper.setDefaultPrettyPrinter(PrettyPrinter())
    }

    @Test
    fun migrationTestUsers() {
        val jacksonMapper = jacksonObjectMapper()

        val users = userRepository.findAll()
        val usersFromJson: List<User> = jacksonMapper.readValue(readFileAsText("users.json"))

        val tasks = jiraTaskRepository.findAll()
        val tasksFromJson: List<JiraTask> = jacksonMapper.readValue(readFileAsText("tasks.json"))

        assertEquals(users.size, usersFromJson.size)
        assertEquals(tasks.size, tasksFromJson.size)

        for (i in users.indices) {
            assertEquals(users[i].id, usersFromJson[i].id)
            assertEquals(users[i].tgLogin, usersFromJson[i].tgLogin)
        }

        for (i in tasks.indices) {
            assertEquals(tasks[i].id, tasksFromJson[i].id)
            assertEquals(tasks[i].status, tasksFromJson[i].status)
            assertEquals(tasks[i].reviewer?.id, tasksFromJson[i].reviewer?.id)
            assertEquals(tasks[i].developer.id, tasksFromJson[i].developer.id)
        }
    }


    fun readFileAsText(fileName: String): String = File(fileName).readText(Charsets.UTF_8)
}
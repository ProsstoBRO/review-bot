package com.prosstobro.reviewbot.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.prosstobro.reviewbot.domain.JiraTask
import com.prosstobro.reviewbot.domain.User
import com.prosstobro.reviewbot.repository.JiraTaskRepository
import com.prosstobro.reviewbot.repository.UserRepository
import com.prosstobro.reviewbot.utils.DbSequenceGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import javax.annotation.PostConstruct

@Service
class MigrationService(
    val userRepository: UserRepository,
    val jiraTaskRepository: JiraTaskRepository,
    val dbSequenceGenerator: DbSequenceGenerator
) {
    @Transactional
    @PostConstruct
    fun migrate() {
        val jacksonMapper = jacksonObjectMapper()
        val usersFromJson: List<User> = jacksonMapper.readValue(readFileAsText("users.json"))
        dbSequenceGenerator.getNextSequence(User.SEQUENCE_NAME)

        val tasksFromJson: List<JiraTask> = jacksonMapper.readValue(readFileAsText("tasks.json"))

        for (user in usersFromJson) {
            user.id = dbSequenceGenerator.getNextSequence(User.SEQUENCE_NAME)
        }

        for (task in tasksFromJson) {
            var id = -1L
            while (id != task.id){
                id = dbSequenceGenerator.getNextSequence(JiraTask.SEQUENCE_NAME)
            }
            task.id = id
        }

        userRepository.saveAll(usersFromJson)
        jiraTaskRepository.saveAll(tasksFromJson)
    }

    fun readFileAsText(fileName: String): String = File(fileName).readText(Charsets.UTF_8)
}
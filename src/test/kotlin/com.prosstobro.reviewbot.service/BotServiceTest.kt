package com.prosstobro.reviewbot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.prosstobro.reviewbot.domain.JiraTaskStatus.CLOSED
import com.prosstobro.reviewbot.domain.Role.DEVELOPER
import com.prosstobro.reviewbot.domain.Role.REVIEWER
import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.domain.User
import com.prosstobro.reviewbot.repository.JiraTaskRepository
import com.prosstobro.reviewbot.repository.UserRepository
import com.prosstobro.reviewbot.utils.DbSequenceGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ResourceLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Testcontainers
import utils.DatabaseContainerConfiguration
import utils.PrettyPrinter


@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class BotServiceTest : DatabaseContainerConfiguration() {

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Autowired
    lateinit var botService: BotService
    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var taskRepository: JiraTaskRepository
    @Autowired
    lateinit var dbSequenceGenerator: DbSequenceGenerator


    final var objectMapper = ObjectMapper()

    var developer = User("developer", 1L, "developerName", "developerLastName")
    var reviewer = User("reviewer", 2L, "reviewerName", "reviewerLastName")


    init {
        objectMapper.setDefaultPrettyPrinter(PrettyPrinter())
    }

    @BeforeEach
    fun dataInit() {
        userRepository.deleteAll()
        taskRepository.deleteAll()

        developer.roles.add(DEVELOPER)
        developer.id = dbSequenceGenerator.getNextSequence(User.SEQUENCE_NAME)
        userRepository.save(developer)
        developer = userRepository.findByChatId(developer.chatId)!!

        reviewer.roles.add(REVIEWER)
        reviewer.id = dbSequenceGenerator.getNextSequence(User.SEQUENCE_NAME)
        userRepository.save(reviewer)
        reviewer = userRepository.findByChatId(reviewer.chatId)!!
    }

    @Test
    fun storyLifeCycle() {
        var request: TgRequest = commandStartRequest()
        var response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/commandStartResponse.json")

        request = commandCreateRequest()
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/commandCreateResponse.json")

        //create
        request = createTaskRequest()
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/createTaskResponse.json")

        //change type
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/changeTaskTypeResponse.json")

        //set reviewer
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/changeReviewerResponse.json")

        //task list - waiting_review
        request = commandMyTasksListRequest()
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/commandMyTasksListResponse_waitingReview.json")

        //review list - waiting_review
        request = commandMyReviewListRequest()
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/commandMyReviewListResponse_waitingReview.json")

        //action for review - waiting-review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/actionsForReviewResponse_waitingReview.json")

        //start review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/startReviewResponse.json")

        //review list - in_review
        request = commandMyReviewListRequest()
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/commandMyReviewListResponse_inReview.json")

        //action for review - in_review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/actionsForReviewResponse_inReview.json")

        //return review to work
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[1][0].callbackData, reviewer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/returnReviewToWorkResponse.json")

        //task list - not-approved
        request = commandMyTasksListRequest()
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/commandMyTasksListResponse_notApproved.json")

        //actions for task - not-approved
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/actionsForTaskResponse_notApproved.json")

        //take task in work
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/takeTaskInWorkResponse.json")

        //task list - in-work
        request = commandMyTasksListRequest()
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/commandMyTasksListResponse_inWork.json")

        //actions for task - in work
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/actionsForTaskResponse_inWork.json")

        //send task on review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/sendTaskOnReviewResponse.json")

        //review list - waiting_review
        request = commandMyReviewListRequest()
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/commandMyReviewListResponse_waitingReview.json")

        //action for review - waiting-review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/actionsForReviewResponse_waitingReview.json")

        //start review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/startReviewResponse.json")

        //review list - in_review
        request = commandMyReviewListRequest()
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/commandMyReviewListResponse_inReview.json")

        //action for review - in_review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/actionsForReviewResponse_inReview.json")

        //approve task
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/approveTaskResponse.json")

        //task list - approved
        request = commandMyTasksListRequest()
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/commandMyTasksListResponse_approved.json")

        //actions for task - approved
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/actionsForTaskResponse_approved.json")

        //close task - approved
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = botService.processMessage(request)
        assertResponseFromFile(response, "fullTasksLifeCycle/closeTaskResponse.json")

        assertEquals(CLOSED, taskRepository.findAll()[0].status)
    }

    private fun commandStartRequest(): TgRequest = TgRequest(developer.chatId, "/start", developer)
    private fun commandCreateRequest(): TgRequest = TgRequest(developer.chatId, "/create", developer)
    private fun createTaskRequest(): TgRequest = TgRequest(developer.chatId, "https://task.corp.dev/TASK_1234", developer)
    private fun callbackRequest(callback: String, user: User): TgRequest = TgRequest(user.chatId, callback, user)
    private fun commandMyTasksListRequest(): TgRequest = TgRequest(developer.chatId, "/my_tasks", developer)
    private fun commandMyReviewListRequest(): TgRequest = TgRequest(reviewer.chatId, "/tasks_in_review", reviewer)

    private fun assertResponseFromFile(response: List<TgResponse>, fileName: String) {
        assertEquals(
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response).replace("\n", "\r\n"),
            getJsonFromFile(fileName)
        )
    }

    private fun getJsonFromFile(fileName: String): String =
        resourceLoader.getResource("classpath:${fileName}").file.readText(charset = Charsets.UTF_8)
}
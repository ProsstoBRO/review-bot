package com.prosstobro.reviewbot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.prosstobro.reviewbot.ReviewBotApplication
import com.prosstobro.reviewbot.domain.JiraTaskStatus.CLOSED
import com.prosstobro.reviewbot.domain.Role.DEVELOPER
import com.prosstobro.reviewbot.domain.Role.REVIEWER
import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.domain.User
import com.prosstobro.reviewbot.repository.JiraTaskRepository
import com.prosstobro.reviewbot.repository.UserRepository
import com.prosstobro.reviewbot.utils.DbSequence
import com.prosstobro.reviewbot.utils.DbSequenceGenerator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.core.io.ResourceLoader
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Testcontainers
import utils.DatabaseContainerConfiguration
import utils.PrettyPrinter


@ExtendWith(SpringExtension::class)
@Testcontainers
@ContextConfiguration(
    classes = [ReviewBotServiceTestContextConfiguration::class, ReviewBotApplication::class]
)
@DataMongoTest(excludeAutoConfiguration = [EmbeddedMongoAutoConfiguration::class])
@ActiveProfiles("test")
open class ReviewBotServiceTest : DatabaseContainerConfiguration() {

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Autowired
    lateinit var reviewBotService: ReviewBotService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var taskRepository: JiraTaskRepository

    @Autowired
    lateinit var dbSequenceGenerator: DbSequenceGenerator

    @Autowired
    lateinit var operations: MongoTemplate

    private var objectMapper = ObjectMapper()

    private var developer = User("developer", 1L, "developerName", "developerLastName")
    private var reviewer = User("reviewer", 2L, "reviewerName", "reviewerLastName")

    init {
        objectMapper.setDefaultPrettyPrinter(PrettyPrinter())
    }

    @BeforeEach
    fun createUsers() {
        if (userRepository.findAll().isEmpty()) {
            developer.roles.add(DEVELOPER)
            developer.id = dbSequenceGenerator.getNextSequence(User.SEQUENCE_NAME)
            userRepository.save(developer)

            reviewer.roles.add(REVIEWER)
            reviewer.id = dbSequenceGenerator.getNextSequence(User.SEQUENCE_NAME)
            userRepository.save(reviewer)
        }

        developer = userRepository.findByChatId(developer.chatId)!!
        reviewer = userRepository.findByChatId(reviewer.chatId)!!
    }

    @AfterEach
    fun cleanUp() {
        taskRepository.deleteAll()
        val q = Query(Criteria.where("id").`is`("TASK_SEQUENCE"))
        val u: Update = Update().set("sequence", 0)
        operations.findAndModify(
            q, u,
            FindAndModifyOptions.options().returnNew(true).upsert(true), DbSequence::class.java
        )
    }

    @Test
    fun storyLifeCycle() {
        var request: TgRequest = commandStartRequest()
        var response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandStartResponse.json")

        request = commandCreateRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandCreateResponse.json")

        //create
        request = createTaskRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/createTaskResponse.json")

        //change type
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "storyFlow/changeTaskTypeResponse.json")

        //set reviewer
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "storyFlow/changeReviewerResponse.json")

        //task list - waiting_review
        request = commandMyTasksListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyTasksListResponse_waitingReview.json")

        //review list - waiting_review
        request = commandMyReviewListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyReviewListResponse_waitingReview.json")

        //action for review - waiting-review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForReviewResponse_waitingReview.json")

        //start review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/startReviewResponse.json")

        //review list - in_review
        request = commandMyReviewListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyReviewListResponse_inReview.json")

        //action for review - in_review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForReviewResponse_inReview.json")

        //return review to work
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[1][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/returnReviewToWorkResponse.json")

        //task list - not-approved
        request = commandMyTasksListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyTasksListResponse_notApproved.json")

        //actions for task - not-approved
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForTaskResponse_notApproved.json")

        //take task in work
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/takeTaskInWorkResponse.json")

        //task list - in-work
        request = commandMyTasksListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyTasksListResponse_inWork.json")

        //actions for task - in work
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForTaskResponse_inWork.json")

        //send task on review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/sendTaskOnReviewResponse.json")

        //review list - waiting_review
        request = commandMyReviewListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "storyFlow/commandMyReviewListResponse_waitingReview.json")

        //action for review - waiting-review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForReviewResponse_waitingReview.json")

        //start review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/startReviewResponse.json")

        //review list - in_review
        request = commandMyReviewListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyReviewListResponse_inReview.json")

        //action for review - in_review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForReviewResponse_inReview.json")

        //approve task
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/approveTaskResponse.json")

        //task list - approved
        request = commandMyTasksListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyTasksListResponse_approved.json")

        //actions for task - approved
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForTaskResponse_approved.json")

        //close task - approved
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/closeTaskResponse.json")

        assertEquals(CLOSED, taskRepository.findAll()[0].status)
    }

    @Test
    fun defectLifeCycle() {
        var request: TgRequest = commandStartRequest()
        var response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandStartResponse.json")

        request = commandCreateRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandCreateResponse.json")

        //create
        request = createTaskRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/createTaskResponse.json")

        //change type
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[1][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "defectFlow/changeTaskTypeResponse.json")

        //review list - created
        request = commandDefectsOnReviewListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "defectFlow/commandDefectsOnReviewListRequest.json")

        //action for review - in_review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "defectFlow/actionsForReviewResponse_created.json")

        //start review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/startReviewResponse.json")

        //review list - in_review
        request = commandMyReviewListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyReviewListResponse_inReview.json")

        //action for review - in_review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForReviewResponse_inReview.json")

        //return review to work
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[1][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/returnReviewToWorkResponse.json")

        //task list - not-approved
        request = commandMyTasksListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyTasksListResponse_notApproved.json")

        //actions for task - not-approved
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForTaskResponse_notApproved.json")

        //take task in work
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/takeTaskInWorkResponse.json")

        //task list - in-work
        request = commandMyTasksListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyTasksListResponse_inWork.json")

        //actions for task - in work
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForTaskResponse_inWork.json")

        //send task on review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/sendTaskOnReviewResponse.json")

        //review list - waiting_review
        request = commandMyReviewListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyReviewListResponse_waitingReview.json")

        //action for review - waiting-review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForReviewResponse_waitingReview.json")

        //start review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/startReviewResponse.json")

        //review list - in_review
        request = commandMyReviewListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyReviewListResponse_inReview.json")

        //action for review - in_review
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForReviewResponse_inReview.json")

        //approve task
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, reviewer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/approveTaskResponse.json")

        //task list - approved
        request = commandMyTasksListRequest()
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/commandMyTasksListResponse_approved.json")

        //actions for task - approved
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/actionsForTaskResponse_approved.json")

        //close task - approved
        request = callbackRequest(response[0].keyboardMarkup!!.keyboard[0][0].callbackData, developer)
        response = reviewBotService.processRequest(request)
        assertResponseFromFile(response, "commonFlow/closeTaskResponse.json")

        assertEquals(CLOSED, taskRepository.findAll()[0].status)
    }

    private fun commandStartRequest(): TgRequest = TgRequest(developer.chatId, "/start", developer)
    private fun commandCreateRequest(): TgRequest = TgRequest(developer.chatId, "/create", developer)
    private fun createTaskRequest(): TgRequest =
        TgRequest(developer.chatId, "https://task.corp.dev/TASK_1234", developer)

    private fun callbackRequest(callback: String, user: User): TgRequest = TgRequest(user.chatId, callback, user)
    private fun commandMyTasksListRequest(): TgRequest = TgRequest(developer.chatId, "/my_tasks", developer)
    private fun commandMyReviewListRequest(): TgRequest = TgRequest(reviewer.chatId, "/tasks_in_review", reviewer)
    private fun commandDefectsOnReviewListRequest(): TgRequest =
        TgRequest(reviewer.chatId, "/defects_on_review", reviewer)

    private fun assertResponseFromFile(response: List<TgResponse>, fileName: String) {
        assertEquals(
            getJsonFromFile(fileName),
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response).replace("\n", "\r\n")
        )
    }

    private fun getJsonFromFile(fileName: String): String =
        resourceLoader.getResource("classpath:${fileName}").file.readText(charset = Charsets.UTF_8)
}
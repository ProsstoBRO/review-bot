package com.prosstobro.reviewbot.service

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.domain.User
import com.prosstobro.reviewbot.messageResolver.LoginMessageResolver
import com.prosstobro.reviewbot.messageResolver.MessageResolver
import com.prosstobro.reviewbot.messageResolver.UnknownRequestMessageResolver
import com.prosstobro.reviewbot.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.Update
import javax.annotation.PostConstruct

@Service
class ReviewBotService(
    val messageResolvers: MutableMap<String, MessageResolver>,
    val unknownRequestMessageResolver: UnknownRequestMessageResolver,
    val loginMessageResolver: LoginMessageResolver,
    val userRepository: UserRepository,
) {
    private val logger = KotlinLogging.logger {}
    private val resolversToSkip: List<String> = listOf("LoginMessageResolver", "UnknownRequestMessageResolver")

    @PostConstruct
    private fun postConstruct() {
        for (resolverToSkip in resolversToSkip)
            messageResolvers.remove(resolverToSkip)
    }

    fun processRequest(request: TgRequest): List<TgResponse> {
        logger.info { "Received message: '${request.data}' from chatId: '${request.chatId}' user: '${request.user.toString()}'" }

        var tgResponses = loginMessageResolver.processAndCreateAnswer(request)
        if (tgResponses.isEmpty()) {
            val messageResolver: MessageResolver = messageResolvers.values.stream()
                .filter { messageResolver -> messageResolver.requestTypeIsMatched(request.data) }
                .findFirst().orElse(unknownRequestMessageResolver)

            logger.info { "${messageResolver::class.simpleName} process message: '${request.data}'" }
            tgResponses = messageResolver.processAndCreateAnswer(request)
        }

        return tgResponses
    }

    @Transactional
    fun getRequestFromUpdate(update: Update) = if (update.hasMessage()) {
        var user = userRepository.findByChatId(update.message.chatId)
        if (update.message.from.lastName == null) {
            update.message.from.lastName = ""
        }
        if (user == null) {
            user = User(
                update.message.from.userName,
                update.message.chatId,
                update.message.from.firstName,
                update.message.from.lastName
            )
        }
        TgRequest(update.message.chatId, update.message.text, user)
    } else if (update.hasCallbackQuery()) {
        val user = userRepository.findByChatId(update.callbackQuery.message.chatId)
        TgRequest(update.callbackQuery.message.chatId, update.callbackQuery.data, user)
    } else {
        throw IllegalArgumentException("Cannot get request from update")
    }
}
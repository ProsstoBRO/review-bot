package com.prosstobro.reviewbot.service

import com.prosstobro.reviewbot.domain.TgRequest
import com.prosstobro.reviewbot.domain.TgResponse
import com.prosstobro.reviewbot.domain.User
import com.prosstobro.reviewbot.messageResolver.MessageResolver
import com.prosstobro.reviewbot.messageResolver.UnknownRequestMessageResolver
import com.prosstobro.reviewbot.repository.UserRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update
import javax.annotation.PostConstruct

@Service
class BotService(
    val userRepository: UserRepository,
    val messageResolvers: MutableMap<String, MessageResolver>,
    val unknownRequestMessageResolver: UnknownRequestMessageResolver,
    val loginMessageResolver: MessageResolver
) : TelegramLongPollingBot() {

    private val logger = KotlinLogging.logger {}
    private val resolversToSkip: List<String> = listOf("LoginMessageResolver", "UnknownRequestMessageResolver")

    @Value("\${bot.token}")
    private val botToken: String = ""

    @Value("\${bot.name}")
    private val botName: String = ""

    override fun getBotToken(): String = botToken
    override fun getBotUsername(): String = botName

    @PostConstruct
    private fun postConstruct() {
        for (resolverToSkip in resolversToSkip)
            messageResolvers.remove(resolverToSkip)
    }

    override fun onUpdateReceived(update: Update?) {
        if (update!!.hasMessage() || update.hasCallbackQuery()) {
            deleteLastMessageIfPressButton(update)
            val request: TgRequest = getRequestFromUpdate(update)

            logger.info { "Received message: '${request.data}' from chatId: '${request.chatId}' user: '${request.user.toString()}'" }

            var tgResponses = loginMessageResolver.processAndCreateAnswer(request)
            if (tgResponses.isEmpty()) {
                val messageResolver: MessageResolver = messageResolvers.values.stream()
                    .filter { messageResolver -> messageResolver.requestTypeIsMatched(request) }
                    .findFirst().orElse(unknownRequestMessageResolver)

                logger.info { "${messageResolver::class.simpleName} process message: '${request.data}'" }
                tgResponses = messageResolver.processAndCreateAnswer(request)
            }

            sendTgResponse(tgResponses)
            logger.info { "Send answers: '$tgResponses'" }
        }
    }

    private fun getRequestFromUpdate(update: Update) = if (update.hasMessage()) {
        var user = userRepository.findByChatId(update.message.chatId)
        if (update.message.from.lastName == null){
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

    private fun sendTgResponse(tgResponses: List<TgResponse>) {
        for (answer in tgResponses) {
            sendMessageByChatId(answer)
        }
    }

    private fun sendMessageByChatId(tgResponse: TgResponse) {
        val responseMessage = SendMessage(tgResponse.chatId.toString(), tgResponse.text)
        responseMessage.replyMarkup = tgResponse.keyboardMarkup
        execute(responseMessage)
    }

    private fun deleteLastMessageIfPressButton(update: Update?) {
        if (update != null) {
            if (update.hasCallbackQuery()) run {
                val deleteMessage = DeleteMessage(
                    update.callbackQuery.message.chatId.toString(),
                    update.callbackQuery.message.messageId
                )
                execute(deleteMessage)
            }
        }
    }
}
package com.prosstobro.reviewbot.service

import com.prosstobro.reviewbot.domain.TgResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class ReviewTelegramLongPollingBot(
    val reviewBotService: ReviewBotService
) : TelegramLongPollingBot() {

    private val logger = KotlinLogging.logger {}

    @Value("\${bot.token}")
    private val botToken: String = ""

    @Value("\${bot.name}")
    private val botName: String = ""

    override fun getBotToken(): String = botToken
    override fun getBotUsername(): String = botName

    override fun onUpdateReceived(update: Update?) {
        if (update!!.hasMessage() || update.hasCallbackQuery()) {
            deleteLastMessageIfPressButton(update)

            val tgRequest = reviewBotService.getRequestFromUpdate(update)
            val tgResponses = reviewBotService.processRequest(tgRequest)

            sendTgResponse(tgResponses)
            logger.info { "Send answers: '$tgResponses'" }
        }
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
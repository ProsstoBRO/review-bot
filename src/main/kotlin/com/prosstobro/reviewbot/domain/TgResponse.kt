package com.prosstobro.reviewbot.domain

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

data class TgResponse(val chatId: Long, val text: String, var keyboardMarkup: InlineKeyboardMarkup?)

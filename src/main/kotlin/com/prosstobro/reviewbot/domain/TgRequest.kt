package com.prosstobro.reviewbot.domain

data class TgRequest(val chatId: Long, val data: String, val user: User?)

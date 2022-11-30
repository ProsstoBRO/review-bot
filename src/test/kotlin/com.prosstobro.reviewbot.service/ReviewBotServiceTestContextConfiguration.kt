package com.prosstobro.reviewbot.service

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ComponentScan(
    "com.prosstobro.reviewbot.repository",
    "com.prosstobro.reviewbot.service",
    "com.prosstobro.reviewbot.utils",
    "com.prosstobro.reviewbot.messageResolver",
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = [(ReviewTelegramLongPollingBot::class)]
    )]
)
class ReviewBotServiceTestContextConfiguration
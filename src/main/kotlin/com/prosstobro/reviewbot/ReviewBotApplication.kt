package com.prosstobro.reviewbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReviewBotApplication

fun main(args: Array<String>) {
	runApplication<ReviewBotApplication>(*args)
}

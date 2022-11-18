package com.prosstobro.reviewbot.utils

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.stereotype.Component

@Document
@Component
class DbSequence {
    @Id
    var id: String? = null
    @Field("sequence_number")
    var sequence: Long = 0
}
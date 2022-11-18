package com.prosstobro.reviewbot.utils

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.util.*


@Service
class DbSequenceGenerator(
    val operations: MongoTemplate
) {

    fun getNextSequence(sequenceName: String?): Long {
        val q = Query(Criteria.where("id").`is`(sequenceName))
        val u: Update = Update().inc("sequence", 1)
        val counter = operations.findAndModify(
            q, u,
            FindAndModifyOptions.options().returnNew(true).upsert(true), DbSequence::class.java
        )
        return if (!Objects.isNull(counter)) counter!!.sequence else 1L
    }
}
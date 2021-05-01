package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class DeadlineManagementModel(
        @BsonId
        val cid:String,
        val deadlineslist:HashMap<String,DeadlineModel> = HashMap()
)
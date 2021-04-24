package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class DeadlineModel(
    @BsonId
    val did:String,
    val description:String,
    val fromdate:String,
    val todate:String
)
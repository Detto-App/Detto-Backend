package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class DeadlineManagementModel(
        @BsonId
        val cid:String,
        val deadlinesList:ArrayList<DeadlineModel> = ArrayList()
)
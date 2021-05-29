package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class TimelineManagementModel(
    @BsonId
    val pid:String,
    val timelinelist:ArrayList<Timeline> = ArrayList()
)

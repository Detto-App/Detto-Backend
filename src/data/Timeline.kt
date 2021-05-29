package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class Timeline(
    val tiid:String,
    val tittle:String,
    val assigned_to:String,
    val date:String,
    val status:String
)

package com.dettoapp.detto.Models

import org.bson.codecs.pojo.annotations.BsonId

data class Todo(
    @BsonId val toid:String,
    val tittle:String,
    val category:String,
    val assignedTo:String,
    val status:String
)
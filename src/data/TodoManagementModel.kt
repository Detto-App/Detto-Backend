package com.dettoapp.data

import com.dettoapp.detto.Models.Todo
import org.bson.codecs.pojo.annotations.BsonId

data class TodoManagementModel(
    @BsonId
    val pid:String,
    val todolist:HashMap<String, Todo> = HashMap()
)

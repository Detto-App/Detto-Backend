package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class ChatGroup(@BsonId val pid: String, val messages: CircularList<ChatMessages> = CircularList())

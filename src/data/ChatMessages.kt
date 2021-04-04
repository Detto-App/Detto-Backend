package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class ChatMessages(@BsonId val pid: String, val messages: CircularList<Chat> = CircularList())

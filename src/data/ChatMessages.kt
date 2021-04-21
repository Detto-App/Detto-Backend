package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class ChatMessages(val message: String, val name: String, val time: String, val senderid: String, @BsonId val chatid: String)
package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class Chat(@BsonId val time: String, val message: String)
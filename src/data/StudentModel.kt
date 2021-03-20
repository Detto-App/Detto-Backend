package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId


data class StudentModel(
    val name: String,
    @BsonId val email: String,
    val uid: String,
    val susn: String
)

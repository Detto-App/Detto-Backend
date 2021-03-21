package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class TeacherModel(val name: String, @BsonId  val email: String,  val uid: String)
package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class TeacherModel(override val name: String, @BsonId override val email: String, override val uid: String) : User(name, email, uid)
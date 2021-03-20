package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId


data class StudentModel(override val name: String, @BsonId override val email: String, override val uid: String, val susn: String)
    : User(name, email, uid)

package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class ClassRoomStudents(@BsonId val classID: String,
                             val studentList: HashSet<StudentModel> = HashSet())


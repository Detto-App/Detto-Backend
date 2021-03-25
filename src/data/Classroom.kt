package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class Classroom(val classroomname: String, val sem: String, val section: String, @BsonId val classroomuid: String, val teacher: TeacherModel)

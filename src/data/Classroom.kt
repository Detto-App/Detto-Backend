package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

class Classroom(val classroomname: String, val year: String, val section: String, @BsonId val classroomuid: String, val userid: String)
 {

}
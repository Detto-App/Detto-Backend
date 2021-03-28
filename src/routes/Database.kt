package com.dettoapp.routes

import com.dettoapp.data.ClassRoomStudents
import com.dettoapp.data.Classroom
import com.dettoapp.data.StudentModel
import com.dettoapp.data.TeacherModel
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("UsersDatabase")


val classRoomCollection = database.getCollection<Classroom>()
val classRoomStudentsCollection = database.getCollection<ClassRoomStudents>()
val teachersCollection = database.getCollection<TeacherModel>()
val studentsCollection = database.getCollection<StudentModel>("students")

































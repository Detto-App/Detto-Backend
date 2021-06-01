package com.dettoapp.routes

import com.dettoapp.data.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("UsersDatabase")


val classRoomCollection = database.getCollection<Classroom>()
val classRoomStudentsCollection = database.getCollection<ClassRoomStudents>()
val teachersCollection = database.getCollection<TeacherModel>()
val studentsCollection = database.getCollection<StudentModel>("students")
val projectCollection = database.getCollection<ProjectModel>()
val chatMessageCollection = database.getCollection<ChatGroup>()
val deadlineManagementCollection = database.getCollection<DeadlineManagementModel>()
val todoManagementCollection = database.getCollection<TodoManagementModel>()
val rubricsCollection= database.getCollection<RubricsModel>()
val projectRubricsCollection= database.getCollection<ProjectRubricsModel>()
val timelineManagementCollection = database.getCollection<TimelineManagementModel>()

val submissionCollection = database.getCollection<SubmissionMegaModel>()


































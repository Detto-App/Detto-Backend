package com.dettoapp.routes

import com.dettoapp.data.ClassRoomStudents
import com.dettoapp.data.Classroom
import com.dettoapp.data.StudentModel
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bson.Document
import org.litote.kmongo.eq
import org.litote.kmongo.setValue


fun Route.classroomRoute() {

    authenticate {
        route("/createClassroom") {
            post {
                try {
                    val incomingClassRoomData = call.receive<Classroom>()
                    classRoomCollection.insertOne(incomingClassRoomData)
                    val classroomStudents = ClassRoomStudents(incomingClassRoomData.classroomuid)
                    classRoomStudentsCollection.insertOne(classroomStudents)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post

                }
            }

        }
    }

    authenticate {
        route("/getClassroom/{uid}") {
            get {
                try {
                    val uid = call.parameters["uid"]
                    val classroom = classRoomCollection.findOne(Classroom::classroomuid eq uid)
                    if (classroom == null)
                        call.respond(HttpStatusCode.BadRequest)
                    else
                        call.respond(classroom)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            }
        }
    }

    authenticate {
        route("/regStudentToClassroom/{cid}") {
            post {
                try {
                    val cid = call.parameters["cid"]
                    val classroom = classRoomStudentsCollection.findOne(ClassRoomStudents::classID eq cid)
                    val incomingStudentModel = call.receive<StudentModel>()
                    if (classroom == null) {
                        val set = HashSet<StudentModel>()
                        set.add(incomingStudentModel)
                        val classroomStudents = ClassRoomStudents(cid!!, set)
                        classRoomStudentsCollection.insertOne(classroomStudents)
                    } else {
                        val tempSet = classroom.studentList
                        tempSet.add(incomingStudentModel)
                        classRoomStudentsCollection.updateOne(
                                ClassRoomStudents::classID eq cid,
                                setValue(ClassRoomStudents::studentList, tempSet)
                        )
                    }

                    val getStudentModel = studentsCollection.findOne(StudentModel::uid eq incomingStudentModel.uid)
                    if (getStudentModel != null) {
                        val tempSet = getStudentModel.classrooms
                        tempSet.add(cid!!)
                        studentsCollection.updateOne(
                                StudentModel::uid eq incomingStudentModel.uid,
                                setValue(StudentModel::classrooms, tempSet)
                        )
                        call.respond(HttpStatusCode.OK)
                    } else
                        call.respond(HttpStatusCode.BadRequest, "Error")

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "fff" + e.localizedMessage)
                    return@post
                }
            }
        }
    }

    authenticate {
        route("/deleteClassroom/{classid}")
        {
            get {
                try {
                    val classID = call.parameters["classid"]
                    classRoomCollection.findOneAndDelete(Classroom::classroomuid eq classID)
                    val classRoomStudents = classRoomStudentsCollection.findOneAndDelete(ClassRoomStudents::classID eq classID)
                    deleteClassIdInStudents(classRoomStudents)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "" + e.localizedMessage)
                    return@get
                }
            }
        }
    }

    authenticate {
        route("/getClassStudents/{classID}")
        {
            get {
                try {
                    val classID = call.parameters["classID"]
                    val tempClassStudents = classRoomStudentsCollection.findOne(ClassRoomStudents::classID eq classID)
                    if (tempClassStudents != null)
                        call.respond(HttpStatusCode.OK, tempClassStudents)
                    else
                        call.respond(HttpStatusCode.BadRequest)

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

            }
        }
    }

    route("/class") {
        get {

            try {
                val list = classRoomCollection.find().toList()
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.OK, e.localizedMessage)
            }
        }
    }

    route("/cid/{classid}")
    {
        get {
            var id = call.parameters["classid"]
            id = "cid/${id}"
            call.respond(FreeMarkerContent("index.ftl", mapOf("id" to id)))
        }
    }

    route("/deleteAllClass")
    {
        get {
            classRoomCollection.deleteMany(Document())
            call.respond(HttpStatusCode.OK)
        }
    }

    route("/getAllClassStudents")
    {
        get {
            try {
                call.respond(HttpStatusCode.OK, classRoomStudentsCollection.find().toList())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

        }
    }
}

private fun deleteClassIdInStudents(classRoomStudents: ClassRoomStudents?) {
    if (classRoomStudents != null) {
        GlobalScope.launch(Dispatchers.IO) {
            for (student in classRoomStudents.studentList) {
                launch {
                    deleteClassId(classRoomStudents.classID, student.uid)
                }
            }
        }
    }
}

private suspend fun deleteClassId(classId: String, studentId: String) {
    val studentModel = studentsCollection.findOne(StudentModel::uid eq studentId)
    studentModel?.let {
//        val x  = projectCollection.findOne(ProjectModel::cid eq classId,ProjectModel::studentList contains studentModel.susn)
        it.classrooms.remove(classId)
        studentsCollection.updateOne(StudentModel::uid eq studentId, studentModel)
    }
}

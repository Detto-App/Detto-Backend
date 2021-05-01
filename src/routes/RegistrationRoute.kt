package com.dettoapp.routes

import com.dettoapp.auth.JwtConfig
import com.dettoapp.data.*
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.bson.Document
import org.litote.kmongo.div
import org.litote.kmongo.eq


fun Route.registerUser() {

    route("/registerStudent")
    {
        post {
            try {
                val incomingStudent = call.receive<StudentModel>()
                studentsCollection.insertOne(incomingStudent)
                val token = Token(JwtConfig.makeToken(incomingStudent))
                call.respond(token)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
        }
    }

    route("/registerTeacher")
    {
        post {
            try {
                val incomingTeacher = call.receive<TeacherModel>()
                teachersCollection.insertOne(incomingTeacher)
                val token = Token(JwtConfig.makeToken(incomingTeacher))
                call.respond(token)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
        }
    }

    route("/getDetails/{email}")
    {
        get {
            val role = call.request.headers["role"]?.toInt()
            val email = call.parameters["email"]

            if (role == 0) {
                val teacher = teachersCollection.findOne(TeacherModel::email eq email)
                if (teacher != null) {
                    val token = JwtConfig.makeToken(teacher)
                    call.respond(HttpStatusCode.OK, ReceivingUserModel(teacher = teacher, token = token))
                } else
                    call.respond(HttpStatusCode.BadRequest)
            } else if (role == 1) {
                val student = studentsCollection.findOne(TeacherModel::email eq email)
                if (student != null) {
                    val token = JwtConfig.makeToken(student)
                    call.respond(HttpStatusCode.OK, ReceivingUserModel(student = student, token = token))
                } else
                    call.respond(HttpStatusCode.BadRequest)
            }
        }
    }


    authenticate {
        route("/getTeacherClassrooms/{temail}") {
            get {
                try {
                    val tEmail = call.parameters["temail"]
                    val list = classRoomCollection.find(Classroom::teacher / TeacherModel::email eq tEmail).toList()
                    call.respond(list)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "" + e.localizedMessage)
                    return@get
                }
            }
        }
    }

    authenticate {
        route("/getStudentClassroom/{semail}")
        {
            get {
                try {
                    val sEmail = call.parameters["semail"]
                    val classrooms = (studentsCollection.findOne(StudentModel::email eq sEmail))!!.classrooms
                    val list = ArrayList<Classroom>()
                    for (element in classrooms) {
                        val temp = classRoomCollection.findOne(Classroom::classroomuid eq element)
                        if (temp != null)
                            list.add(temp)
                    }
                    call.respond(HttpStatusCode.OK, list)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            }
        }
    }


    route("/deleteAllStudents")
    {
        get {
            studentsCollection.deleteMany(Document())
            call.respond(HttpStatusCode.OK)

        }
    }

    route("/deleteAllTeachers")
    {
        get {
            teachersCollection.deleteMany(Document())
            call.respond(HttpStatusCode.OK)
        }
    }


    route("/gets")
    {
        get {
            try {
                val list = studentsCollection.find().toList()
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.OK, e.localizedMessage)
            }
        }
    }

    route("/gett")
    {
        get {
            try {
                val list: List<TeacherModel> = teachersCollection.find().toList()
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.OK, e.localizedMessage)
            }
        }
    }
}


package com.dettoapp.routes

import com.dettoapp.auth.JwtConfig
import com.dettoapp.data.*
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.client.engine.callContext
import io.ktor.features.ContentTransformationException
import io.ktor.freemarker.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.withContext
import org.bson.Document
import org.eclipse.jetty.http.HttpStatus
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoUtil
import java.util.concurrent.Executors


private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("UsersDatabase")
private val teachers = database.getCollection<TeacherModel>()
val students = database.getCollection<StudentModel>("students")

fun Route.registerUser() {

    route("/registerStudent")
    {
        post {
            try {
                val incomingStudent = call.receive<StudentModel>()
                students.insertOne(incomingStudent)
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
                teachers.insertOne(incomingTeacher)
                val token = Token(JwtConfig.makeToken(incomingTeacher))
                call.respond(token)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
        }
    }
    route("/getTeacherClassrooms/{uid}") {
        get {
            try {
                val uid = call.parameters["uid"]
                val list = classRoomCollection.find( Classroom::teacher / TeacherModel::email eq uid).toList()
                call.respond(list)


            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest,""+e.localizedMessage)
                return@get
            }
        }
    }

    route("/deleteAllStudents")
    {
        get {
            students.deleteMany(Document())
            call.respond(HttpStatusCode.OK)

        }
    }

    route("/deleteAllTeachers")
    {
        get {
            teachers.deleteMany(Document())
            call.respond(HttpStatusCode.OK)
        }
    }

    route("/gets")
    {
        get {
            try {
                val list = students.find().toList()
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
                val list : List<TeacherModel> = teachers.find().toList()
                call.respond(HttpStatusCode.OK,list)
            } catch (e: Exception) {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.OK, e.localizedMessage)
            }
        }
    }

    route("/getDetails/{email}")
    {
        get {
            val role = call.request.headers["role"]?.toInt()
            val email = call.parameters["email"]

            if (role == 0) {
                val teacher = teachers.findOne(TeacherModel::email eq email)
                if (teacher != null) {
                    val token = JwtConfig.makeToken(teacher)
                    call.respond(HttpStatusCode.OK, ReceivingUserModel(teacher = teacher, token = token))
                } else
                    call.respond(HttpStatusCode.BadRequest)
            } else if (role == 1) {
                val student = students.findOne(TeacherModel::email eq email)
                if (student != null) {
                    val token = JwtConfig.makeToken(student)
                    call.respond(HttpStatusCode.OK, ReceivingUserModel(student = student, token = token))
                } else
                    call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
    authenticate {
        route("/getStudentClassroom/{semail}")
        {
            get {
                var sEmail = call.parameters["semail"]
                val classrooms = (students.findOne(StudentModel::email eq sEmail))!!.classrooms
                val list = ArrayList<Classroom>()
                for (element in classrooms) {
                    val temp = classRoomCollection.findOne(Classroom::classroomuid eq element)
                    if (temp != null)
                        list.add(temp)
                }
                call.respond(HttpStatusCode.OK, list)
//            id = "cid/${id}"
//            call.respond(FreeMarkerContent("index.ftl", mapOf("id" to id)))
            }
        }
    }

}



//val compute = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
//private suspend fun ApplicationCall.getTeacherDetails()
//{
//    var list : List<TeacherModel>
//    withContext(compute)
//    {
//        list = teachers.find().toList()
//    }
//    respond(HttpStatusCode.OK,list)
//}

//    route("/studentRegis")
//    {
//        post {
//            val request2 = try {
//                println()
//                call.receive<StudentModel>()
//            } catch (e: ContentTransformationException) {
//                call.respond(HttpStatusCode.BadRequest)
//                return@post
//            }
//            students.insertOne(request2)
//            call.respond(HttpStatusCode.OK, call.request.headers["vid"] ?: "")
//        }
//    }
//
//    route("/teacherRegis")
//    {
//        post {
//            val request = try {
//                call.receive<TeacherModel>()
//            } catch (e: ContentTransformationException) {
//                call.respond(HttpStatusCode.BadRequest)
//                return@post
//            }
//            call.respond(HttpStatusCode.OK)
//        }
//    }


package com.dettoapp.routes

import com.dettoapp.auth.JwtConfig
import com.dettoapp.data.ReceivingUserModel
import com.dettoapp.data.StudentModel
import com.dettoapp.data.TeacherModel
import com.dettoapp.data.Token
import io.ktor.application.call
import io.ktor.client.engine.callContext
import io.ktor.features.ContentTransformationException
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast
import org.bson.Document
import org.eclipse.jetty.http.HttpStatus
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoUtil


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

    route("/deleteAllStudents")
    {
        get {

            //database.database.getCollection(KMongoUtil.defaultCollectionName(StudentModel::class)).deleteMany(Document())
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
                val list = teachers.find().toList()
                call.respond(HttpStatusCode.OK, list)
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

            if(role==0)
            {
                val teacher= teachers.findOne(TeacherModel::email eq email)


//                if(teacher!=null)
//                {
//                    val token = JwtConfig.makeToken(teacher)
//                    call.respond(HttpStatusCode.OK,ReceivingUserModel(teacher=teacher,token =token ))
//                }
            }



            val user = students.findOne(StudentModel::email eq call.parameters["email"])

            if (user != null)
                call.respond(HttpStatusCode.OK, user)
            else
                call.respond(HttpStatusCode.BadRequest)
        }
    }
}
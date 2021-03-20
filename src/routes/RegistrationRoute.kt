package com.dettoapp.routes

import com.dettoapp.data.ReceivingUserModel
import com.dettoapp.data.StudentModel
import com.dettoapp.data.TeacherModel
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.request.request
import io.ktor.features.ContentTransformationException
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import sun.rmi.runtime.Log


private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("UsersDatabase")
private val teachers = database.getCollection<TeacherModel>()
val students = database.getCollection<StudentModel>()

fun Route.registerUser() {

    route("/studentRegis")
    {
        post {
            val request2 = try {
                println()
                call.receive<StudentModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            students.insertOne(request2)
            call.respond(HttpStatusCode.OK,call.request.headers["vid"] ?:"")
        }
    }

    route("/teacherRegis")
    {
        post {
            val request = try {
                call.receive<TeacherModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            call.respond(HttpStatusCode.OK)
        }
    }

    route("/gets")
    {
        get {
            try {
                val list = students.find().toList()
                call.respond(HttpStatusCode.OK,list)
            }catch (e:Exception)
            {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.OK,e.localizedMessage)
            }
        }
    }

    route("/gett")
    {
        get {
            try {
                val list = teachers.find().toList()
                call.respond(HttpStatusCode.OK,list)
            }catch (e:Exception)
            {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.OK,e.localizedMessage)
            }
        }
    }

    route("/getDetails/{email}")
    {
        get {
            val user = students.findOne(StudentModel::email eq call.parameters["email"])

            if(user!=null)
                call.respond(HttpStatusCode.OK,user)
            else
                call.respond(HttpStatusCode.BadRequest)
        }
    }
}
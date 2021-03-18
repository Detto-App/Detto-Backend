package com.dettoapp.routes

import com.dettoapp.data.StudentModel
import com.dettoapp.data.TeacherModel
import io.ktor.application.call
import io.ktor.features.ContentTransformationException
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.registerUser() {

    val studentsList = ArrayList<StudentModel>()
    val teachersList = ArrayList<TeacherModel>()

    route("/studentRegis")
    {
        post {
            val request = try {
                call.receive<StudentModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            call.respond(HttpStatusCode.OK)
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
            call.respond(HttpStatusCode.OK,studentsList)
        }
    }

    route("/gett")
    {
        get {
            call.respond(HttpStatusCode.OK,teachersList)
        }
    }
}
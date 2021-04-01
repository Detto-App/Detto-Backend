package com.dettoapp.routes

import com.dettoapp.auth.JwtConfig
import com.dettoapp.data.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.projectRoute() {

    authenticate {
        route("/registerProject")
        {
            post {
                try {
                    val incomeProject = call.receive<ProjectModel>()
                    projectCollection.insertOne(incomeProject)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "qwdqw" + e.localizedMessage)
                    return@post
                }
            }
        }
    }
}
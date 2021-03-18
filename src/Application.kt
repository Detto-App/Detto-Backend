package com.dettoapp

import com.dettoapp.data.User
import com.dettoapp.routes.registerUser
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.client.*
import io.ktor.client.engine.jetty.*
import io.ktor.request.ContentTransformationException

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val list2: ArrayList<User> = ArrayList()
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing)
    {
        registerUser()
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Html)
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }

        post("/vinay")
        {
            val users = try {
                call.receive<User>()
                //call.respond("Done")
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadGateway)
                return@post
            }
            list2.add(users)
            call.respond(HttpStatusCode.OK)
        }

        get("/data")
        {
            call.respond(HttpStatusCode.OK, list2)
        }
    }
}


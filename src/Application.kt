package com.dettoapp

import com.dettoapp.Utility.Constants
import com.dettoapp.auth.JwtConfig
import com.dettoapp.data.StudentModel
import com.dettoapp.data.User
import com.dettoapp.routes.*
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.request.ContentTransformationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {


//    CoroutineScope(Dispatchers.IO).launch{
//        classRoomCollection.drop()
//        students.drop()
//        classRoomStudents.drop()
//    }

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }

    install(Authentication)
    {
        jwt {
            verifier(JwtConfig.verifier)
            realm = Constants.ISSUER

            validate {
                UserIdPrincipal( it.payload.getClaim("id").asString())
            }
        }
    }

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(Routing)
    {
        registerUser()
        classroomRoute()
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
            call.respond(HttpStatusCode.OK)
        }

        get("/data")
        {
            call.respond(HttpStatusCode.OK, "empty")
        }

        post("/login") {
            val credentials = call.receive<StudentModel>()
            val token = JwtConfig.makeToken(credentials)
            call.respondText(token)
        }

        authenticate {
            route("/sec")
            {
                get {
                    call.respond(HttpStatusCode.OK,"Hello")
                }
            }

        }
    }
}


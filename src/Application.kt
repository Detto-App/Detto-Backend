package com.dettoapp

import com.dettoapp.Utility.Constants
import com.dettoapp.auth.JwtConfig
import com.dettoapp.data.StudentModel
import com.dettoapp.data.User
import com.dettoapp.routes.*
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.freemarker.FreeMarker
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.websocket.WebSockets
import java.util.*


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {


    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)
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
                UserIdPrincipal(it.payload.getClaim("id").asString())
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
        projectRoute()
        chat()
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
                    call.respond(HttpStatusCode.OK, "Hello")
                }
            }

        }

        route("/deleteDb")
        {
            get {
                classRoomCollection.drop()
                classRoomStudentsCollection.drop()
                teachersCollection.drop()
                studentsCollection.drop()
                projectCollection.drop()
                call.respond(HttpStatusCode.OK)
            }
        }

    }

//    CoroutineScope(Dispatchers.IO).launch {
//        val queue = CircularQueue<String>()
//
//        queue.add("a")
//        queue.add("b")
//        queue.add("c")
//        queue.add("d")
//        queue.add("e")
//        queue.add("f")
//
//    }

}

suspend fun WebSocketSession.sendText(frame: String) {
    this.send(Frame.Text(frame))
}

fun<E> Queue<E>.print()
{
    val x = PriorityQueue<E>(this)
    while (!x.isEmpty())
    {
        println(x.peek())
        x.remove()
    }
}



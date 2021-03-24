package com.dettoapp.routes

import com.dettoapp.data.Classroom
import com.dettoapp.data.User
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.*
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.eclipse.jetty.http.HttpStatus
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import kotlin.Exception

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("UsersDatabase")
val classroomcollection = database.getCollection<Classroom>()
fun Route.classroomRoute() {
    authenticate {
        route("/createClassroom") {
            post {
                try {
                    val incomingclassroomdata = call.receive<Classroom>()
                    classroomcollection.insertOne(incomingclassroomdata)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post

                }
            }
        }
    }
    route("/class") {
        get {

            try {
                val list = classroomcollection.find().toList()
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.OK, e.localizedMessage)
            }
        }
    }

    authenticate {
        route("/getClassroom/{uid}") {
            get {
                try {
                    val uid = call.parameters["uid"]
                    val classroom= classroomcollection.findOne(Classroom::classroomuid eq uid)
                    if(classroom==null)
                        call.respond(HttpStatusCode.BadRequest)
                    else
                        call.respond(classroom)
                    call.respondText("" + uid)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            }
        }
    }

    route("/cid/{classid}")
    {
        get {
            val id = call.parameters["classid"]
            val user = User("vv","dfdsf","uid")
            call.respond(FreeMarkerContent("index.ftl", mapOf("id" to id)))
        }
    }
}

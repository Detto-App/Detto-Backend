package com.dettoapp.routes

import com.dettoapp.data.Classroom
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.eclipse.jetty.http.HttpStatus
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import kotlin.Exception

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("UsersDatabase")
val classroomcollection= database.getCollection<Classroom>()
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
    }

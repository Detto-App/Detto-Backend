package com.dettoapp.routes

import com.dettoapp.data.ClassRoomStudents
import com.dettoapp.data.ProjectModel
import com.dettoapp.data.RubricsModel
import com.dettoapp.data.StudentModel
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.bson.Document
import org.litote.kmongo.addToSet
import org.litote.kmongo.eq

fun Route.rubricsRoute(){
    route("/createRubrics")
    {
        post {
            try {
                val incomingModel = call.receive<RubricsModel>()
                rubricsCollection.insertOne(incomingModel)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
        }
    }
    route("/showRubrics"){
        get {

            try {
                val list = rubricsCollection.find().toList()
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.OK, e.localizedMessage)
            }
        }
    }
    route("/deleteRubrics"){
        get{
            try {
                rubricsCollection.deleteMany(Document())
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
        }
    }
    route("/getRubrics/{classId}"){
        get {
            try {
                val classID = call.parameters["classID"]
                val rubrics = rubricsCollection.findOne(RubricsModel::cid eq classID)
                if (rubrics != null)
                    call.respond(HttpStatusCode.OK, rubrics)
                else
                    call.respond(HttpStatusCode.BadRequest)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

        }
    }
    }

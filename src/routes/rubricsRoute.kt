package com.dettoapp.routes

import com.dettoapp.data.*
import com.google.gson.Gson
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.bson.Document
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

fun Route.rubricsRoute() {
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
    route("/showRubrics") {
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
    route("/deleteRubrics") {
        get {
            try {
                rubricsCollection.deleteMany(Document())
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
        }
    }
    route("/getRubrics/{classId}") {
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

    route("/insertProjectRubrics")
    {
        post {
            try {
                val incomingModel = call.safeReceive<ArrayList<ProjectRubricsModel>>()
                projectRubricsCollection.insertMany(incomingModel)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest,e.localizedMessage)
                return@post
            }
        }
    }

    route("/getProjectRubrics")
    {
        get {
            try {
                val list = projectRubricsCollection.find().toList()
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest,e.localizedMessage)
                return@get
            }
        }
    }

    route("/deleteProjectRubrics")
    {
        get {
            try {
                projectRubricsCollection.deleteMany(Document())
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
        }
    }
    route("/getProjectRubrics/{cid}/{pid}")
    {
        get {
            try {
                val pid = call.parameters["pid"]
                val projectRubricsList=projectRubricsCollection.find(ProjectRubricsModel::pid eq pid).toList()
                call.respond(HttpStatusCode.OK,projectRubricsList)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
        }
    }


    route("/updateProjectRubrics/{cid}/{pid}")
    {
        post {
            try {
                val incomingModel = call.receive<HashMap<String,RubricsModel>>()
                val cid = call.parameters["cid"]
                val pid = call.parameters["pid"]
                for(i in incomingModel.keys){

                    projectRubricsCollection.updateOne(and(ProjectRubricsModel::usn eq i,ProjectRubricsModel::pid eq pid ), setValue(ProjectRubricsModel::rubrics,incomingModel[i]))

                }


                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
        }
    }

    }
suspend inline fun <reified T> ApplicationCall.safeReceive(): T {
    val json = this.receiveOrNull<String>()
    return Gson().fromJson(json, T::class.java)
}



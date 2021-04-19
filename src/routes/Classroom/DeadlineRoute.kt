package com.dettoapp.routes.Classroom

import com.dettoapp.data.ClassRoomStudents
import com.dettoapp.data.Classroom
import com.dettoapp.data.DeadlineManagementModel
import com.dettoapp.data.DeadlineModel
import com.dettoapp.routes.classRoomCollection
import com.dettoapp.routes.classRoomStudentsCollection
import com.dettoapp.routes.deadlineManagementCollection
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

fun Route.deadlineRoute() {

    authenticate {
        route("/createDeadline/{cid}") {
            post {
                try {
                    val classID = call.parameters["cid"]
                    val tempClassDeadline = deadlineManagementCollection.findOne(DeadlineManagementModel::cid eq classID)
                    val incomingDeadlinesData = call.receive<DeadlineModel>()
                    if (tempClassDeadline == null) {
                        val deadlineArray = ArrayList<DeadlineModel>()
                        deadlineArray.add(incomingDeadlinesData)
                        val deadlineManagementModel = DeadlineManagementModel(classID!!, deadlineArray)
                        deadlineManagementCollection.insertOne(deadlineManagementModel)
                    } else {
                        val deadlineArray = tempClassDeadline.deadlinesList
                        deadlineArray.add(incomingDeadlinesData)
                        deadlineManagementCollection.updateOne(
                                DeadlineManagementModel::cid eq classID,
                                setValue(DeadlineManagementModel::deadlinesList, deadlineArray)
                        )

                    }
                    call.respond(HttpStatusCode.OK)

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
            }

        }
    }
    route("/deadlines") {
        get {

            try {
                val list = deadlineManagementCollection.find().toList()
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.OK, e.localizedMessage)
            }
        }
    }
}
package com.dettoapp.routes.Classroom

import com.dettoapp.data.*
import com.dettoapp.routes.classRoomCollection
import com.dettoapp.routes.classRoomStudentsCollection
import com.dettoapp.routes.deadlineManagementCollection
import com.dettoapp.routes.studentsCollection
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bson.Document
import org.litote.kmongo.deleteOne
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
                        val deadlineArray = HashMap<String,DeadlineModel>()
                        deadlineArray[incomingDeadlinesData.did]=incomingDeadlinesData
                        val deadlineManagementModel = DeadlineManagementModel(classID!!, deadlineArray)
                        deadlineManagementCollection.insertOne(deadlineManagementModel)
                    } else {
                        val deadlineArray = tempClassDeadline.deadlineslist
                        deadlineArray[incomingDeadlinesData.did]=incomingDeadlinesData
                        deadlineManagementCollection.updateOne(
                                DeadlineManagementModel::cid eq classID,
                                setValue(DeadlineManagementModel::deadlineslist, deadlineArray)
                        )

                    }
                    call.respond(HttpStatusCode.OK)

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
            }

        }
        route("/deleteDeadline/{cid}/{did}"){
            post{
                try{
                    val classID = call.parameters["cid"]
                    val did = call.parameters["did"]
                    val deadlinesMap=deadlineManagementCollection.findOne(DeadlineManagementModel::cid eq classID)
                    deleteDeadlineInDeadlinesMap(deadlinesMap!!,did!!,classID!!)
                    call.respond(HttpStatusCode.OK)


                }
                catch(e:Exception){
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
//                println(e.localizedMessage)
                call.respond(HttpStatusCode.OK, e.localizedMessage)
            }
        }
    }
    route("/deleteAllDeadlines")
    {
        get {
            deadlineManagementCollection.deleteMany(Document())
            call.respond(HttpStatusCode.OK)
        }
    }

    authenticate {
        route("getDeadline/{cid}") {
            get {
                try {
                    val classID = call.parameters["cid"]
                    val tempClassDeadline =
                        deadlineManagementCollection.findOne(DeadlineManagementModel::cid eq classID)
                    if (tempClassDeadline == null)
                        call.respond(HttpStatusCode.BadRequest)
                    val list=ArrayList<DeadlineModel>()
                    for((K,V) in tempClassDeadline!!.deadlineslist)
                        list.add(V)

                    call.respond(HttpStatusCode.OK,list)

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            }
        }
    }
}
private fun deleteDeadlineInDeadlinesMap(deadlineManagementModel: DeadlineManagementModel,did:String,cid:String) {
    if (deadlineManagementModel != null) {
        GlobalScope.launch(Dispatchers.IO) {
            if (did in deadlineManagementModel.deadlineslist.keys) {
                deadlineManagementModel?.let {
                    it.deadlineslist.remove(did)
                    deadlineManagementCollection.updateOne(DeadlineManagementModel::cid eq cid, deadlineManagementModel)
                }
            } else {
                throw Exception("InvalidDeadline")

            }
        }
    }
}

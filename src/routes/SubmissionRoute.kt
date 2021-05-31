package com.dettoapp.routes

import com.dettoapp.data.SubmissionMegaModel
import com.dettoapp.data.SubmissionModel
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.bson.Document
import org.litote.kmongo.eq

fun Route.submissionRoute() {
    route("/submission/{pid}")
    {
        post {
            try {
                val incomeSubmission = call.receive<SubmissionModel>()
                val pid = call.parameters["pid"]!!

                var submissionMegaModel = submissionCollection.findOne(SubmissionMegaModel::pid eq pid)

                if(submissionMegaModel==null)
                {
                    val submissionMegaModel2 = SubmissionMegaModel(pid, arrayListOf(incomeSubmission))
                    submissionCollection.insertOne(submissionMegaModel2)
                }else
                {
                    val list = submissionMegaModel.fileList
                    list.add(incomeSubmission)
                    submissionCollection.updateOne(SubmissionMegaModel::pid eq pid, submissionMegaModel)
                }
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest,""+e.localizedMessage)
                return@post
            }
        }

        get {
            try {

                val pid = call.parameters["pid"]!!

                val submissionMegaModel = submissionCollection.findOne(SubmissionMegaModel::pid eq pid)

                if(submissionMegaModel==null)
                {
                    call.respond(HttpStatusCode.OK, ArrayList<SubmissionModel>())
                }else
                {
                    call.respond(HttpStatusCode.OK,submissionMegaModel.fileList)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
        }
    }

    route("/getSub")
    {
        get {
            val x = submissionCollection.find(Document()).toList()
            call.respond(HttpStatusCode.OK,x)
        }
    }
}
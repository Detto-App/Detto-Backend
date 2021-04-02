package com.dettoapp.routes

import com.dettoapp.auth.JwtConfig
import com.dettoapp.data.*
import com.mongodb.client.model.Updates.addToSet
//import com.mongodb.client.model.Accumulators.addToSet
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.addToSet
import org.litote.kmongo.eq


fun Route.projectRoute() {

    authenticate {
        route("/registerProject")
        {
            post {
                try {
                    val incomeProject = call.receive<ProjectModel>()
                    projectCollection.insertOne(incomeProject)
                    val studentList=incomeProject.studentList
                    for((key,value) in studentList){
                        studentsCollection.updateOne(StudentModel::susn eq value, addToSet(StudentModel::projects,incomeProject.pid))
                    }
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "qwdqw" + e.localizedMessage)
                    return@post
                }
            }
        }

    }

    route("/getp")
    {
        get{
            try {
                //val incomeProject = call.receive<ProjectModel>()

                call.respond(HttpStatusCode.OK,projectCollection.find().toList())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "qtyui" + e.localizedMessage)
                return@get
            }
        }
    }
}
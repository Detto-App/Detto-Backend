package com.dettoapp.routes

//import com.mongodb.client.model.Accumulators.addToSet
import com.dettoapp.data.ProjectModel
import com.dettoapp.data.StudentModel
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.litote.kmongo.addToSet
import org.litote.kmongo.eq


fun Route.projectRoute() {

    authenticate {
        route("/registerProject/{susn}")
        {
            post {
                try {
                    val incomeProject = call.receive<ProjectModel>()
                    val susn = call.parameters["susn"]
                    projectCollection.insertOne(incomeProject)
                    studentsCollection.updateOne(StudentModel::susn eq susn, addToSet(StudentModel::projects, incomeProject.pid))
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
            }
        }




        route("/getProjectDetails/{pid}")
        {
            get {
                try {
                    val pid = call.parameters["pid"]
                    val project = projectCollection.findOne(ProjectModel::pid eq pid)
                    if (project != null) {
                        call.respond(HttpStatusCode.OK, project)

                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            }
        }
        route("/regStudentToProject/{pid}/{name}")
        {
            post {
                try {
                    val pid = call.parameters["pid"]
                    val sName = call.parameters["name"]
                    val projectModel = projectCollection.findOne(ProjectModel::pid eq pid)
                    if (projectModel != null) {
                        projectModel.studentNameList.add(sName!!)
                        projectCollection.updateOne(ProjectModel::pid eq pid, projectModel)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
            }
        }



        route("/getProjects/{cid}")
        {
            get {
                try {
                    val classID = call.parameters["cid"]
                    val projectList = projectCollection.find(ProjectModel::cid eq classID).toList()
                    call.respond(HttpStatusCode.OK, projectList)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            }
        }
    }
    route("/getp")
    {
        get {
            try {
                val list = projectCollection.find().toList()
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
        }
    }

}
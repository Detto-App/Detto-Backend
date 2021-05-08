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
import org.bson.Document
import org.litote.kmongo.addToSet
import org.litote.kmongo.eq
import org.litote.kmongo.setValue


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
        route("/regStudentToProject/{pid}/{name}/{susn}")
        {
            post {
                try {
                    val pid = call.parameters["pid"]
                    val sName = call.parameters["name"]
                    val susn = call.parameters["susn"]
                    val projectModel = projectCollection.findOne(ProjectModel::pid eq pid)
                    if (projectModel != null) {
                        projectModel.studentNameList.add(sName!!)
                        projectModel.studentList.add(susn!!)
                        projectCollection.updateOne(ProjectModel::pid eq pid, projectModel)
                        studentsCollection.updateOne(StudentModel::susn eq susn, addToSet(StudentModel::projects, pid))
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


    route("/deleteProjects")
    {
        get {
            try {
                projectCollection.deleteMany(Document())
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
        }
    }

    authenticate {
        route("/changeStatus/{pid}/{status}")
        {
            get {
                try {
                    val pid = call.parameters["pid"]
                    val status = call.parameters["status"]
                    val obj = projectCollection.findOneAndUpdate(
                        ProjectModel::pid eq pid,
                        setValue(ProjectModel::status, status)
                    )
                    if (obj != null) {
                        call.respond(HttpStatusCode.OK)
                    } else call.respond(HttpStatusCode.BadRequest, "" + obj)

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            }
        }
    }

    authenticate {
        route("/getManyProjectDetails") {
            post {
                try {
                    val listOfProjects = call.receive<HashSet<String>>()
                    val listOfProjectDetails = ArrayList<ProjectModel>()
                    for (i in listOfProjects) {
                        listOfProjectDetails.add(projectCollection.findOne(ProjectModel::pid eq i)!!)
                    }
                    call.respond(HttpStatusCode.OK, message = listOfProjectDetails)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "" + e.localizedMessage)
                    return@post
                }
            }
        }
    }

    authenticate {
        route("/updateProject/{pid}") {
            post {
                try {
                    val projectModel = call.receive<ProjectModel>()
                    val pid = call.parameters["pid"]
                    projectCollection.updateOne(ProjectModel::pid eq pid, projectModel)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "" + e.localizedMessage)
                    return@post
                }
            }
        }
    }
}
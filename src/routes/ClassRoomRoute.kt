package com.dettoapp.routes

import com.dettoapp.data.ClassRoomStudents
import com.dettoapp.data.Classroom
import com.dettoapp.data.StudentModel
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.bson.Document
import org.eclipse.jetty.http.HttpStatus
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("UsersDatabase")
val classRoomCollection = database.getCollection<Classroom>()
val classRoomStudents = database.getCollection<ClassRoomStudents>()

fun Route.classroomRoute() {

    authenticate {
        route("/createClassroom") {
            post {
                try {
                    val incomingClassRoomData = call.receive<Classroom>()
                    classRoomCollection.insertOne(incomingClassRoomData)
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
                val list = classRoomCollection.find().toList()
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
                    val classroom = classRoomCollection.findOne(Classroom::classroomuid eq uid)
                    if (classroom == null)
                        call.respond(HttpStatusCode.BadRequest)
                    else
                        call.respond(classroom)
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
            var id = call.parameters["classid"]
            id = "cid/${id}"
            call.respond(FreeMarkerContent("index.ftl", mapOf("id" to id)))
        }
    }

    route("/deleteAllClass")
    {
        get {
            classRoomCollection.deleteMany(Document())
            call.respond(HttpStatusCode.OK)
        }
    }

    authenticate {
        route("/regStudentToClassroom/{cid}") {
            post {
                try {
                    val cid = call.parameters["cid"]
                    val classroom = classRoomStudents.findOne(ClassRoomStudents::classID eq cid)
                    val incomingStudentModel = call.receive<StudentModel>()
                    if (classroom == null) {
                        val set = HashSet<StudentModel>()
                        set.add(incomingStudentModel)
                        val classroomStudents = ClassRoomStudents(cid!!, set)
                        classRoomStudents.insertOne(classroomStudents)
                    } else {
                        val tempSet = classroom.studentList
                        tempSet.add(incomingStudentModel)
                        classRoomStudents.updateOne(
                            ClassRoomStudents::classID eq cid,
                            setValue(ClassRoomStudents::studentList, tempSet)
                        )
                    }

                    val getStudentModel = students.findOne(StudentModel::uid eq incomingStudentModel.uid)
                    if (getStudentModel != null) {
                        val tempSet = getStudentModel!!.classrooms
                        tempSet.add(cid!!)
                        students.updateOne(
                            StudentModel::uid eq incomingStudentModel.uid,
                            setValue(StudentModel::classrooms, tempSet)
                        )
                        call.respond(HttpStatusCode.OK)
                    } else
                        call.respond(HttpStatusCode.BadRequest, "Error")

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "fff" + e.localizedMessage)
                    return@post
                }
            }
        }
    }


    route("/getAllClassStudents")
    {
        get {
            try {
                call.respond(HttpStatusCode.OK, classRoomStudents.find().toList())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

        }
    }

    route("/getClassStudents/{classID}")
    {
        get {
            try {
                val classID = call.parameters["classID"]
                call.respond(HttpStatusCode.OK, classRoomStudents.find(ClassRoomStudents::classID eq classID).toList())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

        }
    }
    route("/getclassrooms/{uid}")
    {
        get {
            try {
                val uid = call.parameters["uid"]
                call.respond(HttpStatusCode.OK, students.find(StudentModel::uid eq uid).toList())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
        }
    }

    authenticate {
        route("/deleteClassroom/{classid}")
        {
            get {
                try {
                    val classID = call.parameters["classid"]
                    classRoomCollection.findOneAndDelete(Classroom::classroomuid eq classID)
                    classRoomStudents.findOneAndDelete(ClassRoomStudents::classID eq classID)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "" + e.localizedMessage)
                    return@get
                }
            }
        }
    }
}


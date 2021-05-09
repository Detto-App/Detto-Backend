package com.dettoapp.routes

import com.dettoapp.data.DeadlineManagementModel
import com.dettoapp.data.DeadlineModel
import com.dettoapp.data.TodoManagementModel
import com.dettoapp.detto.Models.Todo
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.bson.Document
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

fun Route.todoRoute() {
    authenticate {
        route("/createTodo/{cid}") {
            post {
                try {
                    val classID = call.parameters["cid"]
                    val tempClassTodo =
                        todoManagementCollection.findOne(TodoManagementModel::cid eq classID)
                    val incomingTodoData = call.receive<Todo>()
                    if (tempClassTodo == null) {
                        val todoArray = HashMap<String, Todo>()
                        todoArray[incomingTodoData.toid] = incomingTodoData
                        val todoManagementModel = TodoManagementModel(classID!!, todoArray)
                        todoManagementCollection.insertOne(todoManagementModel)
                    } else {
                        val todoArray = tempClassTodo.todolist
                        todoArray[incomingTodoData.toid] = incomingTodoData
                        todoManagementCollection.updateOne(
                            TodoManagementModel::cid eq classID,
                            setValue(TodoManagementModel::todolist, todoArray)
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
    route("/todos") {
        get {

            try {
                val list = todoManagementCollection.find().toList()
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.OK, e.localizedMessage)
            }
        }
    }
    route("/deleteAllTodo")
    {
        get {
            todoManagementCollection.deleteMany(Document())
            call.respond(HttpStatusCode.OK)
        }
    }
}

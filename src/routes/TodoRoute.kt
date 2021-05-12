package com.dettoapp.routes

import com.dettoapp.data.TodoManagementModel
import com.dettoapp.detto.Models.Todo
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bson.Document
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

fun Route.todoRoute() {
//    authenticate {
        route("/createTodo/{pid}") {
            post {
                try {
                    val classID = call.parameters["pid"]
                    val tempClassTodo =
                        todoManagementCollection.findOne(TodoManagementModel::pid eq classID)
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
                            TodoManagementModel::pid eq classID,
                            setValue(TodoManagementModel::todolist, todoArray)
                        )

                    }
                    call.respond(HttpStatusCode.OK)

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
            }

//        }
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
    authenticate {
        route("/deleteTodo/{pid}/{toid}") {
            get {
                try {
                    val pid = call.parameters["pid"]
                    val toid = call.parameters["toid"]
                    val Todomap = todoManagementCollection.findOne(TodoManagementModel::pid eq pid)
                    deleteTodoInTodoMap(Todomap!!, toid!!, pid!!)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            }
        }
    }
    authenticate {
        route("/getTodo/{pid}") {
            get {
                try {
                    val classID = call.parameters["pid"]
                    val tempClassTodo =
                        todoManagementCollection.findOne(TodoManagementModel::pid eq classID)
                    if (tempClassTodo == null)
                        call.respond(HttpStatusCode.BadRequest)
                    val list=ArrayList<Todo>()
                    for((K,V) in tempClassTodo!!.todolist)
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

private fun deleteTodoInTodoMap(todoManagementModel: TodoManagementModel, toid:String, pid:String) {
    if (todoManagementModel != null) {
        GlobalScope.launch(Dispatchers.IO) {
            if (toid in todoManagementModel.todolist.keys) {
                todoManagementModel?.let {
                    it.todolist.remove(toid)
                    todoManagementCollection.updateOne(TodoManagementModel::pid eq pid, todoManagementModel)
                }
            } else {
                throw Exception("Invalid Todo")

            }
        }
    }
}

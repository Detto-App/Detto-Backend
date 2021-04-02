package com.dettoapp.routes

import com.dettoapp.data.Connection
import com.dettoapp.sendText
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.websocket.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet


val connectionsHashMap = HashMap<String, MutableSet<Connection?>>()
val list = arrayListOf<String>("Apple", "Mongo", "IDK")
fun Route.chat() {

    route("/chat/{id}")
    {
        webSocket {
            val id = call.parameters["id"]!!

            val connections: MutableSet<Connection?>
            if (connectionsHashMap[id] == null) {
                connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
                connectionsHashMap[id] = connections
                println("Count inside NULL " + connectionsHashMap.count())
            } else {
                println("Count is " + connectionsHashMap.count())
                connections = connectionsHashMap[id]!!
            }
            val thisConnection = Connection(this)
            try {
                println("Adding user!")
                connections += thisConnection
                println("Connects Count " + connections.size)

                list.forEach { data ->
                    thisConnection.session.sendText(data)
                }

                sendText("You are connected! There are ${connections.count()} users here.")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
                    list.add(textWithUsername)
                    connections.forEach {
                        if (it != thisConnection) {
                            it?.session?.sendText(textWithUsername)
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
                println("Connects Count " + connections.size)
            }
        }
    }
}
//        webSocket {
//            val id = call.parameters["id"]
//            connections = if (connectionsHashMap[id] != null) {
//               val x =  connectionsHashMap[id]!!
//                x
//            } else {
//                val tempConnection = Collections.synchronizedSet<Connection?>(LinkedHashSet())
//                if (id != null) {
//                    connectionsHashMap[id] = tempConnection
//                }
//                tempConnection
//            }
//            println("Adding user!")
//            val thisConnection = Connection(this)
//            connections = connections + thisConnection
//            try {
//                sendText("You are connected! There are ${connections.count()} users here.")
//                for (frame in incoming) {
//                    frame as? Frame.Text ?: continue
//                    val receivedText = frame.readText()
//                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
//                    connections.forEach {
//                        it?.session?.sendText(textWithUsername)
//                    }
//                }
//            } catch (e: Exception) {
//                println(e.localizedMessage)
//            } finally {
//                println("Removing $thisConnection!")
//                connections = connections - thisConnection
//            }
//        }
//    route{
//        webSocket("/chat") {
//            send("You are connected!")
//            for(frame in incoming) {
//                frame as? Frame.Text ?: continue
//                val receivedText = frame.readText()
//                send("You said: $receivedText")
//            }
//        }
//    }

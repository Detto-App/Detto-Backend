package com.dettoapp.routes

import com.dettoapp.data.Chat
import com.dettoapp.data.ChatMessages
import com.dettoapp.data.Connection
import com.dettoapp.sendText
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.websocket.*
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet


val connectionsHashMap = HashMap<String, MutableSet<Connection?>>()
val chatMessagesHashMap = HashMap<String, ChatMessages>()

fun Route.chat() {

    route("/deleteChat")
    {
        get {
            try {
                chatMessageCollection.drop()
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.localizedMessage + "")
                return@get
            }

        }
    }

    route("/getChat")
    {
        get {
            call.respond(HttpStatusCode.OK, chatMessageCollection.find().toList())
        }
    }
    route("/chat/{id}")
    {
        webSocket {
            val id = call.parameters["id"]!!
            val connections: MutableSet<Connection?>
            var chatMessages: ChatMessages? = null

            if (connectionsHashMap[id] == null) {
                connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
                connectionsHashMap[id] = connections
                //println("Count inside NULL " + connectionsHashMap.count())
            } else {
                //println("Count is " + connectionsHashMap.count())
                connections = connectionsHashMap[id]!!
            }

            val thisConnection = Connection(this)

            try {
                //println("Adding user!")
                connections += thisConnection
                //println("Connects Count " + connections.size)

                if (chatMessagesHashMap[id] == null) {
                    chatMessages = chatMessageCollection.findOne(ChatMessages::pid eq id)
                    if (chatMessages == null) {
                        chatMessages = ChatMessages(id)
                        chatMessagesHashMap[id] = chatMessages
                        chatMessageCollection.insertOne(chatMessages)
                    }
                } else {
                    chatMessages = chatMessagesHashMap[id]
                }

                val list = chatMessages!!.messages.getList(true)
                list.forEach { data ->
                    thisConnection.session.sendText(data.message)
                }

                val messages = chatMessages.messages
                sendText("You are connected! There are ${connections.count()} users here.")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    messages.add(Chat(DateTimeFormatter.ISO_INSTANT.format(Instant.now()), receivedText))
                    connections.forEach {
                        if (it != thisConnection) {
                            it?.session?.sendText(receivedText)
                        }
                    }
                }
            } catch (e: Exception) {
                sendText("" + e.localizedMessage)
            } finally {
                //println("Removing $thisConnection!")
                connections -= thisConnection
                //println("Connects Count " + connections.size)
                if (connections.size == 0) {
                    chatMessageCollection.updateOne(ChatMessages::pid eq id, setValue(ChatMessages::messages, chatMessages!!.messages))
                    connectionsHashMap.remove(id)
                    chatMessagesHashMap.remove(id)
                }
            }
        }
    }
}

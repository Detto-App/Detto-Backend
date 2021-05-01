package com.dettoapp.routes

import com.dettoapp.data.ChatMessages
import com.dettoapp.data.ChatGroup
import com.dettoapp.data.CircularList
import com.dettoapp.data.Connection
import com.dettoapp.sendText
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

val connectionsHashMap = HashMap<String, MutableSet<Connection?>>()
val chatMessagesHashMap = HashMap<String, CircularList<ChatMessages>>()
private val gson = Gson()
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
            val chatMessagesMessages: CircularList<ChatMessages>
            val thisConnection = Connection(this)

            connections = getConnection(id)
            chatMessagesMessages = getChatMessageList(id)

            try {
                connections += thisConnection
                chatMessagesMessages.forEach { data ->
                    sendText(gson.toJson(data))
                }

                sendText("You are connected! There are ${connections.count()} users here.")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val chatMessageLocal: ChatMessages
                    try {
                        chatMessageLocal = gson.fromJson(receivedText, ChatMessages::class.java)
                    } catch (e: Exception) {
                        continue
                    }

                    storeAndUpdateChatMessage(id, chatMessageLocal, chatMessagesMessages)
                    connections.forEach {
                        if (it != thisConnection) {
                            it?.session?.sendText(gson.toJson(chatMessageLocal))
                        }
                    }
                }
            } catch (e: Exception) {
                sendText("" + e.localizedMessage)
            } finally {
                connections -= thisConnection
                if (connections.size == 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        chatMessageCollection.updateOne(ChatGroup::pid eq id, setValue(ChatGroup::messages, chatMessagesMessages))
                    }
                    chatMessagesHashMap.remove(id)
                    connectionsHashMap.remove(id)
                }
            }
        }
    }
}

private fun getConnection(id: String): MutableSet<Connection?> {
    return if (connectionsHashMap[id] == null) {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        connectionsHashMap[id] = connections
        connections
    } else {
        connectionsHashMap[id]!!
    }
}

private suspend fun getChatMessageList(id: String) = if (chatMessagesHashMap[id] == null) {
    var localChatMessages = chatMessageCollection.findOne(ChatGroup::pid eq id)
    if (localChatMessages == null) {
        localChatMessages = ChatGroup(id)
        chatMessagesHashMap[id] = localChatMessages.messages
        CoroutineScope(Dispatchers.IO).launch {
            chatMessageCollection.insertOne(localChatMessages)
        }
    }
    localChatMessages.messages
} else {
    chatMessagesHashMap[id]!!
}

private fun storeAndUpdateChatMessage(id: String, message: ChatMessages, chatMessagesList: CircularList<ChatMessages>) {
    chatMessagesList.add(message)
    chatMessagesHashMap[id] = chatMessagesList
}

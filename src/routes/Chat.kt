package com.dettoapp.routes

import com.dettoapp.data.Chat
import com.dettoapp.data.ChatMessages
import com.dettoapp.data.CircularList
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
import io.ktor.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

val connectionsHashMap = HashMap<String, MutableSet<Connection?>>()
val chatMessagesHashMap = HashMap<String, CircularList<Chat>>()

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
            val chatMessages: CircularList<Chat>
            val thisConnection = Connection(this)

            connections = getConnection(id)
            chatMessages = getChatMessageList(id)

            try {
                connections += thisConnection
                chatMessages.forEach { data ->
                    sendText(data.message)
                }

                sendText("You are connected! There are ${connections.count()} users here.")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    storeAndUpdateChatMessage(id, receivedText, chatMessages)
                    connections.forEach {
                        if (it != thisConnection) {
                            it?.session?.sendText(receivedText)
                        }
                    }
                }
            } catch (e: Exception) {
                sendText("" + e.localizedMessage)
            } finally {
                connections -= thisConnection
                if (connections.size == 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        chatMessageCollection.updateOne(ChatMessages::pid eq id, setValue(ChatMessages::messages, chatMessages))
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
    var localChatMessages = chatMessageCollection.findOne(ChatMessages::pid eq id)
    if (localChatMessages == null) {
        localChatMessages = ChatMessages(id)
        chatMessagesHashMap[id] = localChatMessages.messages
        CoroutineScope(Dispatchers.IO).launch {
            chatMessageCollection.insertOne(localChatMessages)
        }
    }
    localChatMessages.messages
} else {
    chatMessagesHashMap[id]!!
}

private fun storeAndUpdateChatMessage(id: String, message: String, chatMessages: CircularList<Chat>) {
    chatMessages.add(Chat(DateTimeFormatter.ISO_INSTANT.format(Instant.now()), message))
    chatMessagesHashMap[id] = chatMessages
}

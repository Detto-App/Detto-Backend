package com.dettoapp.data

import io.ktor.http.cio.websocket.*
import java.util.concurrent.atomic.*

class Connection(val session: DefaultWebSocketSession)
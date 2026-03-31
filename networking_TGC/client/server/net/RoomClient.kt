package net

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import protocol.ClientMessage
import protocol.ServerMessage

class RoomClient(
    private val scope: CoroutineScope,
    private val serverUrl: String // e.g. "ws://100.101.102.103:8080/game"
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient { install(WebSockets) }

    private var session: WebSocketSession? = null

    private val _serverMessages = MutableSharedFlow<ServerMessage>()
    val serverMessages: SharedFlow<ServerMessage> = _serverMessages

    fun connect() {
        scope.launch {
            session = client.webSocketSession(serverUrl)
            listen()
        }
    }

    private suspend fun listen() {
        val s = session ?: return
        for (frame in s.incoming) {
            if (frame is Frame.Text) {
                val msg = json.decodeFromString<ServerMessage>(frame.readText())
                _serverMessages.emit(msg)
            }
        }
    }

    fun hostRoom(playerName: String) {
        send(ClientMessage.HostRoom(playerName))
    }

    fun joinRoom(roomCode: String, playerName: String) {
        send(ClientMessage.JoinRoom(roomCode, playerName))
    }

    private fun send(msg: ClientMessage) {
        scope.launch {
            val s = session ?: return@launch
            val text = json.encodeToString(msg)
            s.send(text)
        }
    }
}

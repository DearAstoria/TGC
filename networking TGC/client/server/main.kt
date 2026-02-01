package server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import protocol.ClientMessage
import protocol.ServerMessage

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(WebSockets)
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        routing {
            webSocket("/game") {
                println("Client connected: $this")

                sendSerialized(ServerMessage.Error("Welcome to TCG server (stub)."))

                incoming.consumeEach { frame ->
                    val text = (frame as? Frame.Text)?.readText() ?: return@consumeEach
                    println("Received raw: $text")

                    val msg = Json.decodeFromString<ClientMessage>(text)
                    handleClientMessage(msg, this)
                }
            }
        }
    }.start(wait = true)
}

suspend fun handleClientMessage(msg: ClientMessage, session: DefaultWebSocketServerSession) {
    when (msg) {
        is ClientMessage.PlayCard -> {
            println("PlayCard: ${msg.cardId} -> ${msg.targetId}")
            // TODO: hook into game engine, then broadcast new state
            val dummyState = """{"board":"stub"}"""
            session.sendSerialized(ServerMessage.GameStateUpdate(dummyState))
        }
        is ClientMessage.EndTurn -> {
            println("EndTurn by ${msg.playerId}")
            // TODO: update turn, then broadcast
        }
    }
}

suspend inline fun <reified T> DefaultWebSocketServerSession.sendSerialized(value: T) {
    val json = Json.encodeToString(T::class.serializer(), value)
    send(json)
}

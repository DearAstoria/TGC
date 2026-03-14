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

val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
}

fun main() {
    embeddedServer(Netty, port = 8080) {

        install(WebSockets)

        install(ContentNegotiation) {
            json(json)
        }

        routing {

            webSocket("/game") {

                ConnectionManager.add(this)

                try {

                    println("Client connected: $this")

                    sendSerialized(
                        ServerMessage.Error("Welcome to TCG server (stub).")
                    )

                    incoming.consumeEach { frame ->

                        val text = (frame as? Frame.Text)?.readText()
                            ?: return@consumeEach

                        println("Received raw: $text")

                        val msg = json.decodeFromString<ClientMessage>(text)

                        handleClientMessage(msg)

                    }

                } finally {

                    ConnectionManager.remove(this)

                }
            }
        }
    }.start(wait = true)
}


// TODO IMPLEMENTATION — now properly broadcasts to all clients
suspend fun handleClientMessage(msg: ClientMessage) {

    when (msg) {

        is ClientMessage.PlayCard -> {

            println("PlayCard: ${msg.cardId} -> ${msg.targetId}")

            // TODO: hook into game engine
            // Example stub game state
            val dummyState = """{"board":"updated_after_play"}"""

            val serverMsg =
                ServerMessage.GameStateUpdate(dummyState)

            val serialized =
                json.encodeToString(ServerMessage.serializer(), serverMsg)

            ConnectionManager.broadcast(serialized)
        }


        is ClientMessage.EndTurn -> {

            println("EndTurn by ${msg.playerId}")

            // TODO: hook into game engine
            val dummyState = """{"board":"next_turn"}"""

            val serverMsg =
                ServerMessage.GameStateUpdate(dummyState)

            val serialized =
                json.encodeToString(ServerMessage.serializer(), serverMsg)

            ConnectionManager.broadcast(serialized)
        }
    }
}


// helper function (still useful for single-client sends like welcome message)
suspend inline fun <reified T>
DefaultWebSocketServerSession.sendSerialized(value: T) {

    val serialized =
        json.encodeToString(
            kotlinx.serialization.serializer(),
            value
        )

    send(serialized)
}
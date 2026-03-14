package client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import protocol.ClientMessage
import protocol.ServerMessage

val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
}

fun main() = runBlocking {

    val client = HttpClient(CIO) {
        install(WebSockets)
    }

    client.webSocket("ws://localhost:8080/game") {

        println("Connected to server")

        // Listen for server messages
        launch {

            for (frame in incoming) {

                val text =
                    (frame as? Frame.Text)?.readText()
                        ?: continue

                println("Server raw: $text")

                try {

                    val msg =
                        json.decodeFromString(
                            ServerMessage.serializer(),
                            text
                        )

                    handleServerMessage(msg)

                } catch (e: Exception) {

                    println("Failed to decode message: ${e.message}")
                }
            }
        }


        // Send PlayCard test
        val playCardMsg =
            ClientMessage.PlayCard(
                cardId = "FIRE_DRAGON_001",
                targetId = "ENEMY_HERO"
            )

        sendSerialized(playCardMsg)


        delay(2000)


        // Send EndTurn test
        val endTurnMsg =
            ClientMessage.EndTurn(
                playerId = "player_stub"
            )

        sendSerialized(endTurnMsg)


        delay(10000)
    }

    client.close()
}



suspend fun DefaultClientWebSocketSession.sendSerialized(msg: ClientMessage) {

    val text =
        json.encodeToString(
            ClientMessage.serializer(),
            msg
        )

    send(text)
}



fun handleServerMessage(msg: ServerMessage) {

    when (msg) {

        is ServerMessage.GameStateUpdate -> {

            println("=== GAME STATE UPDATE ===")

            println("Turn Player: ${msg.state.turnPlayerId}")

            println("Players:")
            msg.state.players.forEach {
                println("  ${it.id} HP:${it.health}")
            }

            println("Board:")
            msg.state.board.forEach {
                println("  Card:${it.id} Owner:${it.ownerId}")
            }

            println("=========================")
        }


        is ServerMessage.Error -> {

            println("Server message: ${msg.message}")
        }
    }
}
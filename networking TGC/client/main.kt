package client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import protocol.ClientMessage
import protocol.ServerMessage

fun main() = runBlocking {
    val client = HttpClient(CIO) {
        install(WebSockets)
    }

    client.webSocket("ws://localhost:8080/game") {
        // Listen
        launch {
            for (frame in incoming) {
                val text = (frame as? Frame.Text)?.readText() ?: continue
                println("Server raw: $text")
                // You can decode to ServerMessage when ready:
                // val msg = Json.decodeFromString<ServerMessage>(text)
            }
        }

        // Send a test PlayCard
        val msg = ClientMessage.PlayCard(cardId = "FIRE_DRAGON_001", targetId = "ENEMY_HERO")
        val json = Json.encodeToString(ClientMessage.serializer(), msg)
        send(json)

        // Keep alive for a bit
        kotlinx.coroutines.delay(5000)
    }

    client.close()
}

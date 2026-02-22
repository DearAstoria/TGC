package server

import io.ktor.server.websocket.*
import java.util.concurrent.CopyOnWriteArraySet

object ConnectionManager {

    private val sessions = CopyOnWriteArraySet<DefaultWebSocketServerSession>()

    fun add(session: DefaultWebSocketServerSession) {
        sessions.add(session)
        println("Client added. Total clients: ${sessions.size}")
    }

    fun remove(session: DefaultWebSocketServerSession) {
        sessions.remove(session)
        println("Client removed. Total clients: ${sessions.size}")
    }

    suspend fun broadcast(message: String) {
        sessions.forEach { session ->
            session.send(message)
        }
    }

    suspend inline fun <reified T> broadcastSerialized(value: T) {
        val json = kotlinx.serialization.json.Json.encodeToString(
            kotlinx.serialization.serializer(),
            value
        )
        broadcast(json)
    }
}
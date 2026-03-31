package server

import io.ktor.websocket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

data class PlayerConnection(
    val name: String,
    val session: WebSocketSession
)

data class Room(
    val code: String,
    val players: MutableList<PlayerConnection> = mutableListOf()
)

class RoomManager {

    private val rooms = ConcurrentHashMap<String, Room>()

    fun createRoom(hostName: String, session: WebSocketSession): Room {
        val code = generateRoomCode()
        val room = Room(code)
        room.players += PlayerConnection(hostName, session)
        rooms[code] = room
        return room
    }

    fun joinRoom(code: String, playerName: String, session: WebSocketSession): Room? {
        val room = rooms[code] ?: return null
        room.players += PlayerConnection(playerName, session)
        return room
    }

    fun getPlayers(code: String): List<String> =
        rooms[code]?.players?.map { it.name } ?: emptyList()

    private fun generateRoomCode(): String =
        (1..6)
            .map { ('A'..'Z').random(Random) }
            .joinToString("")
}

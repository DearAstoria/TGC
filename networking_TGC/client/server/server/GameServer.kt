package server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import protocol.*
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class GameServer(
    private val port: Int = 8080
) {
    private val json = Json { ignoreUnknownKeys = true }

    // Room manager
    private val rooms = RoomManager()

    // Track which room each session belongs to
    private val sessionRoom = ConcurrentHashMap<WebSocketSession, String>()

    fun start() {
        embeddedServer(Netty, port = port) {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(30)
            }

            routing {
                webSocket("/game") {
                    println("Client connected")

                    try {
                        incoming.consumeEach { frame ->
                            when (frame) {
                                is Frame.Text -> handleTextMessage(this, frame.readText())
                                is Frame.Binary -> handleBinaryMessage(this, frame.data)
                                else -> Unit
                            }
                        }
                    } finally {
                        println("Client disconnected")
                        sessionRoom.remove(this)
                    }
                }
            }
        }.start(wait = false)
    }

    // -------------------------------
    // TEXT MESSAGE HANDLING (Rooms, Turns, PlayCard)
    // -------------------------------
    private suspend fun handleTextMessage(
        session: WebSocketSession,
        raw: String
    ) {
        val msg = try {
            json.decodeFromString<ClientMessage>(raw)
        } catch (e: Exception) {
            session.send(json.encodeToString(ServerMessage.Error("Invalid message format")))
            return
        }

        when (msg) {

            // -------------------------------
            // HOST ROOM
            // -------------------------------
            is ClientMessage.HostRoom -> {
                val room = rooms.createRoom(msg.playerName, session)
                sessionRoom[session] = room.code

                session.send(json.encodeToString(ServerMessage.RoomCreated(room.code)))
            }

            // -------------------------------
            // JOIN ROOM
            // -------------------------------
            is ClientMessage.JoinRoom -> {
                val room = rooms.joinRoom(msg.roomCode, msg.playerName, session)
                if (room == null) {
                    session.send(json.encodeToString(ServerMessage.RoomError("Room not found")))
                    return
                }

                sessionRoom[session] = room.code

                val players = rooms.getPlayers(room.code)
                val payload = json.encodeToString(ServerMessage.RoomJoined(room.code, players))

                room.players.forEach { it.session.send(payload) }
            }

            // -------------------------------
            // PLAY CARD
            // -------------------------------
            is ClientMessage.PlayCard -> {
                val roomCode = sessionRoom[session] ?: return
                broadcastToRoom(roomCode, ServerMessage.GameStateUpdate("{\"event\":\"play\"}"))
            }

            // -------------------------------
            // END TURN
            // -------------------------------
            is ClientMessage.EndTurn -> {
                val roomCode = sessionRoom[session] ?: return
                broadcastToRoom(roomCode, ServerMessage.GameStateUpdate("{\"event\":\"endTurn\"}"))
            }

            else -> Unit
        }
    }

    // -------------------------------
    // BINARY MESSAGE HANDLING (X,Y,Z)
    // -------------------------------
    private suspend fun handleBinaryMessage(
        session: WebSocketSession,
        bytes: ByteArray
    ) {
        val roomCode = sessionRoom[session] ?: return

        val move = try {
            BinaryProtocol.decodeMoveCard(bytes)
        } catch (e: Exception) {
            session.send(json.encodeToString(ServerMessage.Error("Invalid binary packet")))
            return
        }

        val msg = ServerMessage.CardMoved(move.cardId, move.x, move.y, move.z)
        broadcastToRoom(roomCode, msg)
    }

    // -------------------------------
    // BROADCAST HELPERS
    // -------------------------------
    private suspend fun broadcastToRoom(roomCode: String, msg: ServerMessage) {
        val room = rooms.getRoom(roomCode) ?: return
        val payload = json.encodeToString(msg)
        room.players.forEach { it.session.send(payload) }
    }
}

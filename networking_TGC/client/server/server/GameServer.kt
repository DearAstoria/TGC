package server
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import protocol.*
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
fun main() {
   embeddedServer(Netty, port = 8080) {
       install(WebSockets)
       routing {
           webSocket("/game") {
               GameServer.handleConnection(this)
           }
       }
   }.start(wait = true)
}
object GameServer {
   private val rooms = ConcurrentHashMap<String, GameRoom>()
   suspend fun handleConnection(session: WebSocketSession) {
       var player: Player? = null
       var room: GameRoom? = null
       try {
           session.incoming.consumeEach { frame ->
               when (frame) {
                   is Frame.Text -> {
                       val text = frame.readText()
                       val msg = Json.decodeFromString(ClientMessage.serializer(), text)
                       when (msg) {
                           is ClientMessage.HostRoom -> {
                               val code = generateRoomCode()
                               val newRoom = GameRoom(code)
                               rooms[code] = newRoom
                               player = newRoom.addPlayer(msg.playerName, session)
                               room = newRoom
                               session.sendSerialized(ServerMessage.RoomCreated(code))
                           }
                           is ClientMessage.JoinRoom -> {
                               val joinRoom = rooms[msg.roomCode]
                               if (joinRoom != null) {
                                   player = joinRoom.addPlayer(msg.playerName, session)
                                   room = joinRoom
                                   session.sendSerialized(
                                       ServerMessage.RoomJoined(
                                           roomCode = msg.roomCode,
                                           players = joinRoom.players.map { it.name },
                                           yourPlayerId = player!!.id
                                       )
                                   )
                                   joinRoom.broadcast(
                                       ServerMessage.PlayerJoined(msg.playerName)
                                   )
                               }
                           }
                           is ClientMessage.CardMove -> {
                               room?.broadcast(
                                   ServerMessage.CardMoved(
                                       cardId = msg.cardId,
                                       x = msg.x,
                                       y = msg.y,
                                       z = msg.z,
                                       playerId = player!!.id
                                   )
                               )
                           }
                           is ClientMessage.Chat -> {
                               room?.broadcast(
                                   ServerMessage.ChatBroadcast(
                                       playerName = msg.playerName,
                                       message = msg.message
                                   )
                               )
                           }
                           is ClientMessage.EndTurn -> {
                               // You can expand this later
                           }
                           ClientMessage.Ping -> {
                               session.sendSerialized(ServerMessage.Pong)
                           }
                       }
                   }
                   is Frame.Binary -> {
                       val bytes = frame.readBytes()
                       if (bytes.isNotEmpty() && bytes[0] == 0x01.toByte()) {
                           val move = decodeBinaryMove(bytes)
                           room?.broadcast(
                               ServerMessage.CardMoved(
                                   cardId = move.cardId,
                                   x = move.x,
                                   y = move.y,
                                   z = move.z,
                                   playerId = player!!.id
                               )
                           )
                       }
                   }
                   else -> {}
               }
           }
       } finally {
           if (player != null && room != null) {
               room.removePlayer(player!!)
           }
       }
   }
   private fun generateRoomCode(): String =
       (1000..9999).random().toString()
   private fun decodeBinaryMove(bytes: ByteArray): ClientMessage.CardMove {
       val buffer = ByteBuffer.wrap(bytes)
       buffer.position(1)
       val cardId = buffer.int
       val x = buffer.float
       val y = buffer.float
       val z = buffer.get().toInt()
       return ClientMessage.CardMove(cardId, x, y, z)
   }
}
data class Player(
   val id: Int,
   val name: String,
   val session: WebSocketSession
)
class GameRoom(val code: String) {
   private val _players = Collections.synchronizedList(mutableListOf<Player>())
   val players: List<Player> get() = _players
   private var nextPlayerId = 1
   fun addPlayer(name: String, session: WebSocketSession): Player {
       val player = Player(nextPlayerId++, name, session)
       _players.add(player)
       return player
   }
   fun removePlayer(player: Player) {
       _players.remove(player)
   }
   suspend fun broadcast(msg: ServerMessage) {
       val json = Json.encodeToString(ServerMessage.serializer(), msg)
       _players.forEach { it.session.send(Frame.Text(json)) }
   }
}
suspend fun WebSocketSession.sendSerialized(msg: ServerMessage) {
   val json = Json.encodeToString(ServerMessage.serializer(), msg)
   send(Frame.Text(json))
}
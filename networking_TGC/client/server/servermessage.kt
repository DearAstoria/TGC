package protocol
import kotlinx.serialization.Serializable
@Serializable
sealed class ServerMessage {
   // Sent when a room is successfully created
   @Serializable
   data class RoomCreated(val roomCode: String) : ServerMessage()
   // Sent when a player successfully joins a room
   @Serializable
   data class RoomJoined(
       val roomCode: String,
       val players: List<String>,
       val yourPlayerId: Int
   ) : ServerMessage()
   // Broadcast when a new player joins
   @Serializable
   data class PlayerJoined(val playerName: String) : ServerMessage()
   // Broadcast when a player moves a card
   @Serializable
   data class CardMoved(
       val cardId: Int,
       val x: Float,
       val y: Float,
       val z: Int,
       val playerId: Int
   ) : ServerMessage()
   // Full game state sync
   @Serializable
   data class GameStateUpdate(
       val cards: List<CardState>,
       val currentTurnPlayerId: Int
   ) : ServerMessage()
   // Chat broadcast
   @Serializable
   data class ChatBroadcast(val playerName: String, val message: String) : ServerMessage()
   // Server heartbeat
   @Serializable
   object Pong : ServerMessage()
}
@Serializable
data class CardState(
   val cardId: Int,
   val x: Float,
   val y: Float,
   val z: Int,
   val ownerId: Int
)
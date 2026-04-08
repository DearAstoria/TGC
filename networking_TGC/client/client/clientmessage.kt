package protocol
import kotlinx.serialization.Serializable
@Serializable
sealed class ClientMessage {
   // When a player wants to host a room
   @Serializable
   data class HostRoom(val playerName: String) : ClientMessage()
   // When a player wants to join a room
   @Serializable
   data class JoinRoom(val roomCode: String, val playerName: String) : ClientMessage()
   // When a player moves a card (binary X/Y/Z)
   @Serializable
   data class CardMove(val cardId: Int, val x: Float, val y: Float, val z: Int) : ClientMessage()
   // When a player ends their turn
   @Serializable
   data class EndTurn(val playerId: Int) : ClientMessage()
   // Chat message
   @Serializable
   data class Chat(val playerName: String, val message: String) : ClientMessage()
   // Ping for connection stability
   @Serializable
   object Ping : ClientMessage()
}
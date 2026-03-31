package protocol

import kotlinx.serialization.Serializable

@Serializable
sealed class ClientMessage {

    @Serializable
    data class HostRoom(val playerName: String) : ClientMessage()

    @Serializable
    data class JoinRoom(val roomCode: String, val playerName: String) : ClientMessage()

    @Serializable
    data class PlayCard(val cardId: String, val targetId: String?) : ClientMessage()

    @Serializable
    data class EndTurn(val playerId: String) : ClientMessage()

    @Serializable
    data class MoveCard(val cardId: String, val x: Float, val y: Float, val z: Float) : ClientMessage()
}

@Serializable
sealed class ServerMessage {

    @Serializable
    data class RoomCreated(val roomCode: String) : ServerMessage()

    @Serializable
    data class RoomJoined(val roomCode: String, val players: List<String>) : ServerMessage()

    @Serializable
    data class RoomError(val message: String) : ServerMessage()

    @Serializable
    data class GameStateUpdate(val stateJson: String) : ServerMessage()

    @Serializable
    data class Error(val message: String) : ServerMessage()

    @Serializable
    data class CardMoved(val cardId: String, val x: Float, val y: Float, val z: Float) : ServerMessage()
}

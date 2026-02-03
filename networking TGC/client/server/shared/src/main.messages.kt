package protocol

import kotlinx.serialization.Serializable

@Serializable
sealed class ClientMessage {
    @Serializable
    data class PlayCard(val cardId: String, val targetId: String?) : ClientMessage()

    @Serializable
    data class EndTurn(val playerId: String) : ClientMessage()
}

@Serializable
sealed class ServerMessage {
    @Serializable
    data class GameStateUpdate(val stateJson: String) : ServerMessage()

    @Serializable
    data class Error(val message: String) : ServerMessage()
}

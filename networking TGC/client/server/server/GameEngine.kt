package server

import kotlinx.serialization.Serializable
import protocol.ClientMessage

object GameEngine {

    private val players = mutableListOf<PlayerState>()

    private val cardsOnBoard = mutableListOf<CardState>()

    private var currentTurnPlayerId: String? = null


    fun addPlayer(playerId: String) {

        if (players.any { it.id == playerId }) return

        players.add(
            PlayerState(
                id = playerId,
                health = 20
            )
        )

        if (currentTurnPlayerId == null)
            currentTurnPlayerId = playerId

        println("Player added: $playerId")
    }


    fun handleMessage(msg: ClientMessage) {

        when (msg) {

            is ClientMessage.PlayCard ->
                handlePlayCard(msg)

            is ClientMessage.EndTurn ->
                handleEndTurn(msg)
        }
    }


    private fun handlePlayCard(msg: ClientMessage) {

        cardsOnBoard.add(
            CardState(
                id = msg.cardId,
                ownerId = "player_stub",
                zone = "board"
            )
        )

        println("Card played: ${msg.cardId}")
    }


    private fun handleEndTurn(msg: ClientMessage) {

        val currentIndex =
            players.indexOfFirst { it.id == msg.playerId }

        if (currentIndex == -1) return

        val nextIndex =
            (currentIndex + 1) % players.size

        currentTurnPlayerId =
            players[nextIndex].id

        println("Turn changed to: $currentTurnPlayerId")
    }


    fun getGameState(): GameState {

        return GameState(
            players = players.toList(),
            board = cardsOnBoard.toList(),
            turnPlayerId = currentTurnPlayerId ?: ""
        )
    }
}


@Serializable
data class GameState(
    val players: List<PlayerState>,
    val board: List<CardState>,
    val turnPlayerId: String
)


@Serializable
data class PlayerState(
    val id: String,
    val health: Int
)


@Serializable
data class CardState(
    val id: String,
    val ownerId: String,
    val zone: String
)
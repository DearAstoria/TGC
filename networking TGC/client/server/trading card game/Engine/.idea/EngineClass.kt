class TGCEngine {
    // Current state of the entire global game
    var currentState: GameState = GameState.Lobby

    // The "World Map" of all cards
    val boardState = mutableMapOf<Int, CardPosition>()

    fun handleAction(action: GameAction) {
        when (val state = currentState) {
            is GameState.Lobby -> handleLobby(action)
            is GameState.PlayerTurn -> handleTurn(state, action)
            is GameState.CombatResolution -> resolveCombat(state)
            is GameState.GameOver -> println("Game is over. No actions allowed.")
        }
    }

    private fun handleTurn(state: GameState.PlayerTurn, action: GameAction) {
        when (action) {
            is GameAction.MoveCard -> {
                // AUTHORITATIVE CHECK: Only allow move if it's the player's turn
                println("Moving card ${action.cardId} to ${action.x}, ${action.y}")
                boardState[action.cardId] = CardPosition(action.x, action.y)
                // Broadcast this to all 4 states via Sockets next!
            }
            is GameAction.EndTurn -> {
                // Logic to swap to the next player
                currentState = GameState.PlayerTurn(nextPlayerId(state.playerId))
            }
            else -> println("Invalid action for Turn phase.")
        }
    }
}


see less
Reply
has context menu
Post in channel
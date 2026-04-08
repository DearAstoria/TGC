fun validateAttack(
    state: GameState,
    player: PlayerId,
    action: ClientAction.Attack
): RejectReason? {
    if (state.currentPlayer != player) return RejectReason.NOT_YOUR_TURN
    if (!state.isControlledBy(action.attacker, player)) return RejectReason.NOT_YOUR_UNIT
    return null
}

fun drawCard(player: PlayerId): List<GameEvent> {
    val deck = state.zones.decks[player]!!
    if (deck.isEmpty()) return emptyList()

    val cardId = deck.removeAt(0)
    state.zones.hands[player]!!.add(cardId)

    return listOf(GameEvent.CardDrawn(player, cardId))
}

fun validateCardDraw(
    state: GameState,
    player: PlayerId
): RejectReason? {
    if(state.currentPLayer!= player) return RejectReason.NOT_YOUR_TURN
    return null

    class ActionValidator(private val state: GameState) {

        fun validate(player: PlayerId, action: ClientAction): RejectReason? {
            if (state.currentPlayer != player) {
                return RejectReason.NOT_YOUR_TURN
            }

            return when (action) {
                is ClientAction.Attack -> validateAttack(action)
                is ClientAction.EndPhase -> null
            }
        }

        private fun validateAttack(action: ClientAction.Attack): RejectReason? {
            if (state.phase != GamePhase.COMBAT) {
                return RejectReason.BAD_PHASE
            }

            val attacker = state.entities[action.attacker] ?: return RejectReason.INVALID_ENTITY

            if (attacker.owner != state.currentPlayer) {
                return RejectReason.NOT_YOUR_UNIT
            }

            return null
        }
    }
}
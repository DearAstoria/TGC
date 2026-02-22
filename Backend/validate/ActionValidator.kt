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
}
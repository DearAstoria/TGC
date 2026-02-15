fun validateAttack(
    state: GameState,
    player: PlayerId,
    action: ClientAction.Attack
): RejectReason? {
    if (state.currentPlayer != player) return RejectReason.NOT_YOUR_TURN
    if (!state.isControlledBy(action.attacker, player)) return RejectReason.NOT_YOUR_UNIT
    return null
}

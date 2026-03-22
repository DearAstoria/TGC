class TurnSystem(
    private val state: GameState,
    private val rulesEngine: RulesEngine
) {
    fun startTurn(player: PlayerId): List<GameEvent> {
        val events = mutableListOf<GameEvent>()

        state.currentPlayer = player

        events += refreshResources(player)
        events += drawCard(player)
        events += processStatuses(player)

        return events
    }

    fun endTurn(): List<GameEvent> {
        val nextPlayer = getNextPlayer()
        return startTurn(nextPlayer)
    }

    private fun refreshResources(player: PlayerId): List<GameEvent> {
        return emptyList()
    }

    private fun drawCard(player: PlayerId): List<GameEvent> {
        return rulesEngine.drawCard(player)
    }

    private fun processStatuses(player: PlayerId): List<GameEvent> {
        val events = mutableListOf<GameEvent>()

        val poisonedUnits = state.entities.values
            .filter { it.owner == player }
            .filter { it.statuses.any { s -> s is Status.Poison } }

        for (unit in poisonedUnits) {
            val poison = unit.statuses.filterIsInstance<Status.Poison>().first()
            events += rulesEngine.dealDamage(
                source = unit.id,
                target = unit.id,
                amount = poison.damagePerTurn
            )
        }

        return events
    }

    private fun getNextPlayer(): PlayerId {
        return if (state.currentPlayer.value == 1) PlayerId(2) else PlayerId(1)
    }
}
class TurnSystem(
    private val state: GameState,
    private val rules: RulesEngine
) {

    fun advancePhase(): List<GameEvent> {
        val events = mutableListOf<GameEvent>()

        state.phase = nextPhase()

        when (state.phase) {
            GamePhase.TURN_START -> {
                events += onTurnStart(state.currentPlayer)
            }
            GamePhase.MAIN -> {
                // nothing automatic
            }
            GamePhase.COMBAT -> {
                // could prep combat here
            }
            GamePhase.TURN_END -> {
                events += onTurnEnd()
            }
        }

        return events
    }

    private fun nextPhase(): GamePhase {
        return when (state.phase) {
            GamePhase.TURN_START -> GamePhase.MAIN
            GamePhase.MAIN -> GamePhase.COMBAT
            GamePhase.COMBAT -> GamePhase.TURN_END
            GamePhase.TURN_END -> GamePhase.TURN_START
        }
    }

    private fun onTurnStart(player: PlayerId): List<GameEvent> {
        val events = mutableListOf<GameEvent>()

        // draw
        events += rules.drawCard(player)

        // poison (example)
        val poisoned = state.entities.values
            .filter { it.owner == player }
            .filter { it.statuses.any { s -> s is Status.Poison } }

        for (unit in poisoned) {
            val poison = unit.statuses.filterIsInstance<Status.Poison>().first()
            events += rules.dealDamage(unit.id, unit.id, poison.damagePerTurn)
        }

        return events
    }

    private fun onTurnEnd(): List<GameEvent> {
        // switch player
        state.currentPlayer =
            if (state.currentPlayer.value == 1) PlayerId(2) else PlayerId(1)

        return emptyList()
    }
}
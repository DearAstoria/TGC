class MatchService(
    private val state: GameState,
    private val rules: RulesEngine,
    private val turnSystem: TurnSystem,
    private val validator: ActionValidator
) {

    fun handleAction(player: PlayerId, action: ClientAction): ServerResponse {

        val error = validator.validate(player, action)
        if (error != null) {
            return ServerResponse.Rejected(error)
        }

        val events = mutableListOf<GameEvent>()

        when (action) {
            is ClientAction.Attack -> {
                val atk = state.entities[action.attacker]!!.attack
                events += rules.dealDamage(action.attacker, action.defender, atk)
            }

            is ClientAction.EndPhase -> {
                events += turnSystem.advancePhase()
            }
        }

        return ServerResponse.Accepted(events)
    }
}
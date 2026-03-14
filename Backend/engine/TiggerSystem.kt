fun handle(event: GameEvent): List<GameEvent> {
    return when (event) {
        is GameEvent.DamageDealt ->
            runOnDamageDealtTriggers(event)
        else -> emptyList()
    }
}

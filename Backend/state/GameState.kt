data class GameState(
    val entities: MutableMap<EntityId, Entity>
    val cardInstances: MutableMap<CardInstanceId, CardInstance>
    val zones: Zones

    data class GameState(
        var currentPlayer: PlayerId,
        var phase: GamePhase,
        val entities: MutableMap<EntityId, Entity>,
        val zones: Zones
    )
)

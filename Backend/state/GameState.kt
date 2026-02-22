data class GameState(
    val entities: MutableMap<EntityId, Entity>
    val cardInstances: MutableMap<CardInstanceId, CardInstance>
    val zones: Zones
)

sealed class GameEvent {
    data class DamageDealt(
        val source: EntityId,
        val target: EntityId,
        val amount: Int
    ) : GameEvent()

    data class DamageDealt(
        val source: EntityId,
        val target: EntityId,
        val amount: Int
    ) : GameEvent()
}

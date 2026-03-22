sealed class GameEvent {
    data class DamageDealt(
        val source: EntityId,
        val target: EntityId,
        val amount: Int
    ) : GameEvent()

    data class Heal(
        val source: EntityId,
        val target: EntityId,
        val amount: Int
    ) : GameEvent()

    data class Poison(
        val source: EntityId,
        val target: EntityId,
        val duration: Int
    ) : GameEvent()

    data class Intimidate(
        val source: EntityId,
        val target: EntityId,
        val damageReduction: Int
    )
}

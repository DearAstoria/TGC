fun Heal(source: EntityId, target: EntityId, amount: Int) List<GameEvent> {
    val newHpValue = state.entities[target].hp + amount
    state.entities[target] = state.entities[target].copy(hp = newHpValue)
    return listOf(GameEvent.DamageDealt(source, target, amount))
}
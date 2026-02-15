fun resolveAttack(attacker: EntityId, defender: EntityId): List<GameEvent> {
    val atk = state.entities[attacker]!!.attack
    return dealDamage(attacker, defender, atk)
}

fun dealDamage(
    source: EntityId,
    target: EntityId,
    amount: Int
): List<GameEvent> {

    val entity = state.entities[target] ?: return emptyList()
    val newHp = entity.hp - amount

    state.entities[target] = entity.copy(hp = newHp)

    return listOf(
        GameEvent.DamageDealt(source, target, amount)
    )
}

fun dealDamage(
    source: EntityId,
    target: EntityId,
    amount: Int
): List<GameEvent> {

    val entity = state.entities[target] ?: return emptyList()
    val newHp = entity.hp + amount

    state.entities[target] = entity.copy(hp = newHp)

    return listOf(GameEvent.DamageDealt(source, target, amount)
    )
}
)
fun checkDeaths(): List<GameEvent> {
    val dead = state.entities.values.filter { it.hp <= 0 }
    dead.forEach { state.entities.remove(it.id) }
    return dead.map { GameEvent.UnitDied(it.id) }
}

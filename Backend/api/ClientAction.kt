sealed class ClientAction {
    data class Attack(
        val attacker: EntityId,
        val defender: EntityId
    ) : ClientAction()

    data class Heal(
        val healer: EntityId,
        val target: EntityId
    ) : ClientAction()

    data class LifeSteal(
        val attacker: EntityId,
        val target: EntityId
    ) : ClientAction()
}
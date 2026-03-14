sealed class Status{
    data class Poison(val damagePerTurn: Int) : Status
}
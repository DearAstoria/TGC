sealed class Status{
    data class Poison(val damagePerTurn: Int) : Status
    
    data class Intimidate(val damageReduction: Int) : Status
}
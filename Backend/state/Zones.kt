data class Zones(
    val decks: MutableMap<PlayerId, MutableList<CardInstanceId>>,
    val hands: MutableMap<PlayerId, MutableList<CardInstanceId>>,
    val graveyards: MutableMap<PlayerId, MutableList<CardInstanceId>>
)
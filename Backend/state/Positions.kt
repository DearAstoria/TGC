data class Vec2(
    val x: Float,
    val y: Float
)

object SharedState {
    val playerPositions = mutableMapOf<String, Vec2>()
}
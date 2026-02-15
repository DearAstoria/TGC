sealed class ServerResponse {
    data class Accepted(val events: List<GameEvent>) : ServerResponse()
    data class Rejected(val reason: RejectReason) : ServerResponse()
}

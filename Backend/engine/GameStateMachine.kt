class GameStateMachine {

    fun nextPhase(current: GamePhase): GamePhase {
        return when (current) {
            GamePhase.TURN_START -> GamePhase.MAIN
            GamePhase.MAIN -> GamePhase.COMBAT
            GamePhase.COMBAT -> GamePhase.TURN_END
            GamePhase.TURN_END -> GamePhase.TURN_START
        }
    }
}
package com.yourgame.tcg.game
import com.yourgame.tcg.model.GameState
object GameLogic {
   fun endTurn(state: GameState) {
       state.currentTurnPlayerId = if (state.currentTurnPlayerId == 1) 2 else 1
   }
}
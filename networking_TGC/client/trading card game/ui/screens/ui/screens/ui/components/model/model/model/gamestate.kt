package com.yourgame.tcg.model
data class GameState(
   val cards: MutableList<Card> = mutableListOf(),
   var currentTurnPlayerId: Int = 1
)
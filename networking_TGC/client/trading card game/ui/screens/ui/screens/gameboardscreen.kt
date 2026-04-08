package com.yourgame.tcg.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.yourgame.tcg.ui.components.DraggableCard
import com.yourgame.tcg.vm.GameViewModel
@Composable
fun GameBoardScreen(vm: GameViewModel) {
   val cardMoves = vm.cardMoves.collectAsState(initial = null)
   Box(Modifier.fillMaxSize()) {
       // Example card
       DraggableCard(cardId = 1, vm = vm)
       // Listen for incoming moves
       cardMoves.value?.let { move ->
           // TODO: Update card positions in your game state
       }
   }
}
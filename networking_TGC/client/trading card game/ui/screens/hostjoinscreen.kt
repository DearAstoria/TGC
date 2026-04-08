package com.yourgame.tcg.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourgame.tcg.vm.GameViewModel
@Composable
fun HostJoinScreen(vm: GameViewModel, onEnterGame: () -> Unit) {
   val roomCode by vm.roomCode.collectAsState()
   Column(Modifier.padding(20.dp)) {
       if (roomCode == null) {
           Text("Multiplayer Lobby", style = MaterialTheme.typography.headlineMedium)
           Spacer(Modifier.height(20.dp))
           Button(onClick = { vm.hostRoom("Player1") }) {
               Text("Host Room")
           }
           Spacer(Modifier.height(20.dp))
           var code by remember { mutableStateOf("") }
           OutlinedTextField(
               value = code,
               onValueChange = { code = it },
               label = { Text("Room Code") }
           )
           Button(onClick = { vm.joinRoom(code, "Player1") }) {
               Text("Join Room")
           }
       } else {
           Text("Room Code: $roomCode")
           Button(onClick = onEnterGame) { Text("Enter Game") }
       }
   }
}